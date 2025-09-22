package com.example.employee.repo;

import com.example.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    // quick “search” across name/email (case-insensitive)
    List<Employee> findTop50ByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(
            String fn, String ln, String email);

    // stats by department
    interface DeptCount {
        Long getDepartmentId();
        Long getCount();
    }

    @Query(value = """
            SELECT department_id AS departmentId, COUNT(*) AS count
            FROM employee.employees
            GROUP BY department_id
            """, nativeQuery = true)
    List<DeptCount> countsByDepartment();
}
