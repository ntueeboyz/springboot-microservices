package com.example.department.web;

import com.example.department.dto.*;
import com.example.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Departments")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    @Operation(summary = "List departments with pagination, sorting, and filters")
    @GetMapping
    public PageResponse<DepartmentDTO> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false, name = "name") String nameContains,
            @RequestParam(required = false) String code
    ) {
        Sort sortObj = parseSort(sort);
        var p = service.list(page, size, sortObj, nameContains, code);
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }

    @Operation(summary = "Get department by id")
    @GetMapping("/{id}")
    public DepartmentDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Create department (code must be unique)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentDTO create(@Valid @RequestBody DepartmentDTO dto) {
        return service.create(dto);
    }

    @Operation(summary = "Full update (code remains unique)")
    @PutMapping("/{id}")
    public DepartmentDTO put(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest req) {
        return service.put(id, req);
    }

    @Operation(summary = "Partial update")
    @PatchMapping("/{id}")
    public DepartmentDTO patch(@PathVariable Long id, @Valid @RequestBody PatchDepartmentRequest req) {
        return service.patch(id, req);
    }

    @Operation(summary = "Protective delete; returns 409 if employees reference this department")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteProtective(id);
    }

    @Operation(summary = "Lookup department by code")
    @GetMapping("/by-code/{code}")
    public DepartmentDTO byCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @Operation(summary = "Employees in the department (composed from Employee service)")
    @GetMapping("/{id}/employees")
    public Map<String, Object> employees(@PathVariable Long id,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return service.employeesOf(id, page, size);
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split("[;]");
        Sort sortObj = Sort.unsorted();
        for (String p : parts) {
            String[] kv = p.split(",", 2);
            String field = kv[0].trim();
            String dir = kv.length > 1 ? kv[1].trim().toLowerCase() : "asc";
            Sort.Order order = new Sort.Order("desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
            sortObj = sortObj.and(Sort.by(order));
        }
        return sortObj;
    }
}
