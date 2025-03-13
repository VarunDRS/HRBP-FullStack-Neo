package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MonthBasedServiceImpl {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public Map<String, Map<String, String>> generateAttendanceReport(String monthYear) throws IOException, ParseException {
        // Fetch data for the given month and year
        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);

        // Create a map to store user-wise attendance data
        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();

        // Parse the date and populate the map
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        for (AttendanceEntity attendance : attendanceList) {

            String username = attendance.getUsername();
            String date = attendance.getDate();
            String requestType = getRequestTypeCode(attendance.getType());

            Date parsedDate = dateFormat.parse(date);
            String formattedDate = displayFormat.format(parsedDate);

            userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, requestType);

        }

        // Generate Excel file
        generateExcel(userAttendanceMap, monthYear);

        // Return the processed data
        return userAttendanceMap;
    }

    private String getRequestTypeCode(String requestType) {
        switch (requestType) {
            case "Planned Leave":
                return "P";
            case "Unplanned Leave":
                return "U";
            case "Planned Leave (Second Half)":
                return "P*";
            case "Sick Leave":
                return "S";
            case "Work From Home":
                return "W";
            case "Travelling to HQ":
                return "T";
            case "Holiday":
                return "H";
            case "Elections":
                return "E";
            case "Joined":
                return "J";
            case "Planned Leave (First Half)":
                return "P**";
            default:
                return "";
        }
    }

    private void generateExcel(Map<String, Map<String, String>> userAttendanceMap, String monthYear) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("User Name");

        // Get all unique dates
        Set<String> allDates = new TreeSet<>();
        for (Map<String, String> dateMap : userAttendanceMap.values()) {
            allDates.addAll(dateMap.keySet());
        }

        // Add dates to the header row
        int colNum = 1;
        for (String date : allDates) {
            headerRow.createCell(colNum++).setCellValue(date);
        }

        // Populate user data
        int rowNum = 1;
        for (Map.Entry<String, Map<String, String>> entry : userAttendanceMap.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());

            colNum = 1;
            for (String date : allDates) {
                String requestType = entry.getValue().getOrDefault(date, "");
                row.createCell(colNum++).setCellValue(requestType);
            }
        }

        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream("Attendance_Report_" + monthYear + ".xlsx")) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}