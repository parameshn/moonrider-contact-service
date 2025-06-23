package com.moonrider.zamazon.Controller;

import com.moonrider.zamazon.dto.IdentifyRequest;
import com.moonrider.zamazon.dto.IdentifyResponse;
import com.moonrider.zamazon.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/identify")
    public ResponseEntity<?> identify(@RequestBody IdentifyRequest request) {
        try {
            IdentifyResponse response = contactService.identify(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters");
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Service temporarily unavailable");
        }
    }

    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        return ResponseEntity.status(status).body(error);
    }
}
