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

        // Extract usernames
        List<String> allUsernames = employees.stream()
                .map(EmployeeEntity::getUsername)
                .toList();

        // Manually paginate the usernames
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allUsernames.size());

        List<String> paginatedUsernames = allUsernames.subList(start, end);

        return new PageImpl<>(paginatedUsernames, pageable, allUsernames.size());
    }



    public List<AttendanceEntity> getAttendanceForEmployees(String monthYear, List<String> usernames) {
        return attendanceRepository.findByDateStartingWithAndUsernameIn(monthYear, usernames);
    }

    public Page<String> getPaginatedEmployees(String monthYear, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        // Fetch distinct usernames (pagination needs to be handled in-memory)
        List<String> allUsernames = attendanceRepository.findDistinctUsernamesByMonth(monthYear);

        // Manually paginate the usernames
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allUsernames.size());
        List<String> paginatedUsernames = allUsernames.subList(start, end);

        return new PageImpl<>(paginatedUsernames, pageable, allUsernames.size());
    }


}
