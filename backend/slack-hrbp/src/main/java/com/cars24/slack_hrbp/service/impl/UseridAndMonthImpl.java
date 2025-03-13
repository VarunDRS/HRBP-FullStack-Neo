//package com.cars24.slack_hrbp.service.impl;
//
//import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
//import com.cars24.slack_hrbp.service.UseridAndMonth;
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Service
//@RequiredArgsConstructor
//public class UseridAndMonthImpl implements UseridAndMonth {
//
//    private final UseridAndMonthDaoImpl useridAndMonthDao;
//    private final Map<String, byte[]> reportStorage = new ConcurrentHashMap<>();
//
//    @Override
//    public Map<String, Map<String, String>> getCustomerDetails(String userId) {
//
//        Map<String, Map<String, String>> resp = useridAndMonthDao.getUserDetails(userId);
//        return resp;
//
//    }
//
//    @Override
//    public Map<String, Map<String, String>> getCustomerDetails(String userId, String month) {
//        Map<String, Map<String, String>> resp = useridAndMonthDao.getUserDetails(userId,month);
//        return resp;
//    }
//
////    @Override
////    public byte[] generateAttendanceExcel(String userid, String month) throws IOException {
////        Map<String, Map<String, String>> attendanceData = useridAndMonthDao.getUserDetails(userid, month);
////
////        try (Workbook workbook = new XSSFWorkbook();
////             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
////
////            Sheet sheet = workbook.createSheet("Attendance");
////
////            // Create Header Row
////            Row headerRow = sheet.createRow(0);
////            headerRow.createCell(0).setCellValue("Date");
////            headerRow.createCell(1).setCellValue("Request Type");
////            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
////
////            // List to store and sort dates
////            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>();
////
////            // Retrieve and sort attendance entries
////            for (Map.Entry<String, Map<String, String>> userEntry : attendanceData.entrySet()) {
////                sortedEntries.addAll(userEntry.getValue().entrySet());
////            }
////
////            // Sort the list based on date
////            sortedEntries.sort((entry1, entry2) -> {
////                try {
////                    Date date1 = dateFormat.parse(entry1.getKey());
////                    Date date2 = dateFormat.parse(entry2.getKey());
////                    return date1.compareTo(date2);
////                } catch (ParseException e) {
////                    e.printStackTrace();
////                    return 0;
////                }
////            });
////            // Populate Data
////            int rowNum = 1;
////            for (Map.Entry<String, String> entry : sortedEntries) {
////                Row row = sheet.createRow(rowNum++);
////                row.createCell(0).setCellValue(entry.getKey()); // Date
////                row.createCell(1).setCellValue(entry.getValue()); // Request Type
////            }
////
////            workbook.write(outputStream);
////            return outputStream.toByteArray();
////        }
////    }
//    public SseEmitter generateAttendanceExcelWithSSE(String userid, String month) {
//        SseEmitter emitter = new SseEmitter(0L); // No timeout
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//
//        executor.execute(() -> {
//            try {
//                emitter.send("Processing started...", MediaType.TEXT_PLAIN);
//
//                byte[] excelData = generateAttendanceExcel(userid, month);
//
//                // Store report in memory
//                String key = userid + "_" + month;
//                reportStorage.put(key, excelData);
//
//                // Send download link
//                emitter.send("Processing complete. Download URL: /hr/download/file/" + key, MediaType.TEXT_PLAIN);
//                emitter.complete();
//            } catch (Exception e) {
//                try {
//                    emitter.send("Error: Report generation failed.", MediaType.TEXT_PLAIN);
//                } catch (IOException ignored) {}
//                emitter.completeWithError(e);
//            }
//        });
//
//        executor.shutdown();
//        return emitter;
//    }
//
//    public byte[] generateAttendanceExcel(String userId, String month) throws IOException {
//        Map<String, Map<String, String>> attendanceData = useridAndMonthDao.getUserDetails(userId, month);
//
//        try (Workbook workbook = new XSSFWorkbook();
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//            Sheet sheet = workbook.createSheet("Attendance");
//
//            // Create Header Row
//            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Date");
//            headerRow.createCell(1).setCellValue("Request Type");
//
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
//            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>();
//
//            // Retrieve and sort attendance entries
//            for (Map.Entry<String, Map<String, String>> userEntry : attendanceData.entrySet()) {
//                sortedEntries.addAll(userEntry.getValue().entrySet());
//            }
//
//            sortedEntries.sort((entry1, entry2) -> {
//                try {
//                    Date date1 = dateFormat.parse(entry1.getKey());
//                    Date date2 = dateFormat.parse(entry2.getKey());
//                    return date1.compareTo(date2);
//                } catch (ParseException e) {
//                    return 0;
//                }
//            });
//
//            // Populate data
//            int rowNum = 1;
//            for (Map.Entry<String, String> entry : sortedEntries) {
//                Row row = sheet.createRow(rowNum++);
//                row.createCell(0).setCellValue(entry.getKey());
//                row.createCell(1).setCellValue(entry.getValue());
//            }
//
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    public byte[] getReport(String key) {
//        return reportStorage.get(key);
//    }
//
//
//}

//package com.cars24.slack_hrbp.service.impl;
//
//import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
//import com.cars24.slack_hrbp.service.UseridAndMonth;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class UseridAndMonthImpl implements UseridAndMonth {
//
//    private final UseridAndMonthDaoImpl useridAndMonthDao;
//    private final Map<String, byte[]> reportStorage = new ConcurrentHashMap<>();
//    private final ExecutorService executorService = Executors.newCachedThreadPool();
//
//    @Override
//    public Map<String, Map<String, String>> getCustomerDetails(String userId) {
//        return useridAndMonthDao.getUserDetails(userId);
//    }
//
//    @Override
//    public Map<String, Map<String, String>> getCustomerDetails(String userId, String month) {
//        return useridAndMonthDao.getUserDetails(userId, month);
//    }
//
//    public SseEmitter generateAttendanceExcelWithSSE(String userid, String month) {
//        SseEmitter emitter = new SseEmitter(60000L); // Timeout after 60 seconds
//        executorService.execute(() -> {
//            try {
//                log.info("Report generation started for User: {} Month: {}", userid, month);
//                emitter.send("Processing started...", MediaType.TEXT_PLAIN);
//
//                byte[] excelData = generateAttendanceExcel(userid, month);
//
//                String key = userid + "_" + month;
//                reportStorage.put(key, excelData);
//
//                emitter.send("Processing complete. Download URL: /hr/download/file/" + key, MediaType.TEXT_PLAIN);
//                emitter.complete();
//            } catch (Exception e) {
//                log.error("Error in report generation: ", e);
//                try {
//                    emitter.send("Error: Report generation failed.", MediaType.TEXT_PLAIN);
//                } catch (IOException ignored) {}
//                emitter.completeWithError(e);
//            }
//        });
//        return emitter;
//    }
//
//    public byte[] generateAttendanceExcel(String userId, String month) throws IOException {
//        Map<String, Map<String, String>> attendanceData = useridAndMonthDao.getUserDetails(userId, month);
//        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            Sheet sheet = workbook.createSheet("Attendance");
//
//            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Date");
//            headerRow.createCell(1).setCellValue("Request Type");
//
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
//            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>();
//
//            for (Map.Entry<String, Map<String, String>> userEntry : attendanceData.entrySet()) {
//                sortedEntries.addAll(userEntry.getValue().entrySet());
//            }
//
//            sortedEntries.sort(Comparator.comparing(entry -> {
//                try {
//                    return dateFormat.parse(entry.getKey());
//                } catch (ParseException e) {
//                    log.warn("Date parsing error for: {}", entry.getKey());
//                    return new Date(0); // Default to epoch date in case of failure
//                }
//            }));
//
//            int rowNum = 1;
//            for (Map.Entry<String, String> entry : sortedEntries) {
//                Row row = sheet.createRow(rowNum++);
//                row.createCell(0).setCellValue(entry.getKey());
//                row.createCell(1).setCellValue(entry.getValue());
//            }
//
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    public byte[] getReport(String key) {
//        return reportStorage.get(key);
//    }
//}



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

