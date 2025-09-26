package com.example.employee.health; // or com.example.department.health

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerHealthIndicator(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        boolean anyOpen = false;

        for (CircuitBreaker cb : registry.getAllCircuitBreakers()) {
            var metrics = cb.getMetrics();
            details.put(cb.getName(), Map.of(
                    "state", cb.getState().name(),
                    "failureRate", metrics.getFailureRate(),
                    "slowCallRate", metrics.getSlowCallRate()
            ));
            if (cb.getState() == CircuitBreaker.State.OPEN) {
                anyOpen = true;
            }
        }

        return Health.status(anyOpen ? Status.DOWN : Status.UP)
                .withDetail("circuitBreakers", details)
                .build();
    }
}
