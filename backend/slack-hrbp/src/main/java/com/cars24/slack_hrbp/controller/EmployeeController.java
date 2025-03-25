package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.service.impl.EmployeeServiceImpl;
import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/employee")
@Slf4j
@RestController

public class EmployeeController {

    private final UseridAndMonthImpl usernameService;
    private final EmployeeServiceImpl employeeService;
    private final UseridAndMonthImpl useridandmonth;

    private static final Logger log = LoggerFactory.getLogger(HrController.class);
    private final List<Long> individualResponseTimes = new CopyOnWriteArrayList<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{userid}/{month}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userid, @PathVariable String month){
        return useridandmonth.getCustomerDetails(userid,month);
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{userId}")
    public Map<String, Map<String, String>> getUserDetails(@PathVariable String userId){

        log.info("GetUserDetails Manager called");
        return useridandmonth.getCustomerDetails(userId);

    }

    @GetMapping(value = "/events/{userid}/{frommonth}/{tomonth}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String userid, @PathVariable String frommonth, @PathVariable String tomonth) {
        SseEmitter emitter = new SseEmitter(0L);

        executor.execute(() -> {
            long startTime = System.currentTimeMillis(); // Record start time

            try {
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                // Simulate report generation delay (replace with actual logic)
                byte[] excelData = useridandmonth.generateAttendanceExcel(userid, frommonth, tomonth);

                String directoryPath = "reports";
                File reportsDir = new File(directoryPath);

                if (!reportsDir.exists()) {
                    reportsDir.mkdirs();
                }

                String filePath = "reports/Attendance_" + userid + "_from_" + frommonth + "_to_" + tomonth + ".xlsx";
                Files.write(Paths.get(filePath), excelData);

                long duration = System.currentTimeMillis() - startTime;
                individualResponseTimes.add(duration);
                log.info("Individual Report generated in {} ms", duration);


                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete();
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

    // 🔹 Endpoint to Get P90 Latency for Individual Reports
    @GetMapping("/metrics/p90-latency/individual")
    public ResponseEntity<String> getP90LatencyForIndividual() {
        if (individualResponseTimes.isEmpty()) {
            return ResponseEntity.ok("No data available for individual reports.");
        }

        long p90Latency = calculateP90(individualResponseTimes);
        return ResponseEntity.ok("P90 Latency for Individual Reports: " + p90Latency + " ms");
    }

    // 🔹 P90 Calculation Function
    private long calculateP90(List<Long> responseTimes) {
        if (responseTimes.isEmpty()) {
            return 0;
        }

        List<Long> sortedTimes = responseTimes.stream()
                .sorted()
                .skip(Math.max(0, responseTimes.size() - 100)) // Keep last 100
                .collect(Collectors.toList());

        log.info("Stored response times: {}", sortedTimes);

        int index = (int) Math.ceil(0.90 * sortedTimes.size()) - 1;
        index = Math.max(index, 0);  // Ensure index is not negative
        return sortedTimes.get(index);
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



}
