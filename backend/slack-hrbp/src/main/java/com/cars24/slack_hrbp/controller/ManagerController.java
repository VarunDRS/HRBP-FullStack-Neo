package com.cars24.slack_hrbp.controller;


import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
    @GetMapping("/bymonthreport")
    public ResponseEntity<byte[]> getByMonthandManagerid(
            @RequestParam String monthYear,
            @RequestParam String userId) {

        try {
            // Generate the report and Excel file
            byte[] excelFile = monthBasedService.generateAttendanceReportForManager(monthYear, userId);

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
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month){

        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userid,month);
        return resp;

    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId){
        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userId);
        return resp;
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


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/bymonth")
    public ResponseEntity<Map<String, Object>> getByMonthForManager(
            @RequestParam String monthYear,
            @RequestParam String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        return ResponseEntity.ok(monthBasedService.getAttendanceReportForManager(monthYear, userId, page, limit));
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


}
