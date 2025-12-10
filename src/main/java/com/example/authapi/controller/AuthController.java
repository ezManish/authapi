package com.example.authapi.controller;

import com.example.authapi.model.User;
import com.example.authapi.repository.UserRepository;
import com.example.authapi.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 🔹 SIGNUP - Create new user
    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return "Email already exists!";
        }

        // Encrypt password before saving
        user.setPassword(encoder.encode(user.getPassword()));
        user.setProvider("local");
        userRepository.save(user);

        // Generate JWT after signup
        return jwtUtil.generateToken(user.getEmail());
    }

    // 🔹 LOGIN - Email + Password
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent() &&
                encoder.matches(user.getPassword(), existingUser.get().getPassword())) {

            return jwtUtil.generateToken(user.getEmail());
        }
        return "Invalid email or password!";
    }

    // 🔹 GOOGLE LOGIN / SIGNUP
    @PostMapping("/google")
    public String googleLogin(@RequestBody String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList("241062799395-kms9mcpd3kh7410njnpubhoott8o9lkf.apps.googleusercontent.com")) // replace this
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isEmpty()) {
                userRepository.save(new User(email, "", name, "google"));
            }

            return jwtUtil.generateToken(email);
        } else {
            return "Invalid Google token";
        }
    }
}
