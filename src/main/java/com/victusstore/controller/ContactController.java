package com.victusstore.controller;

import com.victusstore.dto.ContactMessageRequest;
import com.victusstore.service.ContactEmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactEmailService contactEmailService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendContactMessage(@Valid @RequestBody ContactMessageRequest request) {
        contactEmailService.sendContactEmail(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contact message sent successfully"
        ));
    }
}
