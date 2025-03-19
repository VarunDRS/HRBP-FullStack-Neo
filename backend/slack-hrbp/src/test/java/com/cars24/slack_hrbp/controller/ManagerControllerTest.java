package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
        import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.*;

        import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ManagerControllerTest {

    @Mock
    private MonthBasedServiceImpl monthBasedService;

    @Mock
    private UseridAndMonthImpl useridAndMonth;

    @Mock
    private ListAllEmployeesUnderManagerServiceImpl listAllEmployeesUnderManager;

    @Mock
    private ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;

    @InjectMocks
    private ManagerController managerController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testGetUserDetailsWithMonth() {
        // Arrange
        String userId = "123";
        String month = "2023-10";
        Map<String, Map<String, String>> expectedResponse = new HashMap<>();
        when(useridAndMonth.getCustomerDetails(userId, month)).thenReturn(expectedResponse);

        // Act
        Map<String, Map<String, String>> response = managerController.getUserDetails(userId, month);

        // Assert
        assertEquals(expectedResponse, response);
        verify(useridAndMonth, times(1)).getCustomerDetails(userId, month);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testGetUserDetailsWithoutMonth() {
        // Arrange
        String userId = "123";
        Map<String, Map<String, String>> expectedResponse = new HashMap<>();
        when(useridAndMonth.getCustomerDetails(userId)).thenReturn(expectedResponse);

        // Act
        Map<String, Map<String, String>> response = managerController.getUserDetails(userId);

        // Assert
        assertEquals(expectedResponse, response);
        verify(useridAndMonth, times(1)).getCustomerDetails(userId);
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    public void testGetTotalUserCount() {
        // Arrange
        String userId = "123";
        String searchTag = "test";
        int limit = 2;
        long totalEmployees = 10;
        int totalPages = 5;
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("totalEmployees", totalEmployees);
        expectedResponse.put("totalPages", totalPages);
        when(listAllEmployeesUnderManagerDao.getTotalEmployeesCount(userId, searchTag)).thenReturn(totalEmployees);

        // Act
        ResponseEntity<Map<String, Object>> response = managerController.getTotalUserCount(userId, searchTag, limit);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(listAllEmployeesUnderManagerDao, times(1)).getTotalEmployeesCount(userId, searchTag);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testGetByMonthForManager() {
        // Arrange
        String monthYear = "2023-10";
        String userId = "123";
        int page = 1;
        int limit = 5;
        Map<String, Object> expectedResponse = new HashMap<>();
        when(monthBasedService.getAttendanceReportForManager(monthYear, userId, page, limit)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<Map<String, Object>> response = managerController.getByMonthForManager(monthYear, userId, page, limit);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(monthBasedService, times(1)).getAttendanceReportForManager(monthYear, userId, page, limit);
    }
}

