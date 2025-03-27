package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
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

@RestController
@RequestMapping("/manager")
@Slf4j
@RequiredArgsConstructor
public class ManagerController {

    private final MonthBasedServiceImpl monthBasedService;
    private final ListAllEmployeesUnderManagerServiceImpl listAllEmployeesUnderManager;
    private final ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;
    private final AttendanceControllerHelper helper;

    // Modified to use helper
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(
            @PathVariable String userid,
            @PathVariable String month) {
        return helper.handleGetUserDetails(userid, month);
    }

    // Modified to use helper
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId) {
        return helper.handleGetUserDetails(userId);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/displayUsers/{userId}/{searchtag}")
    public ResponseEntity<List<GetUserResponse>> getAllUsers(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        if (page > 0) page -= 1;

        Page<List<String>> users = listAllEmployeesUnderManager.getAllEmployeesUnderManager(
                userId, page, limit, searchtag);
        List<GetUserResponse> responses = new ArrayList<>();

        for (List<String> userDto : users.getContent()) {
            if (userDto.size() >= 3) {
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
    public ResponseEntity<Map<String, Object>> getTotalUserCount(
            @PathVariable String userId,
            @PathVariable String searchtag,
            @RequestParam(value = "limit", defaultValue = "2") int limit) {

        long totalEmployees = listAllEmployeesUnderManagerDao.getTotalEmployeesCount(userId, searchtag);
        int totalPages = (int) Math.ceil((double) totalEmployees / limit);

        Map<String, Object> response = new HashMap<>();
        response.put("totalEmployees", totalEmployees);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    // Modified to use helper
    @GetMapping(value = "/events/{userid}/{frommonth}/{tomonth}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
        return helper.handleDownloadReport(userid, frommonth, tomonth);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/bymonth")
    public ResponseEntity<Map<String, Object>> getByMonthForManager(
            @RequestParam String monthYear,
            @RequestParam String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(monthBasedService.getAttendanceReportForManager(
                monthYear, userId, page, limit));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/bymonthreport")
    public ResponseEntity<byte[]> getByMonthandManagerid(
            @RequestParam String monthYear,
            @RequestParam String managerId) {
        try {
            byte[] excelFile = monthBasedService.generateAttendanceReport(monthYear, managerId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=Manager_Attendance_Report_" + monthYear + ".xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);
        } catch (Exception e) {
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }

    // Modified to use helper
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping(value = "/events/bymonthreportformanager",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEventsForMonth(
            @RequestParam String frommonth,
            @RequestParam String tomonth,
            @RequestParam String managerId) {
        return helper.handleStreamEventsForMonth(frommonth, tomonth, managerId);
    }

    // Modified to use helper
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/download/bymonthreportformanager")
    public ResponseEntity<Resource> downloadMonthReportForManager(
            @RequestParam String frommonth,
            @RequestParam String tomonth) {
        return helper.handleDownloadMonthReport(frommonth, tomonth);
    }
}