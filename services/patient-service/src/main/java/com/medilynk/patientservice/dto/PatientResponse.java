package com.medilynk.patientservice.dto;

import com.medilynk.patientservice.enums.PatientStatus;

public record PatientResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        int age,
        PatientStatus status
) {
}
