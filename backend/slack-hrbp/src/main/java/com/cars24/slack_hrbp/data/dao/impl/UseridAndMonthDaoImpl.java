package com.cars24.slack_hrbp.data.dao.impl;

import com.cars24.slack_hrbp.data.dao.UseridAndMonthDao;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

            try {
                Date parsedDate = inputFormat.parse(entity.getDate()); // Convert String to Date
                formattedDate = outputFormat.format(parsedDate); // Format Date to "MMM-dd"
//                System.out.println(entity);
            } catch (ParseException e) {
                log.error("Error parsing date: {}", entity.getDate(), e);
                continue; // Skip this entry if date parsing fails
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
                    log.info("Inside If - Date belongs to requested month");

                    String formattedDate = outputFormat.format(parsedDate); // Format Date to "MMM-dd"
                    String leaveType = getLeaveAbbreviation(entity.getType());
                    attendanceMap.put(formattedDate, leaveType);
                }
            } catch (ParseException e) {
                log.error("Error parsing date: {}", entity.getDate(), e);
            }
        }

        // Construct final response map
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        result.put(username, attendanceMap);
        return result;
    }

}
