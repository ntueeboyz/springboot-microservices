package com.example.department.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {
    @NotBlank @Size(max = 120)
    private String name;

    @NotBlank @Size(max = 40)
    private String code;            // still unique

    @Email @Size(max = 200)
    private String managerEmail;

    private String description;
}
