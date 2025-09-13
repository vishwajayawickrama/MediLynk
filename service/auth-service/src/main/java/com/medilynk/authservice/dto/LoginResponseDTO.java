package com.medilynk.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
 // this creates a constructor with all arguments
@Getter
public class LoginResponseDTO {
    private String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
