package com.example.employee.client;

import com.example.employee.dto.DepartmentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
@Component
public class DepartmentClientFallback implements DepartmentClient {

    private Throwable cause;

    DepartmentClientFallback() {}

    DepartmentClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public DepartmentDTO getDepartment(Long id) {
        String msg = (cause != null) ? cause.toString() : "unknown";
        log.warn("DepartmentClient fallback for id={} due to {}", id, msg);
        return null;
    }

    @Component
    @RequiredArgsConstructor
    public static class Factory implements FallbackFactory<DepartmentClient> {
        @Override
        public DepartmentClient create(Throwable cause) {
            return new DepartmentClientFallback(cause);
        }
    }
}