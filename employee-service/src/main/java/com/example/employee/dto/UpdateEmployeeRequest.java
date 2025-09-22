package com.example.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmployeeRequest {
    @NotBlank @Size(max = 120)
    private String firstName;

    @NotBlank @Size(max = 120)
    private String lastName;

    @NotBlank @Email @Size(max = 200)
    private String email;

    private Long departmentId;
}
