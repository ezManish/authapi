package com.example.authapi.controller;

import com.example.authapi.model.User;
import com.example.authapi.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class ResetPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/reset-password/{token}")
    public Map<String, String> resetPassword(@PathVariable String token,
            @RequestBody Map<String, String> body) {

        Map<String, String> response = new HashMap<>();

        try {
            // Fix: Use parserBuilder() and Keys.hmacShaKeyFor
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                response.put("error", "Invalid token");
                return response;
            }

            User user = optionalUser.get();
            String newPassword = body.get("newPassword");

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            response.put("message", "Password reset successful");
            return response;

        } catch (ExpiredJwtException e) {
            response.put("error", "Token expired");
        } catch (JwtException e) {
            response.put("error", "Invalid token");
        }

        return response;
    }
}
