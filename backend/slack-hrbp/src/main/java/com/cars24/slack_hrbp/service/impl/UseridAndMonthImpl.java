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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

            // List to store and sort dates
            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>();

            // Retrieve and sort attendance entries
            for (Map.Entry<String, Map<String, String>> userEntry : attendanceData.entrySet()) {
                sortedEntries.addAll(userEntry.getValue().entrySet());
            }

            // Sort the list based on date
            sortedEntries.sort((entry1, entry2) -> {
                try {
                    Date date1 = dateFormat.parse(entry1.getKey());
                    Date date2 = dateFormat.parse(entry2.getKey());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            });
            // Populate Data
            int rowNum = 1;
            for (Map.Entry<String, String> entry : sortedEntries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey()); // Date
                row.createCell(1).setCellValue(entry.getValue()); // Request Type
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }


}

