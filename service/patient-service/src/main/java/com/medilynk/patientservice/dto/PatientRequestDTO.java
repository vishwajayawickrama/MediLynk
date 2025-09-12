package com.medilynk.patientservice.dto;

import com.medilynk.patientservice.dto.validator.CreatePatientValidationGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRequestDTO {
    @NotBlank
    @Size(max = 100, message = "Name can have at most 100 characters")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotBlank(message = "Date of Birth is mandatory")
    private String dateOfBirth;

    @NotBlank(groups = CreatePatientValidationGroup.class ,message = "Registered Date is mandatory") // Only mandatory during creation
    private String registeredDate;
    // Only mandatory during creation. This is enforced using validation groups.
    // Validation groups allow us to apply different validation rules in different contexts (e.g., creation vs. update).
    // Here, registeredDate is required when creating a new patient but can be optional during updates.
    // So create a marker interface CreatePatientValidationGroup and use it in the @NotBlank annotation.

}
