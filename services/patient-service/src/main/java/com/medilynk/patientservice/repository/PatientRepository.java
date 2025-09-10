package com.medilynk.patientservice.repository;

import com.medilynk.patientservice.models.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatientRepository extends MongoRepository<Patient, String> {
}
