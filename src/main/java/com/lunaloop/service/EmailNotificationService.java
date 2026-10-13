
package com.lunaloop.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestEmail(String recipient) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject("LunaLoop Email Test");
        message.setText(
            "Hello!\n\n" +
            "This is a test email from LunaLoop.\n" +
            "Your email notification service is configured.\n\n" +
            "Small Stitches • Big Happiness"
        );

        mailSender.send(message);
    }
}
