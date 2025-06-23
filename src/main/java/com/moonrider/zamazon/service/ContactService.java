package com.moonrider.zamazon.service;

import com.moonrider.zamazon.dto.IdentifyRequest;
import com.moonrider.zamazon.dto.IdentifyResponse;
import com.moonrider.zamazon.entity.Contact;
import com.moonrider.zamazon.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public IdentifyResponse identify(IdentifyRequest request) {
        // 1) Validate up front; let IllegalArgumentException bubble
        if (request.getEmail() == null && request.getPhoneNumber() == null) {
            throw new IllegalArgumentException("Either email or phone number must be provided");
        }

        // 2) Lookup existing contacts
        List<Contact> existingContacts = contactRepository.findByEmailOrPhoneNumber(request.getEmail(),
                request.getPhoneNumber());

        if (existingContacts.isEmpty()) {
            return createNewPrimaryContact(request);
        }

        return processExistingContacts(request, existingContacts);
    }

    private IdentifyResponse createNewPrimaryContact(IdentifyRequest request) {
        Contact newContact = new Contact(request.getEmail(), request.getPhoneNumber(),
                Contact.LinkPrecedence.PRIMARY);
        Contact savedContact = contactRepository.save(newContact);

        List<String> emails = savedContact.getEmail() != null ? Arrays.asList(savedContact.getEmail())
                : new ArrayList<>();
        List<String> phoneNumbers = savedContact.getPhoneNumber() != null ? Arrays.asList(savedContact.getPhoneNumber())
                : new ArrayList<>();

        return new IdentifyResponse(savedContact.getId(), emails, phoneNumbers, new ArrayList<>());
    }

    private IdentifyResponse processExistingContacts(IdentifyRequest request, List<Contact> existingContacts) {
        // Find all related contacts (primary and secondary)
        Set<Contact> allRelatedContacts = findAllRelatedContacts(existingContacts);

        // Determine primary contact
        Contact primaryContact = findPrimaryContact(allRelatedContacts);

        // Check if we need to create a new secondary contact
        boolean needsNewSecondary = shouldCreateNewSecondary(request, allRelatedContacts);

        if (needsNewSecondary) {
            Contact newSecondary = new Contact(request.getEmail(), request.getPhoneNumber(),
                    Contact.LinkPrecedence.SECONDARY);
            newSecondary.setLinkedId(primaryContact.getId());
            contactRepository.save(newSecondary);
            allRelatedContacts.add(newSecondary);
        }

        // Handle contact consolidation if multiple primaries exist
        handleContactConsolidation(allRelatedContacts);

        // Rebuild the related contacts set after potential consolidation
        allRelatedContacts = findAllRelatedContacts(Arrays.asList(primaryContact));
        primaryContact = findPrimaryContact(allRelatedContacts);

        return buildResponse(primaryContact, allRelatedContacts);
    }

    private Set<Contact> findAllRelatedContacts(List<Contact> seedContacts) {
        Set<Contact> allContacts = new HashSet<>();
        Set<Long> processedIds = new HashSet<>();

        for (Contact contact : seedContacts) {
            collectRelatedContacts(contact, allContacts, processedIds);
        }

        return allContacts;
    }

    private void collectRelatedContacts(Contact contact, Set<Contact> allContacts, Set<Long> processedIds) {
        if (processedIds.contains(contact.getId())) {
            return;
        }

        processedIds.add(contact.getId());
        allContacts.add(contact);

        // Find contacts linked to this one
        List<Contact> linkedContacts = contactRepository.findByLinkedId(contact.getId());
        for (Contact linked : linkedContacts) {
            collectRelatedContacts(linked, allContacts, processedIds);
        }

        // If this is a secondary contact, find its primary
        if (contact.getLinkedId() != null) {
            Optional<Contact> primary = contactRepository.findActiveById(contact.getLinkedId());
            if (primary.isPresent()) {
                collectRelatedContacts(primary.get(), allContacts, processedIds);
            }
        }
    }

    private Contact findPrimaryContact(Set<Contact> contacts) {
        return contacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No primary contact found"));
    }

    private boolean shouldCreateNewSecondary(IdentifyRequest request, Set<Contact> existingContacts) {
        boolean emailExists = request.getEmail() == null ||
                existingContacts.stream().anyMatch(c -> request.getEmail().equals(c.getEmail()));
        boolean phoneExists = request.getPhoneNumber() == null ||
                existingContacts.stream().anyMatch(c -> request.getPhoneNumber().equals(c.getPhoneNumber()));

        return !(emailExists && phoneExists);
    }

    private void handleContactConsolidation(Set<Contact> allContacts) {
        List<Contact> primaries = allContacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .sorted(Comparator.comparing(Contact::getCreatedAt))
                .collect(Collectors.toList());

        if (primaries.size() > 1) {
            Contact oldestPrimary = primaries.get(0);

            // Convert newer primaries to secondaries
            for (int i = 1; i < primaries.size(); i++) {
                Contact primaryToConvert = primaries.get(i);
                primaryToConvert.setLinkPrecedence(Contact.LinkPrecedence.SECONDARY);
                primaryToConvert.setLinkedId(oldestPrimary.getId());
                primaryToConvert.setUpdatedAt(LocalDateTime.now());
                contactRepository.save(primaryToConvert);

                // Update contacts that were linked to the converted primary
                List<Contact> childContacts = contactRepository.findByLinkedId(primaryToConvert.getId());
                for (Contact child : childContacts) {
                    child.setLinkedId(oldestPrimary.getId());
                    child.setUpdatedAt(LocalDateTime.now());
                    contactRepository.save(child);
                }
            }
        }
    }

    private IdentifyResponse buildResponse(Contact primaryContact, Set<Contact> allContacts) {
        List<String> emails = allContacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> phoneNumbers = allContacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Long> secondaryContactIds = allContacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.SECONDARY)
                .map(Contact::getId)
                .sorted()
                .collect(Collectors.toList());

        return new IdentifyResponse(primaryContact.getId(), emails, phoneNumbers, secondaryContactIds);
    }
}
