package com.medilynk.patientservice.controllers;

import lombok.RequiredArgsConstructor;
import com.medilynk.patientservice.dto.PatientRequest;
import com.medilynk.patientservice.dto.PatientResponse;
import com.medilynk.patientservice.models.Patient;
import com.medilynk.patientservice.services.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/patients")
@RequiredArgsConstructor
public class PatientController {
     private final PatientService patientService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse CreatePatient(@RequestBody PatientRequest patientRequest) {
        return patientService.createPatient(patientRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PatientResponse> getPatient() {
        return patientService.getAllPatiens();
    }
}
