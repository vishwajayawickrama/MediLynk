package com.medilynk.authservice.service;

import com.medilynk.authservice.dto.LoginRequestDTO;
import com.medilynk.authservice.model.User;
import com.medilynk.authservice.util.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtils = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {

        return userService. findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
        .map(u -> jwtUtils.generateToken(u.getEmail(), u.getRole()));
    }

    public Boolean validateToken(String token) {
        try {
            jwtUtils.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
