package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceControllerHelper {

    private final UseridAndMonthImpl useridAndMonthService;
    private final MonthBasedServiceImpl monthBasedService;

    // Common user detail methods
    public Map<String, Map<String, String>> handleGetUserDetails(String userId, String month) {
        return useridAndMonthService.getCustomerDetails(userId, month);
    }

    public Map<String, Map<String, String>> handleGetUserDetails(String userId) {
        return useridAndMonthService.getCustomerDetails(userId);
    }

    // Common report generation methods
    public SseEmitter handleStreamEvents(String userid, String fromMonth, String toMonth) {
        SseEmitter emitter = new SseEmitter(0L);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                byte[] excelData = useridAndMonthService.generateAttendanceExcel(userid, fromMonth, toMonth);
                ensureReportsDirectoryExists();

                String filePath = saveReportToFile(userid, fromMonth, toMonth, excelData);
                logReportGenerationTime(startTime, filePath);

                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete();
            } catch (IOException e) {
                handleStreamError(emitter, e);
            }
        });

        return emitter;
    }

    public ResponseEntity<Resource> handleDownloadReport(String userid, String fromMonth, String toMonth) {
        String filename = String.format("Attendance_%s_from_%s_to_%s.xlsx", userid, fromMonth, toMonth);
        String filePath = "reports/" + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(file);
        return createDownloadResponse(filename, resource);
    }

    // Common month report methods
    public SseEmitter handleStreamEventsForMonth(String fromMonth, String toMonth, String managerId) {
        validateMonthFormat(fromMonth, toMonth);

        SseEmitter emitter = new SseEmitter(0L);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                emitter.send(SseEmitter.event().data("Generating Excel..."));

                byte[] excelData = monthBasedService.generateAttendanceReports(fromMonth, toMonth, managerId);
                Path filePath = saveMonthReportToSecureLocation(fromMonth, toMonth, excelData);

                log.info("Report generated at: {}", filePath);
                emitter.send(SseEmitter.event().data("Report Ready"));
                emitter.complete();
            } catch (IOException | ParseException e) {
                handleStreamError(emitter, e);
            }
        });

        return emitter;
    }

    public ResponseEntity<Resource> handleDownloadMonthReport(String fromMonth, String toMonth) {
        validateMonthFormat(fromMonth, toMonth);

        String fileName = String.format("Attendance_from_%s_to_%s.xlsx", fromMonth, toMonth);
        Path filePath = getSecureReportPath(fromMonth, toMonth, fileName);

        if (!Files.exists(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(filePath.toFile());
        return createDownloadResponse(fileName, resource);
    }

    // Private helper methods
    private void ensureReportsDirectoryExists() throws IOException {
        Path reportsDir = Paths.get("reports");
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }
    }

    private String saveReportToFile(String userid, String fromMonth, String toMonth, byte[] excelData) throws IOException {
        String filePath = String.format("reports/Attendance_%s_from_%s_to_%s.xlsx", userid, fromMonth, toMonth);
        Files.write(Paths.get(filePath), excelData);
        return filePath;
    }

    private void logReportGenerationTime(long startTime, String filePath) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Report generation time: {} ms", duration);
        log.info("Report generated at: {}", filePath);
    }

    private void handleStreamError(SseEmitter emitter, Exception e) {
        try {
            emitter.send(SseEmitter.event().data("Error generating report"));
            emitter.completeWithError(e);
        } catch (IOException ex) {
            log.error("Error sending SSE event", ex);
        }
    }

    private void validateMonthFormat(String... months) {
        for (String month : months) {
            if (!month.matches("\\d{4}-\\d{2}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use YYYY-MM.");
            }
        }
    }

    private Path saveMonthReportToSecureLocation(String fromMonth, String toMonth, byte[] excelData) throws IOException {
        Path baseDirectory = Paths.get("secure-reports/monthreports").toAbsolutePath().normalize();
        Files.createDirectories(baseDirectory);

        String fileName = String.format("Attendance_from_%s_to_%s.xlsx", fromMonth, toMonth);
        Path filePath = baseDirectory.resolve(fileName).normalize();

        if (!filePath.startsWith(baseDirectory)) {
            throw new SecurityException("Invalid file path.");
        }

        Files.write(filePath, excelData);
        return filePath;
    }

    private Path getSecureReportPath(String fromMonth, String toMonth, String fileName) {
        Path baseDirectory = Paths.get("secure-reports/monthreports").toAbsolutePath().normalize();
        Path filePath = baseDirectory.resolve(fileName).normalize();

        if (!filePath.startsWith(baseDirectory)) {
            throw new SecurityException("Invalid file path.");
        }

        return filePath;
    }

    private ResponseEntity<Resource> createDownloadResponse(String filename, Resource resource) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}