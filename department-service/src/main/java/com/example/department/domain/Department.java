package com.example.department.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments", schema = "department")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(length = 200)
    private String managerEmail;

    @Column(columnDefinition = "text")
    private String description;
}
