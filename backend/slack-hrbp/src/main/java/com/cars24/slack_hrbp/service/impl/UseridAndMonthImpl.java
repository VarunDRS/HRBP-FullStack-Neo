package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
import com.cars24.slack_hrbp.service.UseridAndMonth;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
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

