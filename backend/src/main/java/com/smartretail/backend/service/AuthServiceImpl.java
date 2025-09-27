package com.smartretail.backend.service;

import com.smartretail.backend.dto.LoginRequest;
import com.smartretail.backend.dto.LoginResponse;
import com.smartretail.backend.dto.SignupRequest;
import com.smartretail.backend.models.RefreshToken;
import com.smartretail.backend.models.User;
import com.smartretail.backend.repository.RefreshTokenRepository;
import com.smartretail.backend.repository.UserRepository;
import com.smartretail.backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public User signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hashed
        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        refreshTokenRepository.save(
                new RefreshToken(user.getId(), refreshToken,
                        new Date(System.currentTimeMillis() + jwtUtil.getExpiration() * 2))
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    public String refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.deleteById(storedToken.getId());
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + storedToken.getUserId()));

        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
}
