package com.example.department.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchDepartmentRequest {
    @Size(max = 120)
    private String name;

    @Size(max = 40)
    private String code;            // if provided, must remain unique

    @Email @Size(max = 200)
    private String managerEmail;

    private String description;
}
