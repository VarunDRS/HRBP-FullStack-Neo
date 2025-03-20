package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.service.impl.EmployeeServiceImpl;
import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@RequestMapping("/employee")
@Slf4j
@RestController

public class EmployeeController {

    private final UseridAndMonthImpl usernameService;
    private final EmployeeServiceImpl employeeService;
    private final UseridAndMonthImpl useridandmonth;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month){
        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userid,month);
        return resp;
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId){

        System.out.println("GetUserDetails Manager called");
        Map<String, Map<String, String>> resp = useridandmonth.getCustomerDetails(userId);
        System.out.println(resp);
        return resp;

    }

    @GetMapping(value = "/events/{userid}/{frommonth}/{tomonth}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String userid, @PathVariable String frommonth, @PathVariable String tomonth) {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Send "Generating Excel" message first
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                // Simulate report generation delay (replace with actual logic)
                Thread.sleep(5000); // Simulate processing delay

                byte[] excelData = useridandmonth.generateAttendanceExcel(userid, frommonth ,tomonth);

                String directoryPath = "reports";
                File reportsDir = new File(directoryPath);

                // Ensure the directory exists
                if (!reportsDir.exists()) {
                    reportsDir.mkdirs();  // Create the directory if it doesn't exist
                }
                String filePath = "reports/Attendance_" + userid + "_" + "from_" + frommonth + "_to_" + tomonth + ".xlsx";
                Files.write(Paths.get(filePath), excelData);
                System.out.println(" Report generated at: " + filePath);
                // Send "Download Ready" message
                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete(); // Close connection after sending
            } catch (IOException | InterruptedException e) {
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
    @GetMapping("/download/{userid}/{tomonth}")
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



}
