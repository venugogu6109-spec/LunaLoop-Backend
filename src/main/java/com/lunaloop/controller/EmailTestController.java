
package com.lunaloop.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lunaloop.backend.service.EmailNotificationService;

@RestController
public class EmailTestController {

    private final EmailNotificationService emailNotificationService;

    public EmailTestController(
            EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/api/email-test")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam String recipient) {
        emailNotificationService.sendTestEmail(recipient);
        return ResponseEntity.ok("Test email sent successfully.");
    }
}
