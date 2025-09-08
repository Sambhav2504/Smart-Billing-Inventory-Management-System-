package com.smartretail.backend.service;

import com.smartretail.backend.dto.LoginRequest;
import com.smartretail.backend.dto.LoginResponse;
import com.smartretail.backend.dto.SignupRequest;
import com.smartretail.backend.models.User;
import com.smartretail.backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final Map<String, User> userStore = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User signup(SignupRequest request) {
        System.out.println("[AUTH] Signup attempt for email: " + request.getEmail());
        if (userStore.containsKey(request.getEmail())) {
            System.out.println("[AUTH] Signup failed: Email already exists: " + request.getEmail());
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setUserId("u" + UUID.randomUUID().toString().substring(0, 8));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());
        user.setCreatedAt(new Date());
        userStore.put(request.getEmail(), user);
        System.out.println("[AUTH] Signup successful for: " + request.getEmail());
        return user;
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("[AUTH] Login attempt for email: " + request.getEmail());
        User user = userStore.get(request.getEmail());
        if (user == null) {
            System.out.println("[AUTH] Login failed: User not found: " + request.getEmail());
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("[AUTH] Login failed: Invalid password for: " + request.getEmail());
            throw new RuntimeException("Invalid password");
        }
        user.setLastLogin(new Date());
        userStore.put(request.getEmail(), user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        System.out.println("[AUTH] Login successful for: " + request.getEmail());
        return new LoginResponse(token, user.getUserId(), user.getName(), user.getRole());
    }

    public User getUserByEmail(String email) {
        return userStore.get(email);
    }
}