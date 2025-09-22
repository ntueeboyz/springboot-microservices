package com.example.department.repo;

import com.example.department.domain.Department;
import org.springframework.data.jpa.domain.Specification;

public final class DepartmentSpecifications {
    private DepartmentSpecifications() {}

    public static Specification<Department> nameContains(String term) {
        return (root, q, cb) -> term == null ? null :
                cb.like(cb.lower(root.get("name")), "%" + term.toLowerCase() + "%");
    }

    public static Specification<Department> codeEquals(String code) {
        return (root, q, cb) -> code == null ? null :
                cb.equal(cb.lower(root.get("code")), code.toLowerCase());
    }
}
