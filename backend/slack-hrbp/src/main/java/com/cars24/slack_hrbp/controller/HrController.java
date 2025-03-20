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
import java.io.IOException;
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

    @GetMapping(value = "/events/{userid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String userid) {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Send "Generating Excel" message first
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                // Simulate report generation delay (replace with actual logic)
                Thread.sleep(5000); // Simulate processing delay

                // Send "Download Ready" message
                emitter.send(SseEmitter.event().data("Excel Downloaded successfully"));

                emitter.send(SseEmitter.event().data("DONE"));

                emitter.complete(); // Close connection after sending
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // API to trigger Excel generation & notify frontend
    @GetMapping("/download/{userid}/{month}")
    public void downloadReport(@PathVariable String userid, @PathVariable String month, HttpServletResponse response) {
        SseEmitter emitter = emitters.get(userid);
        try {
            // Notify frontend that generation has started
            if (emitter != null) {
                emitter.send(SseEmitter.event().name("status").data("Excel is being generated..."));
            }

            // Generate Excel
            byte[] excelData = useridandmonth.generateAttendanceExcel(userid, month);

            // Set response headers for file download
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=Attendance_" + userid + "_" + month + ".xlsx");
            response.getOutputStream().write(excelData);
            response.flushBuffer();

            // Notify frontend that download is ready
            if (emitter != null) {
                emitter.send(SseEmitter.event().name("status").data("Excel Downloaded Successfully!"));
                emitter.complete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event().name("status").data("Failed to generate Excel."));
                    emitter.complete();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
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
    public ResponseEntity<byte[]> getByMonth(@RequestParam String monthYear) {
        try {
            // Generate the report and Excel file
            byte[] excelFile = monthBasedService.generateAttendanceReport(monthYear);

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

}


