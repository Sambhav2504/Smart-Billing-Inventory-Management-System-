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

@Service
public class AuthServiceImpl implements AuthService {
    private final Map<String, User> userStore = new HashMap<>();
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User signup(SignupRequest request) {
        System.out.println("[AUTH] Signing up user: " + request.getEmail());
        String userId = "u" + request.getEmail().hashCode();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(userId, request.getEmail(), encodedPassword, request.getName(), request.getRole());
        userStore.put(request.getEmail(), user);
        System.out.println("[AUTH] User signed up successfully: " + request.getEmail());
        return user;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        System.out.println("[AUTH] Logging in user: " + request.getEmail());
        User user = userStore.get(request.getEmail());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("[AUTH] Login successful for: " + request.getEmail());
            user.setLastLogin(new Date());
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return new LoginResponse(token, user.getUserId(), user.getName(), user.getRole());
        }
        System.out.println("[AUTH] Login failed for: " + request.getEmail());
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userStore.get(email);
    }
}