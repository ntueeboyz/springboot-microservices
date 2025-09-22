package com.example.department.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class DepartmentDTO {
    private Long id;

    @NotBlank @Size(max = 120)
    private String name;

    @NotBlank @Size(max = 40)
    private String code;            // unique short identifier

    @Email @Size(max = 200)
    private String managerEmail;

    private String description;
}
