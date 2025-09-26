package com.example.department.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "EMPLOYEE-SERVICE",
        path = "/api/v1/employees",
        contextId = "employeeClient",
        fallbackFactory = EmployeeClientFallback.Factory.class
)
public interface EmployeeClient {

    // We’ll reuse employee list endpoint with department filter for protective delete (size=1 is enough).
    @GetMapping
    Map<String, Object> listByDepartment(@RequestParam("page") int page,
                                         @RequestParam("size") int size,
                                         @RequestParam("departmentId") Long departmentId);

    // Convenience composition endpoint: re-use search or list
    @GetMapping
    Map<String, Object> listPaged(@RequestParam("page") int page,
                                  @RequestParam("size") int size,
                                  @RequestParam(value = "departmentId", required = false) Long departmentId);
}
