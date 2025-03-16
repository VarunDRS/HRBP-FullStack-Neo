package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthBasedDaoImpl {

    private final EmployeeRepository employeeRepository;

    public List<String> getAllEmployeesUnderManager(String managerId) {
        List<EmployeeEntity> employees = employeeRepository.findByManagerId(managerId); // Assuming this returns List<EmployeeEntity>

        return employees.stream()
                .map(EmployeeEntity::getUserId)
                .collect(Collectors.toList());
    }
}
