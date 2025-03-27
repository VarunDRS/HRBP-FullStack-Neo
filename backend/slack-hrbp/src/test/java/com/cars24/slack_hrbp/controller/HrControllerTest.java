//package com.cars24.slack_hrbp.controller;
//
//import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
//import com.cars24.slack_hrbp.data.request.EmployeeUpdateRequest;
//import com.cars24.slack_hrbp.service.impl.HrServiceImpl;
//import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
//import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.util.Collections;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//        import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.when;
//
//class HrControllerTest {
//
//    @InjectMocks
//    HrController hrController;
//
//    @Mock
//    HrServiceImpl hrService;
//
//    @Mock
//    MonthBasedServiceImpl monthBasedService;
//
//    @Mock
//    UseridAndMonthImpl useridandmonth;
//
//    Map<String, Map<String, String>> mockResponse;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.initMocks(this);
//        hrController = new HrController(hrService, monthBasedService, useridandmonth);
//        mockResponse = Collections.singletonMap("data", Collections.singletonMap("key", "value"));
//    }
//
//    @Test
//    void testCreateUser() {
//        when(hrService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(null);
//        CreateEmployeeRequest request = new CreateEmployeeRequest();
//        ResponseEntity<String> response = hrController.createUser(request);
//        assertEquals("Creation was successful", response.getBody());
//    }
//
//    @Test
//    void testUpdateManagerSuccess() {
//        ResponseEntity<String> response = hrController.updateManager("user123", "manager456");
//        assertEquals("Manager updated successfully", response.getBody());
//    }
//
//    @Test
//    void testUpdateManagerFailure() {
//        doThrow(new IllegalArgumentException("Invalid manager ID"))
//                .when(hrService).updateManager(anyString(), anyString());
//
//        ResponseEntity<String> response = hrController.updateManager("user123", "invalid_manager");
//
//        assertEquals("Invalid manager ID", response.getBody());
//    }
//
//
//    @Test
//    void testUpdateRole() {
//        when(hrService.updateUser(any(EmployeeUpdateRequest.class))).thenReturn("Role updated successfully");
//        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
//        ResponseEntity<String> response = hrController.updateRole(request);
//        assertEquals("Role updated successfully", response.getBody());
//    }
//
//    @Test
//    void testGetUserDetailsByUserIdAndMonth() {
//        when(useridandmonth.getCustomerDetails(anyString(), anyString())).thenReturn(mockResponse);
//        Map<String, Map<String, String>> response = hrController.getUserDetails("user123", "March");
//        assertNotNull(response);
//        assertEquals("value", response.get("data").get("key"));
//    }
//
//    @Test
//    void testGetByMonth() {
//        Map<String, Object> mockResponse = Collections.singletonMap("totalRecords", 10);
//
//        when(monthBasedService.getAttendanceReportForHR(anyString(), anyInt(), anyInt()))
//                .thenReturn(mockResponse);
//
//        ResponseEntity<Map<String, Object>> response = hrController.getByMonth("March-2024", 1, 5);
//
//        assertNotNull(response);
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(10, response.getBody().get("totalRecords"));
//    }
//
//}
