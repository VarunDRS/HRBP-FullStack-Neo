package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonthBasedServiceImpl {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MonthBasedDaoImpl monthBasedDao;



    public byte[] generateAttendanceReport(String monthYear, String managerId) throws IOException, ParseException {
        // Fetch data for the given month and year
        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);

        // Fetch employees under the manager
        List<String> employeeIds = monthBasedDao.getAllEmployeesUnderManager(managerId);
        System.out.println("Employee IDs: " + employeeIds);

        // Create a map to store user-wise attendance data
        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();

        // Parse the date and populate the map
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        for (AttendanceEntity attendance : attendanceList) {
            String userid = attendance.getUserid(); // Use userid as the key
            String username = attendance.getUsername(); // Include username for reference
            String date = attendance.getDate();
            String requestType = getRequestTypeCode(attendance.getType());

            Date parsedDate = dateFormat.parse(date);
            String formattedDate = displayFormat.format(parsedDate);

            // Store username and request type in the map
            userAttendanceMap.computeIfAbsent(userid, k -> new HashMap<>()).put(formattedDate, requestType);
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



    private byte[] generateExcel(Map<String, Map<String, String>> userAttendanceMap, String monthYear, List<String> employeeIds) throws IOException, ParseException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("User Name");

        // Generate all dates for the given month
        List<String> allDates = generateAllDatesForMonth(monthYear);

        // Add all dates to the header row
        int colNum = 1;
        for (String date : allDates) {
            headerRow.createCell(colNum++).setCellValue(date);
        }

        // Fetch usernames for employeeIds using EmployeeRepository (Neo4j)
        Map<String, String> userIdToUsernameMap = new HashMap<>();
        for (String userid : employeeIds) {
            List<EmployeeEntity> employeeList = employeeRepository.findAllByUserId(userid);
            if (!employeeList.isEmpty()) {
                // Use the username from the first result
                userIdToUsernameMap.put(userid, employeeList.get(0).getUsername());
            } else {
                userIdToUsernameMap.put(userid, "Unknown User"); // Default to "Unknown User" if username not found
            }
        }

        // Populate user data for all employees
        int rowNum = 1;
        for (String userid : employeeIds) {
            Row row = sheet.createRow(rowNum++);
            String username = userIdToUsernameMap.getOrDefault(userid, "Unknown User"); // Default to "Unknown User" if username not found
            row.createCell(0).setCellValue(username);

            colNum = 1;
            for (String date : allDates) {
                // Get the request type for the userid and date, or default to empty string
                String requestType = userAttendanceMap.getOrDefault(userid, new HashMap<>()).getOrDefault(date, "");
                row.createCell(colNum++).setCellValue(requestType);
            }
        }

        // Write the workbook to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        // Return the byte array
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



//    public byte[] generateAttendanceReportForManager(String monthYear, String managerId) throws IOException, ParseException {
//        // Fetch all attendance data for the month
//        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);
//        System.out.println("In Service layer from Repository : " + attendanceList);
//
//        // Fetch employees under the manager
//        List<String> employeeIds = monthBasedDao.getAllEmployeesUnderManager(managerId);
//        System.out.println("In Service Layer from Dao : " + employeeIds);
//
//
//        // Optimize lookup by using a HashSet
//        Set<String> employeeIdSet = new HashSet<>(employeeIds);
//
//        // Filter attendance records
//        List<AttendanceEntity> filteredAttendance = attendanceList.stream()
//                .filter(attendance -> employeeIdSet.contains(attendance.getUserid()))
//                .collect(Collectors.toList());
//
//        // Prepare the user-wise attendance map
//        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");
//
//        for (AttendanceEntity attendance : filteredAttendance) {
//            String username = attendance.getUsername();
//            String formattedDate = displayFormat.format(dateFormat.parse(attendance.getDate()));
//            String requestType = getRequestTypeCode(attendance.getType()); // Use the existing method to get the request type code
//
//            // Ensure user entry exists and store request type
//            userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, requestType);
//        }
//
//        // Generate Excel file and return it as a byte array
//        return generateExcel(userAttendanceMap, monthYear, employeeIds);
//    }


    private Map<String, Map<String, String>> processAttendanceData(List<AttendanceEntity> attendanceList) throws ParseException {
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
        Workbook combinedWorkbook = new XSSFWorkbook();

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
            Workbook monthlyWorkbook = new XSSFWorkbook(new ByteArrayInputStream(excelData));

            // Copy the sheet from the monthly workbook to the combined workbook
            for (int i = 0; i < monthlyWorkbook.getNumberOfSheets(); i++) {
                Sheet sourceSheet = monthlyWorkbook.getSheetAt(i);

                // Create a unique sheet name for the combined workbook
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

            // Close the monthly workbook
            monthlyWorkbook.close();

            // Move to the next month
            calendar.add(Calendar.MONTH, 1);
        }

        // Write the combined workbook to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        combinedWorkbook.write(outputStream);
        combinedWorkbook.close();

        // Return the byte array
        return outputStream.toByteArray();
    }






//    public byte[] generateAttendanceReportsForManager(String fromMonthYear, String toMonthYear, String managerId) throws IOException, ParseException {
//        // Fetch all employees under the given manager
//        List<String> employeeIds = monthBasedDao.getAllEmployeesUnderManager(managerId);
//        System.out.println("In Service Layer from Dao: " + employeeIds);
//
//        // Optimize lookup by using a HashSet
//        Set<String> employeeIdSet = new HashSet<>(employeeIds);
//
//        // Create a new workbook
//        Workbook workbook = new XSSFWorkbook();
//
//        // Parse the from and to monthYear strings into dates
//        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
//        Date fromDate = inputFormat.parse(fromMonthYear);
//        Date toDate = inputFormat.parse(toMonthYear);
//
//        // Create a calendar to iterate through the months
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(fromDate);
//
//        // Iterate through each month in the range
//        while (!calendar.getTime().after(toDate)) {
//            String currentMonthYear = inputFormat.format(calendar.getTime());
//
//            // Fetch all attendance data for the current month
//            List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(currentMonthYear);
//            System.out.println("In Service layer from Repository for " + currentMonthYear + ": " + attendanceList);
//
//            // Filter attendance records for employees under the manager
//            List<AttendanceEntity> filteredAttendance = attendanceList.stream()
//                    .filter(attendance -> employeeIdSet.contains(attendance.getUserid()))
//                    .collect(Collectors.toList());
//
//            // Prepare the user-wise attendance map
//            Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");
//
//            for (AttendanceEntity attendance : filteredAttendance) {
//                String username = attendance.getUsername();
//                String formattedDate = displayFormat.format(dateFormat.parse(attendance.getDate()));
//                String requestType = getRequestTypeCode(attendance.getType()); // Use the existing method to get the request type code
//
//                // Ensure user entry exists and store request type
//                userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, requestType);
//            }
//
//            // Create a new sheet for the current month
//            Sheet sheet = workbook.createSheet(currentMonthYear);
//            populateSheet(sheet, userAttendanceMap);
//
//            // Move to the next month
//            calendar.add(Calendar.MONTH, 1);
//        }
//
//        // Write the workbook to a ByteArrayOutputStream
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        workbook.write(outputStream);
//        workbook.close();
//
//        // Return the byte array
//        return outputStream.toByteArray();
//    }


    private Map<String, Map<String, String>> formatAttendanceData(List<AttendanceEntity> attendanceRecords, List<String> employeeUserIds) {
        Map<String, Map<String, String>> paginatedReportData = new LinkedHashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        // Initialize the report data for all employees with empty maps
        for (String userId : employeeUserIds) {
            paginatedReportData.putIfAbsent(userId, new LinkedHashMap<>());
        }

        for (AttendanceEntity attendance : attendanceRecords) {
            String userId = attendance.getUserid();
            String date = attendance.getDate();
            String requestType = getRequestTypeCode(attendance.getType());

            try {
                Date parsedDate = dateFormat.parse(date);
                String formattedDate = displayFormat.format(parsedDate);

                paginatedReportData.computeIfAbsent(userId, k -> new LinkedHashMap<>()).put(formattedDate, requestType);
            } catch (ParseException e) {
                e.printStackTrace(); // Log error
            }
        }

        System.out.println("paginatedReportData in formatAttendanceData: " + paginatedReportData);
        return paginatedReportData;
    }







    public Map<String, Object> getAttendanceReportForHR(String monthYear,int page, int limit) {
        // Convert to zero-based index for pagination
        if (page > 0) page -= 1;

        try {
            // Fetch all data first
            Map<String, Map<String, String>> allReportData = monthBasedDao.generateAttendanceReport(monthYear);

            // Convert to a list for pagination
            List<Map.Entry<String, Map<String, String>>> allUsersList = new ArrayList<>(allReportData.entrySet());

            // Paginate based on employees, not individual records
            int totalRecords = allUsersList.size();
            int startIndex = page * limit;
            int endIndex = Math.min(startIndex + limit, totalRecords);

            // Extract paginated data
            Map<String, Map<String, String>> paginatedReportData = new LinkedHashMap<>();
            for (int i = startIndex; i < endIndex; i++) {
                Map.Entry<String, Map<String, String>> entry = allUsersList.get(i);
                paginatedReportData.put(entry.getKey(), entry.getValue());
            }

            // Calculate total pages based on employees
            int totalPages = (int) Math.ceil((double) totalRecords / limit);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("reportData", paginatedReportData);
            response.put("totalPages", totalPages);
            response.put("currentPage", page + 1);
            response.put("pageSize", limit);
            response.put("totalRecords", totalRecords);
            System.out.println("The Response in controller is : "+response);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

    public Map<String, Object> getAttendanceReportForManager(String monthYear, String managerId, int page, int limit) {
        if (page > 0) page -= 1; // Convert to zero-based index

        // Get paginated employees under the manager
        Page<String> employeePage = monthBasedDao.getPaginatedEmployeesForManager(managerId, page, limit);
        List<String> employeeUserIds = employeePage.getContent(); // Use userId instead of username

        System.out.println(employeeUserIds);

        // Fetch attendance records for these employees
        List<AttendanceEntity> attendanceRecords = monthBasedDao.getAttendanceForEmployees(monthYear, employeeUserIds);
        System.out.println("getAttendanceForEmployees in service layer: " + attendanceRecords);

        // Transform attendance records into structured data
        Map<String, Map<String, String>> paginatedReportData = formatAttendanceData(attendanceRecords,employeeUserIds);
        for (Map.Entry<String, Map<String, String>> entry : paginatedReportData.entrySet()) {
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

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("reportData", paginatedReportData);
        response.put("totalPages", employeePage.getTotalPages());
        response.put("currentPage", page + 1);
        response.put("pageSize", limit);
        response.put("totalRecords", employeePage.getTotalElements());

        return response;
    }


}