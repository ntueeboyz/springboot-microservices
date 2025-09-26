package com.example.department.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EmployeeClientFallback implements EmployeeClient {

    private Throwable cause;

    EmployeeClientFallback() {}

    EmployeeClientFallback(Throwable cause) { this.cause = cause; }

    @Override
    public Map<String, Object> listByDepartment(int page, int size, Long departmentId) {
        log.warn("EmployeeClient fallback listByDepartment(depId={}): {}", departmentId, cause);
        return Map.of("content", List.of(), "page", page, "size", size, "fallback", true);
    }

    @Override
    public Map<String, Object> listPaged(int page, int size, Long departmentId) {
        log.warn("EmployeeClient fallback listPaged(depId={}): {}", departmentId, cause);
        return Map.of("content", List.of(), "page", page, "size", size, "downstream", "EMPLOYEE-SERVICE unavailable", "fallback", true);
    }

    @Component
    @RequiredArgsConstructor
    public static class Factory implements FallbackFactory<EmployeeClient> {
        @Override
        public EmployeeClient create(Throwable cause) {
            return new EmployeeClientFallback(cause);
        }
    }
}
