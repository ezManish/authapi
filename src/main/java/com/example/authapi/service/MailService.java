package com.example.authapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @org.springframework.scheduling.annotation.Async
    public void sendResetLink(String to, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Password Reset - Vittles");

        String htmlContent = "<div style='font-family:sans-serif'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>Click the link below to reset your password:</p>" +
                "<a href='" + resetLink + "' target='_blank' " +
                "   style='background:#8B3358;color:#fff;" +
                "          padding:10px 16px;border-radius:6px;" +
                "          text-decoration:none'>Reset Password</a>" +
                "<p>This link expires in 15 minutes.</p>" +
                "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
