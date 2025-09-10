package com.medilynk.patientservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.medilynk.patientservice.enums.PatientStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "Patient")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Patient {
    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String dob;
    private String email;
    private String phone;
    private String address;
    private int age;
    private PatientStatus status;
}
