package com.smartretail.backend.service;

import com.smartretail.backend.dto.LoginRequest;
import com.smartretail.backend.dto.LoginResponse;
import com.smartretail.backend.dto.SignupRequest;
import com.smartretail.backend.models.User;
import com.smartretail.backend.repository.UserRepository;
import com.smartretail.backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User signup(SignupRequest request) {
        System.out.println("[AUTH] Signing up user: " + request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("[AUTH] Signup failed: Email already exists: " + request.getEmail());
            throw new RuntimeException("Email already exists");
        }
        String userId = "u" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User(
                userId,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getRole().toUpperCase()
        );
        User savedUser = userRepository.save(user);
        System.out.println("[AUTH] User signed up successfully: " + savedUser.getEmail());
        return savedUser;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        System.out.println("[AUTH] Logging in user: " + request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.out.println("[AUTH] Login failed: User not found: " + request.getEmail());
                    return new RuntimeException("User not found");
                });
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("[AUTH] Login failed: Invalid password for: " + request.getEmail());
            throw new RuntimeException("Invalid password");
        }
        user.setLastLogin(new Date());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        System.out.println("[AUTH] Login successful for: " + user.getEmail());
        return new LoginResponse(token, user.getUserId(), user.getName(), user.getRole());
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}