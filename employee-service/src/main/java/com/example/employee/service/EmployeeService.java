package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.DepartmentDTO;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.repo.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.example.employee.domain.IdempotencyRecord;
import com.example.employee.repo.IdempotencyRecordRepository;
import com.example.employee.repo.EmployeeSpecifications;
import com.example.employee.dto.UpdateEmployeeRequest;
import com.example.employee.dto.PatchEmployeeRequest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.DigestUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentClient departmentClient;
    private final IdempotencyRecordRepository idemRepo;

    public List<EmployeeDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<EmployeeDTO> list(Integer page, Integer size, Sort sort,
                                  String email, String lastNameContains, Long departmentId) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Employee> spec = Specification.where(EmployeeSpecifications.emailEquals(email))
                .and(EmployeeSpecifications.lastNameContains(lastNameContains))
                .and(EmployeeSpecifications.departmentIdEquals(departmentId));

        Page<Employee> p = repository.findAll(spec, pageable);
        return p.map(this::toDTO);
    }

    public EmployeeDTO getById(Long id) {
        Employee e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        return toDTO(e);
    }

    @Transactional
    public EmployeeDTO create(EmployeeDTO dto, String idempotencyKey) {
        // Enforce email unique
        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Idempotency (optional)
        String reqHash = DigestUtils.md5DigestAsHex((dto.getFirstName() + "|" + dto.getLastName() + "|" + dto.getEmail() + "|" + dto.getDepartmentId()).getBytes());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idemRepo.findByKeyValue(idempotencyKey).orElse(null);
            if (existing != null) {
                if (existing.getRequestHash().equals(reqHash)) {
                    // Safe replay: return prior created resource if we have it
                    if (existing.getEmployeeId() != null) {
                        return getById(existing.getEmployeeId());
                    }
                    // fall-through; otherwise continue to create
                } else {
                    // Different request under same key -> conflict with guidance message
                    throw new IllegalArgumentException("Idempotency key reuse with different payload");
                }
            }
        }

        Employee e = Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .departmentId(dto.getDepartmentId())
                .build();
        e = repository.save(e);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotencyRecord rec = IdempotencyRecord.builder()
                    .keyValue(idempotencyKey)
                    .requestHash(reqHash)
                    .employeeId(e.getId())
                    .build();
            idemRepo.save(rec);
        }
        return toDTO(e);
    }

    @Transactional
    public EmployeeDTO put(Long id, UpdateEmployeeRequest req) {
        Employee e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (repository.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists");
        }
        e.setFirstName(req.getFirstName());
        e.setLastName(req.getLastName());
        e.setEmail(req.getEmail());
        e.setDepartmentId(req.getDepartmentId());
        return toDTO(e);
    }

    @Transactional
    public EmployeeDTO patch(Long id, PatchEmployeeRequest req) {
        Employee e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (req.getEmail() != null && repository.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (req.getFirstName() != null) e.setFirstName(req.getFirstName());
        if (req.getLastName() != null)  e.setLastName(req.getLastName());
        if (req.getEmail() != null)     e.setEmail(req.getEmail());
        if (req.getDepartmentId() != null) e.setDepartmentId(req.getDepartmentId());
        return toDTO(e);
    }

    @Transactional
    public void delete(Long id) {
        Employee e = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        repository.delete(e);
    }

    public List<EmployeeDTO> search(String q) {
        if (q == null || q.isBlank()) return List.of();
        return repository
                .findTop50ByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(q, q, q)
                .stream().map(this::toDTO).toList();
    }

    public List<com.example.employee.dto.DeptCountDTO> stats() {
        return repository.countsByDepartment().stream()
                .map(dc -> new com.example.employee.dto.DeptCountDTO(dc.getDepartmentId(), dc.getCount()))
                .toList();
    }

    private EmployeeDTO toDTO(Employee e) {
        DepartmentDTO dept = null;
        if (e.getDepartmentId() != null) {
            try { dept = departmentClient.getDepartment(e.getDepartmentId()); } catch (Exception ignored) { }
        }
        return EmployeeDTO.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .departmentId(e.getDepartmentId())
                .department(dept)
                .build();
    }
}
