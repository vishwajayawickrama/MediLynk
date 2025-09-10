package com.medilynk.patientservice.controllers;

import com.medilynk.patientservice.dto.PatientRequest;
import com.medilynk.patientservice.dto.PatientResponse;
import com.medilynk.patientservice.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@RestController
@RequestMapping("api/patient")
@RequiredArgsConstructor
public class PatientController {
        private final PatientService patientService;

        @GetMapping
        @ResponseStatus(HttpStatus.OK)
        public PatientResponse getPatient(@RequestParam String id) {
                return patientService.getPatientById(id);
        }

        @PutMapping
        @ResponseStatus(HttpStatus.OK)
        public PatientResponse updatePatient(@RequestParam String id,@RequestBody PatientRequest patientRequest) {
            return patientService.updatePatient(id,patientRequest);
        }

        @DeleteMapping
        @ResponseStatus(HttpStatus.OK)
        public void deletePatient(@RequestParam String id) {
            patientService.deletePatient(id);
        }



}
