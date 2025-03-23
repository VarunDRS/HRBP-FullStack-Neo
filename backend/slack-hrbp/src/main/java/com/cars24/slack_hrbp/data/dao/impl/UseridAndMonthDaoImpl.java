package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.dao.UseridAndMonthDao;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class UseridAndMonthDaoImpl implements UseridAndMonthDao {

    private final AttendanceRepository attendanceRepository;

    @Override
    public Map<String, Map<String, String>> getUserDetails(String userid) {
        log.info("Fetching attendance details for userid: {}", userid);

        List<AttendanceEntity> resp = attendanceRepository.findByUserid(userid);

        if (resp.isEmpty()) {
            return Collections.emptyMap(); // Return an empty map if no data found
        }

        String username = resp.get(0).getUsername();
        Map<String, String> attendanceMap = new LinkedHashMap<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd"); // Input format
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM-dd"); // Output format (Feb-21)

        for (AttendanceEntity entity : resp) {
            String formattedDate = "";
            String dateStr = entity.getDate();

            if (dateStr == null || dateStr.trim().isEmpty()) {
                log.error("Date is null or empty for entity: {}", entity);
                continue; // Skip this entry
            }

            try {
                Date parsedDate = inputFormat.parse(dateStr);
                formattedDate = outputFormat.format(parsedDate);
            } catch (ParseException e) {
                log.error("Error parsing date: {}", dateStr, e);
                continue;
            }

            String leaveType = getLeaveAbbreviation(entity.getType());
            attendanceMap.put(formattedDate, leaveType);
        }


        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        result.put(username, attendanceMap);
        return result;
    }

    private String getLeaveAbbreviation(String leaveType) {
        return switch (leaveType) {
            case "Planned Leave" -> "P";
            case "Unplanned Leave" -> "U";
            case "Planned Leave (Second Half)" -> "P*";
            case "Sick Leave" -> "S";
            case "WFH" -> "W";
            case "Work From Home" -> "W";
            case "Travelling to HQ" -> "T";
            case "Holiday" -> "H";
            case "Elections" -> "E";
            case "Joined" -> "J";
            case "Planned Leave (First Half)" -> "P**";
            default -> "?";
        };

    }

    @Override
    public Map<String, Map<String, String>> getUserDetails(String userid, String frommonth, String tomonth) {
        log.info("Fetching attendance details for userid: {}", userid);

        List<AttendanceEntity> resp = attendanceRepository.findByUserid(userid);
        if (resp.isEmpty()) {
            return Collections.emptyMap(); // Return an empty map if no data found
        }

        String username = resp.get(0).getUsername();
        // Define formatters
        DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // DB date format
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMM-yyyy"); // Input month format

        // Convert frommonth and tomonth to YearMonth
        YearMonth fromMonth = YearMonth.parse(frommonth, monthYearFormatter);
        YearMonth toMonth = YearMonth.parse(tomonth, monthYearFormatter);

        // Filter attendance records based on the leave date range
        List<AttendanceEntity> filteredLeaves = resp.stream()
                .filter(attendance -> {
                    LocalDate leaveDate = LocalDate.parse(attendance.getDate(), dbDateFormatter);
                    YearMonth leaveYearMonth = YearMonth.from(leaveDate);
                    return (leaveYearMonth.equals(fromMonth) || leaveYearMonth.equals(toMonth) ||
                            (leaveYearMonth.isAfter(fromMonth) && leaveYearMonth.isBefore(toMonth)));
                })
                .collect(Collectors.toList());

        // Prepare the report data
        Map<String, Map<String, String>> reportData = new LinkedHashMap<>();

        for (AttendanceEntity leave : filteredLeaves) {
            LocalDate leaveDate = LocalDate.parse(leave.getDate(), dbDateFormatter);
            String monthYearKey = leaveDate.format(monthYearFormatter); // "MMM-yyyy" format for grouping

            reportData.putIfAbsent(monthYearKey, new LinkedHashMap<>()); // Initialize inner map
            reportData.get(monthYearKey).put(leave.getDate(), leave.getType()); // Store leave date & type
        }

        return reportData;
    }

    @Override
    public Map<String, Map<String, String>> getUserDetails(String userid, String month) {
        log.info("Fetching attendance details for userid: {}", userid);

        List<AttendanceEntity> resp = attendanceRepository.findByUserid(userid);
        if (resp.isEmpty()) {
            return Collections.emptyMap(); // Return an empty map if no data found
        }

        String username = resp.get(0).getUsername();
        Map<String, String> attendanceMap = new LinkedHashMap<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd"); // Input format from DB
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM-dd"); // Format (Feb-21)
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yyyy"); // Correct month-year format

        // Correctly parse the input month (e.g., "Feb-2025")
        Date parsedMonth;
        try {
            parsedMonth = monthFormat.parse(month);
            log.info("Parsed Month: {}", parsedMonth);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid month format. Expected format: MMM-yyyy (e.g., Feb-2025)", e);
        }

        String targetMonth = monthFormat.format(parsedMonth); // Convert Date back to "MMM-yyyy"

        for (AttendanceEntity entity : resp) {
            try {

                Date parsedDate = inputFormat.parse(entity.getDate()); // Convert DB date (yyyy-MM-dd) to Date object
                String entityMonth = monthFormat.format(parsedDate); // Convert Date to "MMM-yyyy"

                if (entityMonth.equals(targetMonth)) { // Correct month comparison
                    String formattedDate = outputFormat.format(parsedDate); // Format Date to "MMM-dd"
                    String leaveType = getLeaveAbbreviation(entity.getType());
                    attendanceMap.put(formattedDate, leaveType);
                }
            } catch (ParseException e) {
                log.error("Error parsing date: {}", entity.getDate(), e);
            }
        }

        // Construction of final response map
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        result.put(username, attendanceMap);
        return result;
    }


}
