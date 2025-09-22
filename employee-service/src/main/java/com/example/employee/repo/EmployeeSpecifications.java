package com.example.employee.repo;

import com.example.employee.domain.Employee;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {
    private EmployeeSpecifications() {}

    public static Specification<Employee> emailEquals(String email) {
        return (root, q, cb) -> email == null ? null : cb.equal(cb.lower(root.get("email")), email.toLowerCase());
    }

    public static Specification<Employee> lastNameContains(String term) {
        return (root, q, cb) -> term == null ? null : cb.like(cb.lower(root.get("lastName")), "%" + term.toLowerCase() + "%");
    }

    public static Specification<Employee> departmentIdEquals(Long departmentId) {
        return (root, q, cb) -> departmentId == null ? null : cb.equal(root.get("departmentId"), departmentId);
    }
}
