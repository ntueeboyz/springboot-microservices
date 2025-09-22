package com.example.employee.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys", schema = "employee",
        uniqueConstraints = @UniqueConstraint(name="uk_idem_key", columnNames = "key_value"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="key_value", nullable = false, length = 200, unique = true)
    private String keyValue;

    @Column(name="request_hash", nullable = false, length = 128)
    private String requestHash;

    @Column(name="employee_id")
    private Long employeeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
