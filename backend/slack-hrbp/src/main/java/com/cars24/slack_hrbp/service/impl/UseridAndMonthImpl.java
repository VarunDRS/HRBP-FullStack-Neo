package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
import com.cars24.slack_hrbp.service.UseridAndMonth;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UseridAndMonthImpl implements UseridAndMonth {

    private final UseridAndMonthDaoImpl useridAndMonthDao;

    @Override
    public Map<String, Map<String, String>> getCustomerDetails(String userId) {

        Map<String, Map<String, String>> resp = useridAndMonthDao.getUserDetails(userId);
        return resp;

    }

    @Override
    public Map<String, Map<String, String>> getCustomerDetails(String userId, String month) {
        Map<String, Map<String, String>> resp = useridAndMonthDao.getUserDetails(userId,month);
        return resp;
    }

    @Override
    public byte[] generateAttendanceExcel(String userid, String frommonth, String tomonth) throws IOException {
        Map<String, Map<String, String>> attendanceData = useridAndMonthDao.getUserDetails(userid, frommonth, tomonth);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Leave Report");

            // 1. Get all months in range
            List<String> monthYears = getAllMonthsInRange(frommonth, tomonth);

            // 2. Generate all dates for each month
            Map<String, List<String>> monthDatesMap = new LinkedHashMap<>();
            for (String monthYear : monthYears) {
                monthDatesMap.put(monthYear, getAllDatesForMonth(monthYear));
            }

            // 3. Create Header Row
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            for (String monthYear : monthYears) {
                headerRow.createCell(colIndex).setCellValue(monthYear + " (Leave Date)");
                headerRow.createCell(colIndex + 1).setCellValue(monthYear + " (Leave Type)");
                colIndex += 2;
            }

            // 4. Fill Data Rows
            int maxRows = monthDatesMap.values().stream().mapToInt(List::size).max().orElse(0);
            for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                int col = 0;

                for (String monthYear : monthYears) {
                    List<String> allDates = monthDatesMap.get(monthYear);
                    if (rowIndex <= allDates.size()) {
                        String date = allDates.get(rowIndex - 1);
                        row.createCell(col).setCellValue(date); // Leave Date
                        row.createCell(col + 1).setCellValue(attendanceData.getOrDefault(monthYear, new HashMap<>()).getOrDefault(date, "")); // Leave Type
                    }
                    col += 2;
                }
            }

            // Auto-size columns
            for (int i = 0; i < colIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Helper: Get all months between frommonth and tomonth
    private List<String> getAllMonthsInRange(String fromMonth, String toMonth) {
        List<String> months = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy");

        YearMonth start = YearMonth.parse(fromMonth, formatter);
        YearMonth end = YearMonth.parse(toMonth, formatter);

        while (!start.isAfter(end)) {
            months.add(start.format(formatter));
            start = start.plusMonths(1);
        }
        return months;
    }

    // Helper: Get all dates in a given month
    private List<String> getAllDatesForMonth(String monthYear) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy");
        YearMonth yearMonth = YearMonth.parse(monthYear, formatter);

        List<String> dates = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            dates.add(LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day).toString());
        }
        return dates;
    }


    @Override
    public byte[] generateAttendanceExcel(String userid, String month) throws IOException {
        Map<String, Map<String, String>> attendanceData = useridAndMonthDao.getUserDetails(userid, month);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Attendance");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Request Type");
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM-yyyy"); // Input month format

            Date monthDate;
            try {
                monthDate = inputFormat.parse(month);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid month format, expected MMM-yyyy", e);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(monthDate);
            int year = calendar.get(Calendar.YEAR);
            int monthIndex = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
            // Get total days in the month
            YearMonth yearMonth = YearMonth.of(year, monthIndex);
            //2025-02
            int totalDays = yearMonth.lengthOfMonth();

            Map<String, String> userAttendance = attendanceData.values().iterator().next();

            // Populate Data with all dates
            int rowNum = 1;
            for (int day = 1; day <= totalDays; day++) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                String formattedDate = dateFormat.format(calendar.getTime());

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(formattedDate); // Date

                // Fetch attendance status, default to blank
                row.createCell(1).setCellValue(userAttendance.getOrDefault(formattedDate, ""));
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }




}

