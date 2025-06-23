package com.moonrider.zamazon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonrider.zamazon.dto.IdentifyRequest;
import com.moonrider.zamazon.entity.Contact;
import com.moonrider.zamazon.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional

public class ContactControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
    }

    @Test
    void testIdentifyEndpointNewContact() throws Exception {
        IdentifyRequest request = new IdentifyRequest("test@timelab.com", "123456789");

        mockMvc.perform(post("/api/identify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryContactId").exists())
                .andExpect(jsonPath("$.emails[0]").value("test@timelab.com"))
                .andExpect(jsonPath("$.phoneNumbers[0]").value("123456789"))
                .andExpect(jsonPath("$.secondaryContactIds").isEmpty());
    }

    @Test
    void testIdentifyEndpointExistingContact() throws Exception {
        // Setup existing contact
        Contact existing = new Contact("existing@timelab.com", "987654321", Contact.LinkPrecedence.PRIMARY);
        contactRepository.save(existing);

        IdentifyRequest request = new IdentifyRequest("existing@timelab.com", "555666777");

        mockMvc.perform(post("/api/identify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryContactId").exists())
                .andExpect(jsonPath("$.emails").isArray())
                .andExpect(jsonPath("$.phoneNumbers").isArray());
    }

    @Test
    void testIdentifyEndpointInvalidRequest() throws Exception {
        IdentifyRequest request = new IdentifyRequest(null, null);

        mockMvc.perform(post("/api/identify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIdentifyEndpointMalformedJson() throws Exception {
        mockMvc.perform(post("/api/identify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}
