package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private MonthBasedDaoImpl monthBasedDao;

//    public Map<String, Map<String, String>> generateAttendanceReport(String monthYear) throws IOException, ParseException {
//        // Fetch data for the given month and year
//        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);
//
//        // Create a map to store user-wise attendance data
//        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();
//
//        // Parse the date and populate the map
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");
//
//        for (AttendanceEntity attendance : attendanceList) {
//
//            String username = attendance.getUsername();
//            String date = attendance.getDate();
//            String requestType = getRequestTypeCode(attendance.getType());
//
//            Date parsedDate = dateFormat.parse(date);
//            String formattedDate = displayFormat.format(parsedDate);
//
//            userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, requestType);
//
//        }
//
//        // Generate Excel file
//        generateExcel(userAttendanceMap, monthYear);
//
//        // Return the processed data
//        return userAttendanceMap;
//    }

    //from varun

    public byte[] generateAttendanceReport(String monthYear) throws IOException, ParseException {
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

        // Generate Excel file and return it as a byte array
        return generateExcel(userAttendanceMap, monthYear);
    }


    private String getRequestTypeCode(String requestType) {
        switch (requestType) {
            case "Planned Leave":
                return "P";
            case "Unplanned Leave":
                return "U";
            case "UnPlanned Leave":
                return "U";
            case "Planned Leave (Second Half)":
                return "P*";
            case "Sick Leave":
                return "S";
            case "Work From Home":
                return "W";
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

//    private void generateExcel(Map<String, Map<String, String>> userAttendanceMap, String monthYear) throws IOException {
//        Workbook workbook = new XSSFWorkbook();
//        Sheet sheet = workbook.createSheet("Attendance Report");
//
//        // Create header row
//        Row headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("User Name");
//
//        // Get all unique dates
//        Set<String> allDates = new TreeSet<>();
//        for (Map<String, String> dateMap : userAttendanceMap.values()) {
//            allDates.addAll(dateMap.keySet());
//        }
//
//        // Add dates to the header row
//        int colNum = 1;
//        for (String date : allDates) {
//            headerRow.createCell(colNum++).setCellValue(date);
//        }
//
//        // Populate user data
//        int rowNum = 1;
//        for (Map.Entry<String, Map<String, String>> entry : userAttendanceMap.entrySet()) {
//            Row row = sheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(entry.getKey());
//
//            colNum = 1;
//            for (String date : allDates) {
//                String requestType = entry.getValue().getOrDefault(date, "");
//                row.createCell(colNum++).setCellValue(requestType);
//            }
//        }
//
//        // Write the output to a file
//        try (FileOutputStream fileOut = new FileOutputStream("Attendance_Report_" + monthYear + ".xlsx")) {
//            workbook.write(fileOut);
//        }
//
//        workbook.close();
//    }

    //from varun

    private byte[] generateExcel(Map<String, Map<String, String>> userAttendanceMap, String monthYear) throws IOException {
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

        // Write the workbook to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        // Return the byte array
        return outputStream.toByteArray();
    }



//    public Map<String, Map<String, String>> generateAttendanceReport2(String monthYear, String managerId, int page, int limit) throws IOException, ParseException {
//        // Fetch all attendance data for the month
//        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);
//        System.out.println("In Service layer from Repository : " + attendanceList);
//
//        // Fetch employees under the manager
//        List<String> employeeIds = monthBasedDao.getAllEmployeesUnderManager(managerId);
//        System.out.println("In Service Layer from Dao : " + employeeIds);
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
//            String leaveType = attendance.getType(); //
//
//            // Ensure user entry exists and store leave type
//            userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, leaveType);
//        }
//
//        // Log and return the final map
//        System.out.println("Service layer returning Data : " + userAttendanceMap);
//        return userAttendanceMap;
//    }


    public byte[] generateAttendanceReportForManager(String monthYear, String managerId) throws IOException, ParseException {
        // Fetch all attendance data for the month
        List<AttendanceEntity> attendanceList = attendanceRepository.findByDateStartingWith(monthYear);
        System.out.println("In Service layer from Repository : " + attendanceList);

        // Fetch employees under the manager
        List<String> employeeIds = monthBasedDao.getAllEmployeesUnderManager(managerId);
        System.out.println("In Service Layer from Dao : " + employeeIds);


        // Optimize lookup by using a HashSet
        Set<String> employeeIdSet = new HashSet<>(employeeIds);

        // Filter attendance records
        List<AttendanceEntity> filteredAttendance = attendanceList.stream()
                .filter(attendance -> employeeIdSet.contains(attendance.getUserid()))
                .collect(Collectors.toList());

        // Prepare the user-wise attendance map
        Map<String, Map<String, String>> userAttendanceMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");

        for (AttendanceEntity attendance : filteredAttendance) {
            String username = attendance.getUsername();
            String formattedDate = displayFormat.format(dateFormat.parse(attendance.getDate()));
            String requestType = getRequestTypeCode(attendance.getType()); // Use the existing method to get the request type code

            // Ensure user entry exists and store request type
            userAttendanceMap.computeIfAbsent(username, k -> new HashMap<>()).put(formattedDate, requestType);
        }

        // Generate Excel file and return it as a byte array
        return generateExcel(userAttendanceMap, monthYear);
    }


    //trying

//    public Page<String> getPaginatedEmployees(String monthYear, int page, int limit) {
//        Pageable pageable = PageRequest.of(page, limit);
//
//        // Fetch distinct usernames (pagination needs to be handled in-memory)
//        List<String> allUsernames = attendanceRepository.findDistinctUsernamesByMonth(monthYear);
//
//        // Manually paginate the usernames
//        int start = (int) pageable.getOffset();
//        int end = Math.min(start + pageable.getPageSize(), allUsernames.size());
//        List<String> paginatedUsernames = allUsernames.subList(start, end);
//
//        return new PageImpl<>(paginatedUsernames, pageable, allUsernames.size());
//    }
//
//
//
//    public List<AttendanceEntity> getAttendanceForEmployees(String monthYear, List<String> usernames) {
//        return attendanceRepository.findByDateStartingWithAndUsernameIn(monthYear, usernames);
//    }
//
//    //trying for manager
//    public Page<String> getPaginatedEmployeesForManager(String monthYear, String managerId, int page, int limit) {
//        Pageable pageable = PageRequest.of(page, limit);
//
//        // Fetch distinct usernames of employees under this manager
//        List<String> allUsernames = attendanceRepository.findDistinctUsernamesByManager(monthYear, managerId);
//
//        // Manually paginate the usernames
//        int start = (int) pageable.getOffset();
//        int end = Math.min(start + pageable.getPageSize(), allUsernames.size());
//        List<String> paginatedUsernames = allUsernames.subList(start, end);
//
//        return new PageImpl<>(paginatedUsernames, pageable, allUsernames.size());
//    }


    //cleaning
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

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("reportData", paginatedReportData);
        response.put("totalPages", employeePage.getTotalPages());
        response.put("currentPage", page + 1);
        response.put("pageSize", limit);
        response.put("totalRecords", employeePage.getTotalElements());

        return response;
    }


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



//    public Map<String, Object> getAttendanceReportForHR(String monthYear, int page, int limit) {
//        if (page > 0) page -= 1; // Convert to zero-based index
//
//        List<AttendanceEntity> attendanceRecords = monthBasedDao.getAttendanceForEmployees(monthYear,page,limit);
//
//        // Convert data to structured format
//        Map<String, Map<String, String>> paginatedReportData = formatAttendanceData(attendanceRecords);
//
//        // Prepare response
//        Map<String, Object> response = new HashMap<>();
//        response.put("reportData", paginatedReportData);
//        response.put("totalPages", attendanceRecords.getTotalPages());
//        response.put("currentPage", page + 1);
//        response.put("pageSize", limit);
//        response.put("totalRecords", attendanceRecords.getTotalElements());
//
//        return response;
//    }

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




//    private Map<String, Map<String, String>> formatAttendanceData(List<AttendanceEntity> attendanceRecords) {
//        Map<String, Map<String, String>> paginatedReportData = new LinkedHashMap<>();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM-dd");
//
//
//        // Fill attendance data
//        for (AttendanceEntity attendance : attendanceRecords) {
//            String userId = attendance.getUserid();
//            String date = attendance.getDate();
//            String requestType = getRequestTypeCode(attendance.getType());
//
//            try {
//                Date parsedDate = dateFormat.parse(date);
//                String formattedDate = displayFormat.format(parsedDate);
//                paginatedReportData.get(userId).put(formattedDate, requestType);
//            } catch (ParseException e) {
//                e.printStackTrace(); // Log error
//            }
//        }
//
//        System.out.println("paginatedReportData in formatAttendanceData: " + paginatedReportData);
//        return paginatedReportData;
//    }










}