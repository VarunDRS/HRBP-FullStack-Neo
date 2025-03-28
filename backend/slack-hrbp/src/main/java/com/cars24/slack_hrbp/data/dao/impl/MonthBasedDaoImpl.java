package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.neo4j.core.Neo4jClient;


@Service
@RequiredArgsConstructor
@Slf4j
public class MonthBasedDaoImpl {

    private final EmployeeRepository employeeRepository;
    private final Neo4jClient neo4jClient;
    private final AttendanceRepository attendanceRepository;

    public List<String> getAllEmployeesUnderManager(String managerId) {
        List<EmployeeEntity> employees = employeeRepository.findByManagerId(managerId); // Assuming this returns List<EmployeeEntity>

        return employees.stream()
                .map(EmployeeEntity::getUserId)
                .collect(Collectors.toList());
    }


    public Page<String> getPaginatedEmployeesForManager(String managerId, int page, int limit) {
        String query = "MATCH (:Employee {userId: $managerId})<-[:REPORTED_BY*]-(e:Employee) " +
                "RETURN e.userId SKIP $skip LIMIT $limit";

        var queryBuilder = neo4jClient.query(query)
                .bind(managerId).to("managerId")
                .bind(page * limit).to("skip")
                .bind(limit).to("limit");

        List<String> userIds = (List<String>) queryBuilder.fetchAs(String.class)
                .all();

        long totalEmployees = employeeRepository.countByManagerId(managerId); // Fetch total count separately for pagination

        Pageable pageable = PageRequest.of(page, limit);
        return new PageImpl<>(userIds, pageable, totalEmployees);
    }

    public Page<String> getPaginatedEmployeesForHr(int page, int limit) {
        String query = "MATCH (e:Employee) RETURN e.userId SKIP $skip LIMIT $limit";

        var queryBuilder = neo4jClient.query(query)
                .bind(page * limit).to("skip")
                .bind(limit).to("limit");

        List<String> userIds = (List<String>) queryBuilder.fetchAs(String.class).all();

        long totalEmployees = employeeRepository.count(); // Fetch total count separately for pagination

        Pageable pageable = PageRequest.of(page, limit);
        return new PageImpl<>(userIds, pageable, totalEmployees);
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


    public Page<EmployeeEntity> getEmployees(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return employeeRepository.findAll(pageable);
    }

    private String getRequestTypeCode(String requestType) {
        if (requestType == null) {
            return "";  // Or some default code
        }
        switch (requestType) {
            case "Planned Leave": return "P";
            case "Unplanned Leave":
            case "UnPlanned Leave": return "U";
            case "Planned Leave (Second Half)": return "P*";
            case "Sick Leave": return "S";
            case "Work From Home":
            case "WFH": return "W";
            case "Travelling to HQ": return "T";
            case "Holiday": return "H";
            case "Elections": return "E";
            case "Joined": return "J";
            case "Planned Leave (First Half)": return "P**";
            default: return "";
        }
    }


    public Map<String, Map<String, String>> generateAttendanceReport(String monthYear) throws IOException, ParseException
        {
            // Fetch data for the given month and year
            List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);


            // Create a map to store user-wise attendance data
            Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();

            // Parse the date and populate the map
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

            for (AttendanceEntity attendance : attendanceList) {
                String username = attendance.getUsername();

                // Skip records with null usernames
                if (username == null || username.isEmpty()) {
                    continue;
                }

                String date = attendance.getDate();
                String requestType = attendance.getType() != null ? getRequestTypeCode(attendance.getType()) : "";

                Date parsedDate = null;
                try {
                    parsedDate = dateFormat.parse(date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                String formattedDate = displayFormat.format(parsedDate);

                userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, requestType);
            }
            for (Map.Entry<String, Map<String, String>> entry : userAttendanceMap.entrySet()) {
                Map<String, String> userData = entry.getValue();
                int totalWFH = 0;
                int totalLeaves = 0;

                for (Map.Entry<String, String> dateEntry : userData.entrySet()) {
                    String type = dateEntry.getValue();
                    if (type.equals("W")) {
                        totalWFH++;
                    } else if (type.equals("P") || type.equals("U") || type.equals("S") || type.equals("P*") || type.equals("P**")) {
                        totalLeaves++;
                    }
                }

                // Add Total WFH and Total Leaves to the user's map
                userData.put("Total WFH", String.valueOf(totalWFH));
                userData.put("Total Leaves", String.valueOf(totalLeaves));
            }


            // Return the processed data
            return userAttendanceMap;
        }



}
