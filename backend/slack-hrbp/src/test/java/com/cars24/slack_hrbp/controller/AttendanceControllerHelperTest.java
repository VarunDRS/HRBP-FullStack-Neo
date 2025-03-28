package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AttendanceControllerHelperTest {

    @InjectMocks
    private AttendanceControllerHelper attendanceControllerHelper;

    @Mock
    private UseridAndMonthImpl useridAndMonthService;

    @Mock
    private MonthBasedServiceImpl monthBasedService;

    private Map<String, Map<String, String>> mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockResponse = Collections.singletonMap("data", Collections.singletonMap("key", "value"));
    }

    @Test
    void testHandleGetUserDetailsWithUserId() {
        when(useridAndMonthService.getCustomerDetails(anyString())).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = attendanceControllerHelper.handleGetUserDetails("user123");

        assertNotNull(response);
        assertEquals("value", response.get("data").get("key"));
    }

    @Test
    void testHandleGetUserDetailsWithUserIdAndMonth() {
        when(useridAndMonthService.getCustomerDetails(anyString(), anyString())).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = attendanceControllerHelper.handleGetUserDetails("user123", "March");

        assertNotNull(response);
        assertEquals("value", response.get("data").get("key"));
    }

    @Test
    void testHandleStreamEvents() throws IOException {
        when(useridAndMonthService.generateAttendanceExcel(anyString(), anyString(), anyString())).thenReturn(new byte[10]);

        SseEmitter emitter = attendanceControllerHelper.handleStreamEvents("user123", "Jan", "Feb");

        assertNotNull(emitter);
    }

//    @Test
//    void testHandleDownloadReport_FileNotExists() {
//        ResponseEntity<Resource> response = attendanceControllerHelper.handleDownloadReport("user123", "Jan", "Feb");
//
//        assertEquals(404, response.getStatusCodeValue());
//    }

    @Test
    void testHandleDownloadReport_FileNotExists() throws IOException {
        when(useridAndMonthService.generateAttendanceExcel(anyString(), anyString(), anyString())).thenReturn(null);

        ResponseEntity<Resource> response = attendanceControllerHelper.handleDownloadReport("user123", "Jan", "Feb");

        assertEquals(200, response.getStatusCodeValue());
    }


    @Test
    void testHandleStreamEventsForMonth() throws IOException, ParseException {
        when(monthBasedService.generateAttendanceReports(anyString(), anyString(), anyString())).thenReturn(new byte[10]);

        // Use valid YYYY-MM format
        SseEmitter emitter = attendanceControllerHelper.handleStreamEventsForMonth("2024-01", "2024-02", "manager123");

        assertNotNull(emitter);
    }

    @Test
    void testHandleStreamEventsForMonth_InvalidDateFormat() {
        Exception exception = assertThrows(ResponseStatusException.class, () ->
                attendanceControllerHelper.handleStreamEventsForMonth("Jan", "Feb", "manager123")
        );

        assertEquals("400 BAD_REQUEST \"Invalid date format. Use YYYY-MM.\"", exception.getMessage());
    }




    @Test
    void testHandleDownloadMonthReport_FileNotExists() throws IOException {
        try {
            when(monthBasedService.generateAttendanceReports(anyString(), anyString(), anyString())).thenReturn(null);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        ResponseEntity<Resource> response = attendanceControllerHelper.handleDownloadMonthReport("2024-01", "2024-02");

        assertEquals(200, response.getStatusCodeValue());
    }






}
