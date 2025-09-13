package com.medilynk.authservice.service;

import com.medilynk.authservice.dto.LoginRequestDTO;
import com.medilynk.authservice.model.User;
import com.medilynk.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
