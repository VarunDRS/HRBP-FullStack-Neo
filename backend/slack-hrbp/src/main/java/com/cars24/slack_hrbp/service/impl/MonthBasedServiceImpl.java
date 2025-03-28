
package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.Pair;
import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthBasedServiceImpl {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final MonthBasedDaoImpl monthBasedDao;

    public byte[] generateAttendanceReport(String monthYear, String managerId) throws IOException, ParseException {
        // Fetch data for the given month and year
        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);

        // Fetch employees under the manager
        List<String> employeeIds = monthBasedDao.getAllEmployeesUnderManager(managerId);
        log.info("Employee IDs: {}" + employeeIds);

        // Create a map to store user-wise attendance data
        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();

        // Parse the date and populate the map
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        for (AttendanceEntity attendance : attendanceList) {
            String userid = attendance.getUserid(); // Use userid as the key
            String date = attendance.getDate();
            String requestType = getRequestTypeCode(attendance.getType());

            Date parsedDate = dateFormat.parse(date);
            String formattedDate = displayFormat.format(parsedDate);

            // Store username and request type in the map
            userAttendanceMap.computeIfAbsent(userid, k -> new HashMap<>()).put(formattedDate, requestType);
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

        // Generate Excel file and return it as a byte array
        return generateExcel(userAttendanceMap, monthYear, employeeIds);
    }


    private void populateSheet(Sheet sheet, Map<String, Map<String, String>> userAttendanceMap) {
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
    }

    public String getRequestTypeCode(String requestType) {
        if (requestType == null) {
            return "";  // Or some default code
        }
        switch (requestType) {
            case "Planned Leave":
                return "P";
            case "Unplanned Leave":
            case "UnPlanned Leave":
                return "U";
            case "Planned Leave (Second Half)":
                return "P*";
            case "Sick Leave":
                return "S";
            case "Work From Home":
            case "WFH":
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


    private byte[] generateExcel(Map<String, Map<String, String>> userAttendanceMap, String monthYear, List<String> employeeIds) throws IOException, ParseException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("User Name");
        headerRow.createCell(1).setCellValue("Total Leaves"); // Inserted column
        headerRow.createCell(2).setCellValue("Total WFH"); // Inserted column

        // Generate all dates for the given month
        List<String> allDates = generateAllDatesForMonth(monthYear);

        // Adjust column numbers for allDates
        int colNum = 3; // Shifted to accommodate new columns
        for (String date : allDates) {
            headerRow.createCell(colNum++).setCellValue(date);
        }

        // Fetch usernames for employeeIds using EmployeeRepository (Neo4j)
        Map<String, String> userIdToUsernameMap = new HashMap<>();
        for (String userid : employeeIds) {
            List<EmployeeEntity> employeeList = employeeRepository.findAllByUserId(userid);
            if (!employeeList.isEmpty()) {
                userIdToUsernameMap.put(userid, employeeList.get(0).getUsername());
            } else {
                userIdToUsernameMap.put(userid, "Unknown User");
            }
        }

        // Populate user data for all employees
        int rowNum = 1;
        for (String userid : employeeIds) {
            Row row = sheet.createRow(rowNum++);
            String username = userIdToUsernameMap.getOrDefault(userid, "Unknown User");
            row.createCell(0).setCellValue(username);

            // Insert Total Leaves and Total WFH
            Map<String, String> userAttendance = userAttendanceMap.getOrDefault(userid, new HashMap<>());
            row.createCell(1).setCellValue(userAttendance.getOrDefault("Total Leaves", "0")); // Inserted value
            row.createCell(2).setCellValue(userAttendance.getOrDefault("Total WFH", "0")); // Inserted value

            colNum = 3; // Adjusted to start from the correct column
            for (String date : allDates) {
                String requestType = userAttendance.getOrDefault(date, "");
                row.createCell(colNum++).setCellValue(requestType);
            }
        }

        // Write the workbook to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private List<String> generateAllDatesForMonth(String monthYear) throws ParseException {
        List<String> allDates = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        // Parse the input monthYear to get the year and month
        Date date = inputFormat.parse(monthYear);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Get the number of days in the month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Generate all dates for the month
        for (int day = 1; day <= daysInMonth; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            String formattedDate = displayFormat.format(calendar.getTime());
            allDates.add(formattedDate);
        }

        return allDates;
    }


    public Map<String, Map<String, String>> processAttendanceData(List<AttendanceEntity> attendanceList) throws ParseException {
        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();
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

        return userAttendanceMap;
    }

    public byte[] generateAttendanceReports(String fromMonth, String toMonth, String managerId) throws IOException, ParseException {
        // Create a new workbook to combine all sheets
        try (Workbook combinedWorkbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Parse the from and to monthYear strings into dates
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
            Date fromDate = inputFormat.parse(fromMonth);
            Date toDate = inputFormat.parse(toMonth);

            // Create a calendar to iterate through the months
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromDate);

            // Iterate through each month in the range
            while (!calendar.getTime().after(toDate)) {
                String currentMonthYear = inputFormat.format(calendar.getTime());

                // Generate the report for the current month
                byte[] excelData = generateAttendanceReport(currentMonthYear, managerId);

                // Load the generated workbook
                try (Workbook monthlyWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                    // Copy the sheet from the monthly workbook to the combined workbook
                    for (int i = 0; i < monthlyWorkbook.getNumberOfSheets(); i++) {
                        Sheet sourceSheet = monthlyWorkbook.getSheetAt(i);
                        String sheetName = currentMonthYear; // Use the monthYear as the sheet name
                        Sheet targetSheet = combinedWorkbook.createSheet(sheetName);

                        // Copy all rows and cells from the source sheet to the target sheet
                        for (int j = 0; j <= sourceSheet.getLastRowNum(); j++) {
                            Row sourceRow = sourceSheet.getRow(j);
                            if (sourceRow != null) {
                                Row targetRow = targetSheet.createRow(j);
                                for (int k = 0; k < sourceRow.getLastCellNum(); k++) {
                                    Cell sourceCell = sourceRow.getCell(k);
                                    if (sourceCell != null) {
                                        Cell targetCell = targetRow.createCell(k);
                                        targetCell.setCellValue(sourceCell.getStringCellValue());
                                    }
                                }
                            }
                        }
                    }
                }

                // Move to the next month
                calendar.add(Calendar.MONTH, 1);
            }

            // Write the combined workbook to a ByteArrayOutputStream
            combinedWorkbook.write(outputStream);

            // Return the byte array
            return outputStream.toByteArray();
        }
    }



    public Map<Pair, Map<String, String>> formatAttendanceData(List<AttendanceEntity> attendanceRecords, List<String> employeeUserIds) {
        Map<Pair, Map<String, String>> paginatedReportData = new LinkedHashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        // Create a mapping of userId → username (Assuming you get username from `attendanceRecords`)
        String username = "";
        Map<String, String> userIdToNameMap = new HashMap<>();
        for (String id : employeeUserIds) {
            Optional<EmployeeEntity> employee = employeeRepository.findByUserId(id);
            if (employee.isPresent()) {
                username = employee.get().getUsername();
            }
            userIdToNameMap.put(id, username);
        }

        // Initialize report data for all employees
        for (String userId : employeeUserIds) {
            username = userIdToNameMap.getOrDefault(userId, "Unknown"); // Handle missing usernames
            paginatedReportData.putIfAbsent(new Pair(userId, username), new LinkedHashMap<>());
        }

        for (AttendanceEntity attendance : attendanceRecords) {
            String userId = attendance.getUserid();
            username = userIdToNameMap.getOrDefault(userId, "Unknown");
            Pair key = new Pair(userId, username);

            String date = attendance.getDate();
            String requestType = getRequestTypeCode(attendance.getType());

            try {
                Date parsedDate = dateFormat.parse(date);
                String formattedDate = displayFormat.format(parsedDate);

                paginatedReportData.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(formattedDate, requestType);
            } catch (ParseException e) {
                e.printStackTrace(); // Log error
            }
        }

        log.info("paginatedReportData in formatAttendanceData: {}", paginatedReportData);
        return paginatedReportData;
    }

    // Common helper method that both report methods will use
    private Map<String, Object> generateAttendanceReportCommon(String monthYear, List<String> employeeUserIds,
                                                               Page<String> employeePage, int page) {

        // Fetch attendance records for these employees
        List<AttendanceEntity> attendanceRecords = monthBasedDao.getAttendanceForEmployees(monthYear, employeeUserIds);
        log.info("getAttendanceForEmployees in service layer: {}", attendanceRecords);

        // Transform attendance records into structured data
        Map<Pair, Map<String, String>> paginatedReportData = formatAttendanceData(attendanceRecords, employeeUserIds);

        // Calculate totals for each user
        for (Map.Entry<Pair, Map<String, String>> entry : paginatedReportData.entrySet()) {
            Map<String, String> userData = entry.getValue();
            int totalWFH = 0;
            int totalLeaves = 0;

            for (Map.Entry<String, String> dateEntry : userData.entrySet()) {
                String type = dateEntry.getValue();
                if (type.equals("W")) {
                    totalWFH++;
                } else if (type.equals("P") || type.equals("U") || type.equals("S") ||
                        type.equals("P*") || type.equals("P**")) {
                    totalLeaves++;
                }
            }

            // Add Total WFH and Total Leaves to the user's map
            userData.put("Total WFH", String.valueOf(totalWFH));
            userData.put("Total Leaves", String.valueOf(totalLeaves));
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("reportData", paginatedReportData);
        response.put("totalPages", employeePage.getTotalPages());
        response.put("currentPage", page + 1);
        response.put("pageSize", employeePage.getSize());
        response.put("totalRecords", employeePage.getTotalElements());

        return response;
    }

    // Original HR method (now simplified)
    public Map<String, Object> getAttendanceReportForHR(String monthYear, int page, int limit) {
        if (page > 0) page -= 1; // Convert to zero-based index

        // Get paginated employees for HR
        Page<String> employeePage = monthBasedDao.getPaginatedEmployeesForHr(page, limit);
        List<String> employeeUserIds = employeePage.getContent();

        return generateAttendanceReportCommon(monthYear, employeeUserIds, employeePage, page);
    }

    // Original Manager method (now simplified)
    public Map<String, Object> getAttendanceReportForManager(String monthYear, String managerId, int page, int limit) {
        if (page > 0) page -= 1; // Convert to zero-based index

        // Get paginated employees for Manager
        Page<String> employeePage = monthBasedDao.getPaginatedEmployeesForManager(managerId, page, limit);
        List<String> employeeUserIds = employeePage.getContent();

        return generateAttendanceReportCommon(monthYear, employeeUserIds, employeePage, page);
    }

}
