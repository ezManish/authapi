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
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body)
            throws MessagingException {

        String email = body.get("email");
        Map<String, String> response = new HashMap<>();

        // UserRepository should have: Optional<User> findByEmail(String email);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            response.put("error", "User not found");
            return response;
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

        // Send email
        mailService.sendResetLink(email, resetLink);

        response.put("message", "Password reset link sent successfully");
        return response;
    }
}
