package com.example.employee.client;

import com.example.employee.dto.DepartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "DEPARTMENT-SERVICE",
        path = "/api/v1/departments",
        contextId = "departmentClient",              // bean name seen by Resilience4j
        fallbackFactory = DepartmentClientFallback.Factory.class
)
public interface DepartmentClient {

    @GetMapping("/{id}")
    DepartmentDTO getDepartment(@PathVariable("id") Long id);
}
