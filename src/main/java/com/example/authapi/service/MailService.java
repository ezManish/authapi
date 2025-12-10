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
        // Force manual configuration to bypass application.properties issues
        if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl) {
            org.springframework.mail.javamail.JavaMailSenderImpl senderImpl = (org.springframework.mail.javamail.JavaMailSenderImpl) mailSender;
            senderImpl.setHost("smtp.gmail.com");
            senderImpl.setPort(465);
            senderImpl.setProtocol("smtp");

            java.util.Properties props = senderImpl.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true"); // Enable SSL for port 465
            props.put("mail.smtp.starttls.enable", "false"); // Disable STARTTLS for port 465
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // Explicit socket factory
            props.put("mail.smtp.connectiontimeout", "45000");
            props.put("mail.smtp.timeout", "45000");
            props.put("mail.smtp.writetimeout", "45000");

            System.out.println(
                    "DEBUG: Forcing Mail Config: " + senderImpl.getHost() + ":" + senderImpl.getPort() + " (SSL)");
        }

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
