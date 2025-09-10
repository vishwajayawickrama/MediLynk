package com.medilynk.patientservice.services;
import com.medilynk.patientservice.dto.PatientRequest;
import com.medilynk.patientservice.dto.PatientResponse;
import com.medilynk.patientservice.enums.PatientStatus;
import com.medilynk.patientservice.models.Patient;
import com.medilynk.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientResponse createPatient(PatientRequest patientRequest) {
        Patient patient = Patient.builder()
                .firstname(patientRequest.firstName())
                .lastname(patientRequest.lastName())
                .email(patientRequest.email())
                .phone(patientRequest.phone())
                .address(patientRequest.address())
                .age(patientRequest.age())
                .status(patientRequest.status() != null ? patientRequest.status() : PatientStatus.ACTIVE)
                .build();
        patientRepository.save(patient);

        log.info("Created patient with id {}", patient.getId());

        return new PatientResponse(
                patient.getId(),
                patient.getFirstname(),
                patient.getLastname(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getAge(),
                patient.getStatus());
    }

    public List<PatientResponse> getAllPatiens() {
        return patientRepository.findAll()
                .stream()
                .map(patient -> new PatientResponse(
                        patient.getId(),
                        patient.getFirstname(),
                        patient.getLastname(),
                        patient.getEmail(),
                        patient.getPhone(),
                        patient.getAddress(),
                        patient.getAge(),
                        patient.getStatus()))
                .toList();
    }
}
