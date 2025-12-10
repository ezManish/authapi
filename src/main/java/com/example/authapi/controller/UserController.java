package com.example.authapi.controller;

import com.example.authapi.model.User;
import com.example.authapi.repository.UserRepository;
import com.example.authapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // 🔹 Simple test endpoint (NO JWT needed, just to verify controller works)
    @GetMapping("/ping")
    public Map<String, String> ping() {
        System.out.println("✅ /api/user/ping hit");
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    // 🔒 Protected route: returns USER ID directly from database
    @GetMapping("/me")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        System.out.println("➡️ /api/user/me called");

        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtUtil.extractEmail(token);
        System.out.println("📧 Email from token: " + email);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            System.out.println("❌ No user found for email " + email);
            throw new RuntimeException("User not found for email: " + email);
        }

        User user = optionalUser.get();
        System.out.println("✅ Found user with id " + user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());

        return response;
    }
}
