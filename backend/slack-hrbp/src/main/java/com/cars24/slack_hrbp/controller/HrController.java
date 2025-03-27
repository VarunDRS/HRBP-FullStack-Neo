package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.request.*;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;


import java.util.*;

@Slf4j
@RestController
@RequestMapping("/hr")
@RequiredArgsConstructor
@Valid
public class HrController {

    private final HrServiceImpl hrService;
    private final MonthBasedServiceImpl monthBasedService;
    private final AttendanceControllerHelper helper;

    // HR-specific methods remain unchanged
    @PreAuthorize("hasRole('HR')")
    @PostMapping("/createUser")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateEmployeeRequest createEmployeeRequest) {
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
        String response = hrService.updateUser(employeeUpdateRequest);
        return ResponseEntity.ok().body(response);
    }

    // Modified to use helper
    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month) {
        return helper.handleGetUserDetails(userid, month);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId) {
        return helper.handleGetUserDetails(userId);
    }

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping("/deleteEntry/{userId}/{date}")
    public ResponseEntity<String> deleteEntry(@PathVariable String userId, @PathVariable String date) {
        String resp = hrService.deleteEntry(userId, date);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/addEntry/{userId}")
    public ResponseEntity<String> addEntry(@PathVariable String userId, @RequestBody AddLeaveRequest addLeaveRequest) {
        log.info("The inputs are {} {} {} {}", userId, addLeaveRequest.getDate(),
                addLeaveRequest.getLeaveType(), addLeaveRequest.getReason());
        String resp = hrService.addEntry(userId, addLeaveRequest.getDate(),
                addLeaveRequest.getLeaveType(), addLeaveRequest.getReason());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/displayUsers/{userId}/{searchtag}")
    public ResponseEntity<List<GetUserResponse>> getAllUsers(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        if (page > 0) page -= 1;

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
    public ResponseEntity<Map<String, Object>> getTotalUserCount(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        long totalEmployees = hrService.getTotalEmployeesCount(searchtag);
        int totalPages = (int) Math.ceil((double) totalEmployees / limit);

        Map<String, Object> response = new HashMap<>();
        response.put("totalEmployees", totalEmployees);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    // Modified to use helper
    @GetMapping(value = "/events/{userid}/{frommonth}/{tomonth}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(
            @PathVariable String userid,
            @PathVariable String frommonth,
            @PathVariable String tomonth) {
        return helper.handleStreamEvents(userid, frommonth, tomonth);
    }

    // Modified to use helper
    @GetMapping("/download/{userid}/{frommonth}/{tomonth}")
    public ResponseEntity<Resource> downloadReport(
            @PathVariable String userid,
            @PathVariable String frommonth,
            @PathVariable String tomonth) {
        System.out.println("download backend called");
        return helper.handleDownloadReport(userid, frommonth, tomonth);
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
            @RequestParam String managerId) {
        try {
            byte[] excelFile = monthBasedService.generateAttendanceReport(monthYear, managerId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=Attendance_Report_" + monthYear + ".xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);
        } catch (Exception e) {
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

    // Modified to use helper
    @PreAuthorize("hasRole('HR')")
    @GetMapping(value = "/events/bymonthreport", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEventsForMonth(
            @RequestParam String frommonth,
            @RequestParam String tomonth,
            @RequestParam String managerId) {
        return helper.handleStreamEventsForMonth(frommonth, tomonth, managerId);
    }

    // Modified to use helper
    @PreAuthorize("hasRole('HR')")
    @GetMapping("/download/bymonthreport")
    public ResponseEntity<Resource> downloadMonthReport(
            @RequestParam String frommonth,
            @RequestParam String tomonth) {
        return helper.handleDownloadMonthReport(frommonth, tomonth);
    }
}