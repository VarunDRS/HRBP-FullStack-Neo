package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final AttendanceRepository attendanceRepository;

    public List<String> getAllEmployeesUnderManager(String managerId) {
        List<EmployeeEntity> employees = employeeRepository.findByManagerId(managerId); // Assuming this returns List<EmployeeEntity>

        return employees.stream()
                .map(EmployeeEntity::getUserId)
                .collect(Collectors.toList());
    }

    //clean

    public Page<String> getPaginatedEmployeesForManager(String monthYear, String managerId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        // Fetch employees under this manager
        List<EmployeeEntity> employees = employeeRepository.findByManagerId(managerId);
        System.out.println("getPaginatedEmployeesForManager: " + employees);

        // Extract userIds instead of usernames
        List<String> allUserIds = employees.stream()
                .map(EmployeeEntity::getUserId)
                .toList();

        // Manually paginate the userIds
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allUserIds.size());

        List<String> paginatedUserIds = allUserIds.subList(start, end);

        return new PageImpl<>(paginatedUserIds, pageable, allUserIds.size());
    }




    public List<AttendanceEntity> getAttendanceForEmployees(String monthYear, List<String> userIds) {
        return attendanceRepository.findByDateStartingWithAndUseridIn(monthYear, userIds); // Use userId
    }


    public Page<String> getPaginatedEmployees(String monthYear, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        // Directly fetch paginated employees from Neo4j
        Page<EmployeeEntity> employeePage = employeeRepository.findAll(pageable);

        List<String> employeeUserIds = employeePage.getContent()
                .stream()
                .map(EmployeeEntity::getUserId) // Extract user IDs
                .toList();

        return new PageImpl<>(employeeUserIds, pageable, employeePage.getTotalElements());
    }




}
