package com.moonrider.zamazon.service;

import com.moonrider.zamazon.dto.IdentifyRequest;
import com.moonrider.zamazon.dto.IdentifyResponse;
import com.moonrider.zamazon.entity.Contact;
import com.moonrider.zamazon.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact primaryContact;
    private Contact secondaryContact;

    @BeforeEach
    void setUp() {
        primaryContact = new Contact("doc@timelab.com", "123456789", Contact.LinkPrecedence.PRIMARY);
        primaryContact.setId(1L);
        primaryContact.setCreatedAt(LocalDateTime.now().minusDays(1));

        secondaryContact = new Contact("doc2@timelab.com", "987654321", Contact.LinkPrecedence.SECONDARY);
        secondaryContact.setId(2L);
        secondaryContact.setLinkedId(1L);
        secondaryContact.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateNewPrimaryContact() {
        // Arrange
        IdentifyRequest request = new IdentifyRequest("new@timelab.com", "555666777");
        Contact newContact = new Contact("new@timelab.com", "555666777", Contact.LinkPrecedence.PRIMARY);
        newContact.setId(3L);

        when(contactRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenReturn(new ArrayList<>());
        when(contactRepository.save(any(Contact.class))).thenReturn(newContact);

        // Act
        IdentifyResponse response = contactService.identify(request);

        // Assert
        assertNotNull(response);
        assertEquals(3L, response.getPrimaryContactId());
        assertTrue(response.getEmails().contains("new@timelab.com"));
        assertTrue(response.getPhoneNumbers().contains("555666777"));
        assertTrue(response.getSecondaryContactIds().isEmpty());

        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void testIdentifyExistingContact() {
        // Arrange
        IdentifyRequest request = new IdentifyRequest("doc@timelab.com", "123456789");

        when(contactRepository.findByEmailOrPhoneNumber("doc@timelab.com", "123456789"))
                .thenReturn(Arrays.asList(primaryContact));
        when(contactRepository.findByLinkedId(1L))
                .thenReturn(Arrays.asList(secondaryContact));

        // Act
        IdentifyResponse response = contactService.identify(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPrimaryContactId());
        assertTrue(response.getEmails().contains("doc@timelab.com"));
        assertTrue(response.getPhoneNumbers().contains("123456789"));
    }

    @Test
    void testCreateSecondaryContactWithNewInfo() {
        // Arrange
        IdentifyRequest request = new IdentifyRequest("doc@timelab.com", "999888777");
        Contact newSecondary = new Contact("doc@timelab.com", "999888777", Contact.LinkPrecedence.SECONDARY);
        newSecondary.setId(3L);
        newSecondary.setLinkedId(1L);

        when(contactRepository.findByEmailOrPhoneNumber("doc@timelab.com", "999888777"))
                .thenReturn(Arrays.asList(primaryContact));
        when(contactRepository.findByLinkedId(1L))
                .thenReturn(Arrays.asList(secondaryContact));
        when(contactRepository.save(any(Contact.class)))
                .thenReturn(newSecondary);

        // Act
        IdentifyResponse response = contactService.identify(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPrimaryContactId());
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void testContactConsolidation() {
        // Arrange
        Contact secondPrimary = new Contact("doc3@timelab.com", "111222333", Contact.LinkPrecedence.PRIMARY);
        secondPrimary.setId(4L);
        secondPrimary.setCreatedAt(LocalDateTime.now()); // Newer than first primary

        IdentifyRequest request = new IdentifyRequest("doc@timelab.com", "111222333");

        when(contactRepository.findByEmailOrPhoneNumber("doc@timelab.com", "111222333"))
                .thenReturn(Arrays.asList(primaryContact, secondPrimary));
        when(contactRepository.findByLinkedId(1L))
                .thenReturn(Arrays.asList(secondaryContact));
        when(contactRepository.findByLinkedId(4L))
                .thenReturn(new ArrayList<>());
        when(contactRepository.findActiveById(anyLong()))
                .thenReturn(Optional.of(primaryContact));

        // Act
        IdentifyResponse response = contactService.identify(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPrimaryContactId()); // Oldest primary should remain
        verify(contactRepository, atLeastOnce()).save(any(Contact.class));
    }

    @Test
    void testInvalidRequest() {
        // Arrange
        IdentifyRequest request = new IdentifyRequest(null, null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> contactService.identify(request));
    }

    @Test
    void testDatabaseError() {
        // Arrange
        IdentifyRequest request = new IdentifyRequest("doc@timelab.com", "123456789");
        when(contactRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> contactService.identify(request));
    }

    @Test
    void testComplexScenarioWithMultipleLinks() {
        // Arrange - Complex scenario with multiple secondary contacts
        Contact secondary1 = new Contact("doc.alt1@timelab.com", null, Contact.LinkPrecedence.SECONDARY);
        secondary1.setId(5L);
        secondary1.setLinkedId(1L);

        Contact secondary2 = new Contact(null, "444555666", Contact.LinkPrecedence.SECONDARY);
        secondary2.setId(6L);
        secondary2.setLinkedId(1L);

        IdentifyRequest request = new IdentifyRequest("doc.alt1@timelab.com", "444555666");

        when(contactRepository.findByEmailOrPhoneNumber("doc.alt1@timelab.com", "444555666"))
                .thenReturn(Arrays.asList(secondary1, secondary2));
        when(contactRepository.findActiveById(1L))
                .thenReturn(Optional.of(primaryContact));
        when(contactRepository.findByLinkedId(1L))
                .thenReturn(Arrays.asList(secondaryContact, secondary1, secondary2));

        // Act
        IdentifyResponse response = contactService.identify(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getPrimaryContactId());
        assertEquals(3, response.getSecondaryContactIds().size());
        assertTrue(response.getEmails().contains("doc.alt1@timelab.com"));
        assertTrue(response.getPhoneNumbers().contains("444555666"));
    }
}
