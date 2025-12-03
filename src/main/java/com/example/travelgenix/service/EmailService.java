package com.example.travelgenix.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.web.cors.allowed-origins}")
    private String frontendUrl;

    public void sendResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("TripGenix Admin Password Reset Request");
        message.setText("To reset your password, click the link below:\n\n"
                + resetLink
                + "\n\nThis link will expire in 1 hour.");

        mailSender.send(message);
    }
}