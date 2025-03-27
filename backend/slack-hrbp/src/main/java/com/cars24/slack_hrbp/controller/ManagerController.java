package com.cars24.slack_hrbp.controller;


import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/manager")
@Slf4j
@RequiredArgsConstructor

public class ManagerController {

    private final MonthBasedServiceImpl monthBasedService;
    private final UseridAndMonthImpl useridandmonth;
    private final ListAllEmployeesUnderManagerServiceImpl listAllEmployeesUnderManager;
    private final ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month){

        return useridandmonth.getCustomerDetails(userid,month);

    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId){
        return useridandmonth.getCustomerDetails(userId);
    }


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/displayUsers/{userId}/{searchtag}")
    public ResponseEntity<List<GetUserResponse>> getAllUsers(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        // Convert page number to zero-based index
        if (page > 0) page -= 1;

        Page<List<String>> users = listAllEmployeesUnderManager.getAllEmployeesUnderManager(userId, page, limit, searchtag);
        List<GetUserResponse> responses = new ArrayList<>();

        // Iterate over users.getContent()
        for (List<String> userDto : users.getContent()) {
            if (userDto.size() >= 3) { // Ensure list contains required values
                GetUserResponse res = new GetUserResponse();
                res.setUserId(userDto.get(0));
                res.setEmail(userDto.get(1));
                res.setUsername(userDto.get(2));
                responses.add(res);
            }
        }

        return ResponseEntity.ok().body(responses);
    }


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/displayUsers/count/{userId}/{searchtag}")
    public ResponseEntity<Map<String, Object>> getTotalUserCount(@PathVariable String userId,
                                                                 @PathVariable String searchtag,
                                                                 @RequestParam(value = "limit", defaultValue = "2") int limit) {
        long totalEmployees = listAllEmployeesUnderManagerDao.getTotalEmployeesCount(userId,searchtag);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalEmployees / limit);

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("totalEmployees", totalEmployees);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }


    // Individual calender view
    // Generate report call

    @GetMapping(value = "/events/{userid}/{frommonth}/{tomonth}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String userid,@PathVariable String frommonth,@PathVariable String tomonth) {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                // Send "Generating Excel" message first
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                // Simulate report generation delay (replace with actual logic)
//                Thread.sleep(5000); // Simulate processing delay

                byte[] excelData = useridandmonth.generateAttendanceExcel(userid, frommonth ,tomonth);

                String directoryPath = "reports";
                File reportsDir = new File(directoryPath);

                // Ensure the directory exists
                if (!reportsDir.exists()) {
                    reportsDir.mkdirs();  // Create the directory if it doesn't exist
                }

                long endTime = System.currentTimeMillis(); // Record end time
                long duration = endTime - startTime; // Calculate latency


                log.info("Report generation time: {} ms", duration);

                String filePath = "reports/Attendance_" + userid + "_" + "from_" + frommonth + "_to_" + tomonth + ".xlsx";
                Files.write(Paths.get(filePath), excelData);
                log.info("Report generated at: {}",filePath);
                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete(); // Close connection after sending
            } catch (IOException e) {
                try {
                    emitter.send(SseEmitter.event().data("Error generating report"));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return emitter;
    }

    // Notify frontend for download report for specific time period (from and to)
    @GetMapping("/download/{userid}/{frommonth}/{tomonth}")
    public ResponseEntity<Resource> downloadReport(
            @PathVariable String userid,
            @PathVariable String frommonth,
            @PathVariable String tomonth) {

        String filename = "Attendance_" + userid + "_from_" + frommonth + "_to_" + tomonth + ".xlsx";
        String filePath = "reports/" + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"") // Ensure double quotes
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    // By Month For All Users
    // For one particular month view
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/bymonth")
    public ResponseEntity<Map<String, Object>> getByMonthForManager(
            @RequestParam String monthYear,
            @RequestParam String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        return ResponseEntity.ok(monthBasedService.getAttendanceReportForManager(monthYear, userId, page, limit));
    }

    // For one particular month report
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/bymonthreport")
    public ResponseEntity<byte[]> getByMonthandManagerid(
            @RequestParam String monthYear,
            @RequestParam String managerId) { // Changed parameter name to managerId for consistency
        try {
            // Generate the report and Excel file
            byte[] excelFile = monthBasedService.generateAttendanceReport(monthYear, managerId);

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Manager_Attendance_Report_" + monthYear + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping(value = "/events/bymonthreportformanager", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEventsForMonth(
            @RequestParam String frommonth,
            @RequestParam String tomonth,
            @RequestParam String managerId) {

        // Validate inputs
        if (!frommonth.matches("\\d{4}-\\d{2}") || !tomonth.matches("\\d{4}-\\d{2}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use YYYY-MM.");
        }

        SseEmitter emitter = new SseEmitter(0L); // No timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                // Generate the report
                byte[] excelData = monthBasedService.generateAttendanceReports(frommonth, tomonth, managerId);

                // Define a secure base directory
                Path baseDirectory = Paths.get("secure-reports/monthreports").toAbsolutePath().normalize();
                Files.createDirectories(baseDirectory); // Ensure the directory exists

                // Construct a safe file path
                String fileName = String.format("Attendance_from_%s_to_%s.xlsx", frommonth, tomonth);
                Path filePath = baseDirectory.resolve(fileName).normalize();

                // Prevent directory traversal attacks
                if (!filePath.startsWith(baseDirectory)) {
                    throw new SecurityException("Invalid file path.");
                }

                Files.write(filePath, excelData);
                log.info("Report generated at: {}", filePath);

                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete();
            } catch (IOException | ParseException e) {
                try {
                    emitter.send(SseEmitter.event().data("Error generating report"));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    log.error("Error sending SSE event", ex);
                }
            }
        });

        return emitter;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/download/bymonthreportformanager")
    public ResponseEntity<Resource> downloadMonthReportForManager(
            @RequestParam String frommonth,
            @RequestParam String tomonth) {

        // Validate inputs
        if (!frommonth.matches("\\d{4}-\\d{2}") || !tomonth.matches("\\d{4}-\\d{2}")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Path baseDirectory = Paths.get("secure-reports/monthreports").toAbsolutePath().normalize();
        String fileName = String.format("Attendance_from_%s_to_%s.xlsx", frommonth, tomonth);
        Path filePath = baseDirectory.resolve(fileName).normalize();

        // Prevent directory traversal
        if (!filePath.startsWith(baseDirectory)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        File file = filePath.toFile();
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
