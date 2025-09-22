package com.example.employee.web;

import com.example.employee.dto.*;
import com.example.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Employees")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    // GET /employees — pagination/sort/filters
    @Operation(summary = "List employees with pagination, sorting and filters")
    @GetMapping
    public PageResponse<EmployeeDTO> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort,
            @RequestParam(required = false) String email,
            @RequestParam(required = false, name = "lastName") String lastNameContains,
            @RequestParam(required = false) Long departmentId
    ) {
        Sort sortObj = parseSort(sort);
        var result = service.list(page, size, sortObj, email, lastNameContains, departmentId);
        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    // GET /employees/{id}
    @Operation(summary = "Get employee by id (enriched with department if available)")
    @GetMapping("/{id}")
    public EmployeeDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }

    // POST /employees with optional Idempotency-Key
    @Operation(summary = "Create employee (optional Idempotency-Key header)")
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody EmployeeDTO dto) {
        var created = service.create(dto, idempotencyKey);
        // If this was an idempotent replay, we *may* return 200; for simplicity, always 201 is acceptable.
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /employees/{id}
    @Operation(summary = "Full update an employee")
    @PutMapping("/{id}")
    public EmployeeDTO put(@PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest req) {
        return service.put(id, req);
    }

    // PATCH /employees/{id}
    @Operation(summary = "Partial update an employee")
    @PatchMapping("/{id}")
    public EmployeeDTO patch(@PathVariable Long id, @Valid @RequestBody PatchEmployeeRequest req) {
        return service.patch(id, req);
    }

    // DELETE /employees/{id}
    @Operation(summary = "Delete employee")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // GET /employees/search?q=...
    @Operation(summary = "Search employees by name/email (case-insensitive)")
    @GetMapping("/search")
    public List<EmployeeDTO> search(@RequestParam("q") String query) {
        return service.search(query);
    }

    // GET /employees/stats
    @Operation(summary = "Basic stats (counts by departmentId)")
    @GetMapping("/stats")
    public List<DeptCountDTO> stats() {
        return service.stats();
    }

    // POST /employees:bulkCreate
    @Operation(summary = "Bulk create employees (up to N)")
    @PostMapping("/bulkCreate")
    public ResponseEntity<Map<String, Object>> bulkCreate(
            @RequestParam(defaultValue = "100") int max,
            @Valid @RequestBody List<EmployeeDTO> payload) {

        int limit = Math.min(max, 1000); // hard upper bound
        if (payload.size() > limit) {
            throw new IllegalArgumentException("Too many items; max=" + limit);
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (EmployeeDTO dto : payload) {
            Map<String, Object> r = new HashMap<>();
            try {
                var created = service.create(dto, null);
                r.put("status", "CREATED");
                r.put("employee", created);
            } catch (Exception ex) {
                r.put("status", "ERROR");
                r.put("error", ex.getMessage());
            }
            results.add(r);
        }
        Map<String, Object> body = Map.of(
                "count", results.size(),
                "results", results
        );
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(body);
    }

    private Sort parseSort(String sort) {
        // format: field,asc|desc[;field2,asc]
        String[] parts = sort.split("[;]");
        List<Sort.Order> orders = new ArrayList<>();
        for (String p : parts) {
            String[] kv = p.split(",", 2);
            String field = kv[0].trim();
            String dir = kv.length > 1 ? kv[1].trim().toLowerCase() : "asc";
            orders.add(new Sort.Order("desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, field));
        }
        return Sort.by(orders);
    }
}
