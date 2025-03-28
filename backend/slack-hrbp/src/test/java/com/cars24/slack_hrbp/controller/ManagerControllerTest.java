package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.ListAllEmployeesUnderManagerServiceImpl;
import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private MonthBasedServiceImpl monthBasedService;

    @Mock
    private ListAllEmployeesUnderManagerServiceImpl listAllEmployeesUnderManager;

    @Mock
    private ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;

    @Mock
    private AttendanceControllerHelper helper;

    @InjectMocks
    private ManagerController managerController;

    private static final String MANAGER_ID = "manager123";
    private static final String USER_ID = "user123";
    private static final String MONTH = "2024-03";

    @Test
    void testGetUserDetails_WithMonth() {
        Map<String, Map<String, String>> mockResponse = mock(Map.class);
        when(helper.handleGetUserDetails(USER_ID, MONTH)).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = managerController.getUserDetails(USER_ID, MONTH);

        assertEquals(mockResponse, response);
        verify(helper).handleGetUserDetails(USER_ID, MONTH);
    }

    @Test
    void testGetUserDetails_WithoutMonth() {
        Map<String, Map<String, String>> mockResponse = mock(Map.class);
        when(helper.handleGetUserDetails(USER_ID)).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = managerController.getUserDetails(USER_ID);

        assertEquals(mockResponse, response);
        verify(helper).handleGetUserDetails(USER_ID);
    }

    @Test
    void testGetAllUsers() {
        String searchTag = "active";
        int page = 1;
        int limit = 2;

        // Prepare mock data
        List<String> user1 = Arrays.asList("user1", "user1@example.com", "John Doe");
        List<String> user2 = Arrays.asList("user2", "user2@example.com", "Jane Smith");
        Page<List<String>> mockUsersPage = new PageImpl<>(Arrays.asList(user1, user2));

        when(listAllEmployeesUnderManager.getAllEmployeesUnderManager(MANAGER_ID, 0, limit, searchTag))
                .thenReturn(mockUsersPage);

        ResponseEntity<List<GetUserResponse>> response = managerController.getAllUsers(MANAGER_ID, searchTag, page, limit);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        List<GetUserResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(2, responseBody.size());

        assertEquals("user1", responseBody.get(0).getUserId());
        assertEquals("user1@example.com", responseBody.get(0).getEmail());
        assertEquals("John Doe", responseBody.get(0).getUsername());

        assertEquals("user2", responseBody.get(1).getUserId());
        assertEquals("user2@example.com", responseBody.get(1).getEmail());
        assertEquals("Jane Smith", responseBody.get(1).getUsername());
    }

    @Test
    void testGetTotalUserCount() {
        String searchTag = "active";
        int limit = 2;
        long totalEmployees = 10;

        when(listAllEmployeesUnderManagerDao.getTotalEmployeesCount(MANAGER_ID, searchTag))
                .thenReturn(totalEmployees);

        ResponseEntity<Map<String, Object>> response = managerController.getTotalUserCount(MANAGER_ID, searchTag, limit);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(totalEmployees, responseBody.get("totalEmployees"));
        assertEquals(5, responseBody.get("totalPages"));
    }

    @Test
    void testStreamEvents() {
        String fromMonth = "2024-01";
        String toMonth = "2024-03";
        SseEmitter mockEmitter = mock(SseEmitter.class);

        when(helper.handleStreamEvents(USER_ID, fromMonth, toMonth)).thenReturn(mockEmitter);

        SseEmitter response = managerController.streamEvents(USER_ID, fromMonth, toMonth);

        assertEquals(mockEmitter, response);
        verify(helper).handleStreamEvents(USER_ID, fromMonth, toMonth);
    }

    @Test
    void testGetByMonthForManager() {
        Map<String, Object> mockResponse = mock(Map.class);
        int page = 1;
        int limit = 5;

        when(monthBasedService.getAttendanceReportForManager(MONTH, MANAGER_ID, page, limit))
                .thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = managerController.getByMonthForManager(MONTH, MANAGER_ID, page, limit);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void testGetByMonthAndManagerId_Success() throws IOException, ParseException {
        byte[] mockExcelFile = "Mock Excel Content".getBytes();

        when(monthBasedService.generateAttendanceReport(MONTH, MANAGER_ID))
                .thenReturn(mockExcelFile);

        ResponseEntity<byte[]> response = managerController.getByMonthandManagerid(MONTH, MANAGER_ID);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockExcelFile, response.getBody());

        // Verify headers
        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, headers.getContentType());
        String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisposition.contains("attachment; filename=Manager_Attendance_Report_" + MONTH + ".xlsx"));
    }

    @Test
    void testGetByMonthAndManagerId_Exception() throws IOException, ParseException {
        when(monthBasedService.generateAttendanceReport(MONTH, MANAGER_ID))
                .thenThrow(new RuntimeException("Test Exception"));

        assertThrows(RuntimeException.class, () -> {
            managerController.getByMonthandManagerid(MONTH, MANAGER_ID);
        });
    }

    @Test
    void testStreamEventsForMonth() {
        String fromMonth = "2024-01";
        String toMonth = "2024-03";
        SseEmitter mockEmitter = mock(SseEmitter.class);

        when(helper.handleStreamEventsForMonth(fromMonth, toMonth, MANAGER_ID)).thenReturn(mockEmitter);

        SseEmitter response = managerController.streamEventsForMonth(fromMonth, toMonth, MANAGER_ID);

        assertEquals(mockEmitter, response);
        verify(helper).handleStreamEventsForMonth(fromMonth, toMonth, MANAGER_ID);
    }
}