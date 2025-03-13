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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class ListAllEmployeesUnderManagerDaoImpl{

    private final EmployeeRepository employeeRepository;

    public Page<List<String>> getAllEmployeesUnderManager(String userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        Page<EmployeeEntity> employeePage = employeeRepository.findByManagerId(userId, pageable);

        List<List<String>> employeeIds = employeePage.getContent().stream().map(bt -> {
            List<String> inside = new ArrayList<>();
            inside.add(bt.getUserId());
            inside.add(bt.getEmail());
            inside.add(bt.getUsername());
            return inside;
        }).collect(Collectors.toList());

        return new PageImpl<>(employeeIds, pageable, employeePage.getTotalElements());
    }

    public long getTotalEmployeesCount(String userId) {
        return employeeRepository.countByManagerId(userId);
    }


}
