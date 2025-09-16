package com.medilynk.authservice.service;

import com.medilynk.authservice.dto.LoginRequestDTO;
import com.medilynk.authservice.dto.RegisterRequestDTO;
import com.medilynk.authservice.model.User;
import com.medilynk.authservice.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
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

    public Optional<String> registerUser(RegisterRequestDTO registerRequestDTO) {
        if (userService.findByEmail(registerRequestDTO.getEmail()).isPresent()) {
            log.info("User with email {} already exists", registerRequestDTO.getEmail());
            return Optional.empty(); // User already exists
        }

        String encodedPassword = passwordEncoder.encode(registerRequestDTO.getPassword());

        User newUser = new User();
        newUser.setEmail(registerRequestDTO.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setRole(registerRequestDTO.getRole());
        User newSavedUser = userService.createNewUser(newUser);
        String token = jwtUtils.generateToken(newSavedUser.getEmail(), newSavedUser.getRole());
        return Optional.of(token);
    }
}
