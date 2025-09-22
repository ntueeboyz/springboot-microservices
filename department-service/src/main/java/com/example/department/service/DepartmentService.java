package com.example.department.service;

import com.example.department.client.EmployeeClient;
import com.example.department.domain.Department;
import com.example.department.dto.*;
import com.example.department.repo.DepartmentRepository;
import com.example.department.repo.DepartmentSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;
    private final EmployeeClient employeeClient;

    // List with pagination/sort/filters
    public Page<DepartmentDTO> list(int page, int size, Sort sort, String nameContains, String code) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Department> spec = Specification.where(DepartmentSpecifications.nameContains(nameContains))
                .and(DepartmentSpecifications.codeEquals(code));
        return repository.findAll(spec, pageable).map(this::toDTO);
    }

    public DepartmentDTO getById(Long id) {
        Department d = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Department not found"));
        return toDTO(d);
    }

    public DepartmentDTO getByCode(String code) {
        Department d = repository.findByCode(code).orElseThrow(() -> new EntityNotFoundException("Department not found"));
        return toDTO(d);
    }

    @Transactional
    public DepartmentDTO create(DepartmentDTO dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Department code must be unique");
        }
        Department d = Department.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .managerEmail(dto.getManagerEmail())
                .description(dto.getDescription())
                .build();
        return toDTO(repository.save(d));
    }

    @Transactional
    public DepartmentDTO put(Long id, UpdateDepartmentRequest req) {
        Department d = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Department not found"));
        if (repository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new IllegalArgumentException("Department code must be unique");
        }
        d.setName(req.getName());
        d.setCode(req.getCode());
        d.setManagerEmail(req.getManagerEmail());
        d.setDescription(req.getDescription());
        return toDTO(d);
    }

    @Transactional
    public DepartmentDTO patch(Long id, PatchDepartmentRequest req) {
        Department d = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Department not found"));
        if (req.getCode() != null && repository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new IllegalArgumentException("Department code must be unique");
        }
        if (req.getName() != null) d.setName(req.getName());
        if (req.getCode() != null) d.setCode(req.getCode());
        if (req.getManagerEmail() != null) d.setManagerEmail(req.getManagerEmail());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        return toDTO(d);
    }

    @Transactional
    public void deleteProtective(Long id) {
        Department d = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Department not found"));

        // Protective check: ask Employee service if any employees reference this department
        try {
            Map<String, Object> page = employeeClient.listByDepartment(0, 1, id);
            // If content array has at least 1, block the delete
            Object content = page.get("content");
            if (content instanceof List<?> list && !list.isEmpty()) {
                throw new IllegalArgumentException("Department is in use by employees; reassign or remove employees first");
            }
        } catch (Exception e) {
            // Downstream failure: be conservative and block delete with guidance
            throw new IllegalArgumentException("Cannot verify department usage at this time; try again later");
        }

        repository.delete(d);
    }

    // Composition: return employees under this department (paged)
    public Map<String, Object> employeesOf(Long id, int page, int size) {
        // validate department exists
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Department not found");
        }
        try {
            return employeeClient.listPaged(page, size, id);
        } catch (Exception e) {
            // graceful: stable error shape for composition
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("content", List.of());
            fallback.put("page", page);
            fallback.put("size", size);
            fallback.put("downstream", "EMPLOYEE-SERVICE unavailable");
            return fallback;
        }
    }

    private DepartmentDTO toDTO(Department d) {
        return DepartmentDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .managerEmail(d.getManagerEmail())
                .description(d.getDescription())
                .build();
    }
}
