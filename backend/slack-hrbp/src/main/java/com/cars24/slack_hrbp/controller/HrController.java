package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.data.response.EmployeeDisplayResponse;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/hr")
@RequiredArgsConstructor
@Valid

public class HrController {

    private final HrServiceImpl hrService;
    private final MonthBasedServiceImpl monthBasedService;
    private final UseridAndMonthImpl useridandmonth;
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private String generatedReportPath;


    @PreAuthorize("hasRole('HR')")
    @PostMapping("/createUser")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateEmployeeRequest createEmployeeRequest){
        log.info("HrController createEmployeeRequest, {}", createEmployeeRequest);
        EmployeeEntity employee = hrService.createEmployee(createEmployeeRequest);
        return ResponseEntity.ok("Creation was successful");
    }


    @PreAuthorize("hasRole('HR')")
    @PutMapping("/updateManager/{userId}/{newManagerId}")
    public ResponseEntity<String> updateManager(
            @PathVariable("userId") String userId,
            @PathVariable("newManagerId") String newManagerId) {
        try {
            hrService.updateManager(userId, newManagerId);
            return ResponseEntity.ok("Manager updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasRole('HR')")
    @PutMapping("/updaterole")
    public ResponseEntity<String> updateRole(@RequestBody EmployeeUpdateRequest employeeUpdateRequest) {
        System.out.println(employeeUpdateRequest);
        String response = hrService.updateUser(employeeUpdateRequest);
        return ResponseEntity.ok().body(response);
    }


    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month){
        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userid,month);
        return resp;
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId){
        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userId);
        System.out.println(resp);
        return resp;
    }

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping ("/deleteEntry/{userId}/{date}")
    public ResponseEntity<String> deleteEntry(@PathVariable String userId,@PathVariable String date){
        String resp = hrService.deleteEntry(userId,date);
        return ResponseEntity.ok(resp);
   }

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


                System.out.println("Report generation time: " + duration + " ms");
                String filePath = "reports/Attendance_" + userid + "_" + "from_" + frommonth + "_to_" + tomonth + ".xlsx";
                Files.write(Paths.get(filePath), excelData);
                System.out.println(" Report generated at: " + filePath);
                // Send "Download Ready" message
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

    // API to trigger Excel generation & notify frontend
    @GetMapping("/download/{userid}/{frommonth}/{tomonth}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String userid, @PathVariable String frommonth, @PathVariable String tomonth) {
        String filePath = "reports/Attendance_" + userid + "_" + "from_" + frommonth + "_to_" + tomonth + ".xlsx";
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);


    }

    
    @PreAuthorize("hasRole('HR')")
    @GetMapping("/displayUsers/{userId}/{searchtag}")
    public ResponseEntity<List<GetUserResponse>> getAllUsers(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        if (page > 0)
            page -= 1;

        Page<List<String>> users = hrService.getAllUsers(userId, page, limit, searchtag);
        List<GetUserResponse> responses = new ArrayList<>();

        for (List<String> userDto : users.getContent()) {
                GetUserResponse res = new GetUserResponse();
                res.setUserId(userDto.get(0));
                res.setEmail(userDto.get(1));
                res.setUsername(userDto.get(2));
                responses.add(res);
        }

        return ResponseEntity.ok().body(responses);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/displayUsers/count/{userId}/{searchtag}")
    public ResponseEntity<Map<String, Object>> getTotalUserCount(@PathVariable String userId,
                                                                 @PathVariable String searchtag,
                                                                 @RequestParam(value = "limit", defaultValue = "2") int limit) {

        long totalEmployees = hrService.getTotalEmployeesCount(searchtag);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalEmployees / limit);

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("totalEmployees", totalEmployees);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('HR')")
    @GetMapping("/bymonth")
    public ResponseEntity<Map<String, Object>> getByMonth(
            @RequestParam String monthYear,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        return ResponseEntity.ok(monthBasedService.getAttendanceReportForHR(monthYear, page, limit));
    }


    @PreAuthorize("hasRole('HR')")
    @GetMapping("bymonthreport")
    public ResponseEntity<byte[]> getByMonth(
            @RequestParam String monthYear,
            @RequestParam String managerId) { // Added managerId parameter
        try {
            // Generate the report and Excel file
            byte[] excelFile = monthBasedService.generateAttendanceReport(monthYear, managerId);

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Attendance_Report_" + monthYear + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping(value = "/events/bymonthreport", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEventsForMonth(
            @RequestParam String frommonth,
            @RequestParam String tomonth,
            @RequestParam String managerId) { // Added managerId parameter
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                // Send "Generating Excel" message first
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                // Generate the report
                byte[] excelData = monthBasedService.generateAttendanceReports(frommonth, tomonth, managerId);

                String directoryPath = "monthreports";
                File reportsDir = new File(directoryPath);

                // Ensure the directory exists
                if (!reportsDir.exists()) {
                    reportsDir.mkdirs();  // Create the directory if it doesn't exist
                }

                long endTime = System.currentTimeMillis(); // Record end time
                long duration = endTime - startTime; // Calculate latency

                System.out.println("Report generation time: " + duration + " ms");
                String filePath = "monthreports/Attendance_" + "_" + "from_" + frommonth + "_to_" + tomonth + ".xlsx";
                Files.write(Paths.get(filePath), excelData);
                System.out.println(" Report generated at: " + filePath);

                // Send "Download Ready" message
                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete(); // Close connection after sending
            } catch (IOException | ParseException e) {
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

    // API to trigger Excel generation & notify frontend
    @PreAuthorize("hasRole('HR')")
    @GetMapping("/download/bymonthreport")
    public ResponseEntity<Resource> downloadMonthReport(
            @RequestParam String frommonth,
            @RequestParam String tomonth,
            @RequestParam String managerId) {
        String filePath = "monthreports/Attendance_" + "_" + "from_" + frommonth + "_to_" + tomonth + ".xlsx";
        File file = new File(filePath);

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


