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
                .dob(patientRequest.dob())
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
                patient.getDob(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getAge(),
                patient.getStatus());
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(patient -> new PatientResponse(
                        patient.getId(),
                        patient.getFirstname(),
                        patient.getLastname(),
                        patient.getEmail(),
                        patient.getDob(),
                        patient.getPhone(),
                        patient.getAddress(),
                        patient.getAge(),
                        patient.getStatus()))
                .toList();
    }

    public PatientResponse getPatientById(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        return new PatientResponse(
                patient.getId(),
                patient.getFirstname(),
                patient.getLastname(),
                patient.getEmail(),
                patient.getDob(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getAge(),
                patient.getStatus());
    }

    public PatientResponse updatePatient(String id, PatientRequest patientRequest) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        patient.setFirstname(patientRequest.firstName());
        patient.setLastname(patientRequest.lastName());
        patient.setEmail(patientRequest.email());
        patient.setDob(patientRequest.dob());
        patient.setPhone(patientRequest.phone());
        patient.setAddress(patientRequest.address());
        patient.setAge(patientRequest.age());
        if (patientRequest.status() != null) {
            patient.setStatus(patientRequest.status());
        }

        patientRepository.save(patient);

        log.info("Updated patient with id {}", patient.getId());

        return new PatientResponse(
                patient.getId(),
                patient.getFirstname(),
                patient.getLastname(),
                patient.getEmail(),
                patient.getDob(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getAge(),
                patient.getStatus());
    }

    public void deletePatient(String id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
        log.info("Deleted patient with id {}", id);
    }
}
