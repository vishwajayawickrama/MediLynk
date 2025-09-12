package com.medilynk.patientservice.controller;

import com.medilynk.patientservice.dto.PatientRequestDTO;
import com.medilynk.patientservice.dto.PatientResponseDTO;
import com.medilynk.patientservice.dto.validator.CreatePatientValidationGroup;
import com.medilynk.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController // Marks this class as a REST controller
@RequestMapping("/patients") // Base URL for all endpoints in this controller
@Tag(name = "Patient Management", description = "APIs for managing patients") // Swagger tag for grouping related endpoints
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieve a list of all patients") // Swagger operation description and summary
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        List<PatientResponseDTO> patients = patientService.getPatients();
        return ResponseEntity.ok().body(patients);
    }

    @PostMapping
    @Operation(summary = "Create a new patient", description = "Create a new patient with the provided details")
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) { // @Validated to trigger validation on the DTO fields and specify validation groups
        PatientResponseDTO createdPatient = patientService.createPatient(patientRequestDTO);
        return ResponseEntity.ok().body(createdPatient);
    }

    @PutMapping("/{id}") // ? Should we get id from path variable or request param
    @Operation(summary = "Update an existing patient", description = "Update the details of an existing patient by ID")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable UUID id,
            @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) { // @Validated to trigger validation on the DTO fields

        PatientResponseDTO updatedPatient = patientService.updatePatient(id, patientRequestDTO);
        return ResponseEntity.ok().body(updatedPatient);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient", description = "Delete an existing patient by ID")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
