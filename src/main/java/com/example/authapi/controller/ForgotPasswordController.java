package com.example.authapi.controller;

import com.example.authapi.model.User;
import com.example.authapi.repository.UserRepository;
import com.example.authapi.service.MailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/forgot-password")
    public org.springframework.http.ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> body) {
        Map<String, String> response = new HashMap<>();
        try {
            String email = body.get("email");
            if (email == null || email.isEmpty()) {
                response.put("error", "Email is required");
                return org.springframework.http.ResponseEntity.badRequest().body(response);
            }

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                response.put("error", "User not found");
                return org.springframework.http.ResponseEntity.status(404).body(response);
            }

            // Create a short-lived JWT (15 minutes)
            String token = Jwts.builder()
                    .setSubject(email)
                    .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 mins
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                            jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                    .compact();

            // 🔴 IMPORTANT: change this to your real frontend URL
            String resetLink = "https://vittles-reset.vercel.app/reset-password/" + token;

            System.out.println(
                    "Attempting to send email to: " + email + " using host: " + System.getProperty("spring.mail.host")); // Debug
                                                                                                                         // Log

            // Send email
            mailService.sendResetLink(email, resetLink);

            response.put("message", "Password reset link sent successfully");
            return org.springframework.http.ResponseEntity.ok(response);

        } catch (MessagingException e) {
            e.printStackTrace();
            response.put("error", "Failed to send email: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal error: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(response);
        }
    }
}
