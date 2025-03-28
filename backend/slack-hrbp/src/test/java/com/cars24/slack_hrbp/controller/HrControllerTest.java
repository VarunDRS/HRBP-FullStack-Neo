package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.request.*;
import com.cars24.slack_hrbp.data.response.GetUserResponse;
import com.cars24.slack_hrbp.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrControllerTest {

    @Mock
    private HrServiceImpl hrService;

    @Mock
    private MonthBasedServiceImpl monthBasedService;

    @Mock
    private AttendanceControllerHelper helper;

    @InjectMocks
    private HrController hrController;

    @BeforeEach
    void setUp() {
        // Setup any necessary initial configurations
    }

    @Test
    @WithMockUser(roles = "HR")
    void testCreateUser() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        EmployeeEntity mockEmployee = new EmployeeEntity();
        when(hrService.createEmployee(request)).thenReturn(mockEmployee);

        ResponseEntity<String> response = hrController.createUser(request);
        assertEquals("Creation was successful", response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateManager_Success() {
        doNothing().when(hrService).updateManager("user1", "manager1");
        ResponseEntity<String> response = hrController.updateManager("user1", "manager1");
        assertEquals("Manager updated successfully", response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateManager_Failure() {
        doThrow(new IllegalArgumentException("Invalid user ID"))
                .when(hrService).updateManager("user1", "manager1");
        ResponseEntity<String> response = hrController.updateManager("user1", "manager1");
        assertEquals("Invalid user ID", response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testUpdateRole() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        when(hrService.updateUser(request)).thenReturn("Role updated");
        ResponseEntity<String> response = hrController.updateRole(request);
        assertEquals("Role updated", response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testDeleteEntry() {
        when(hrService.deleteEntry("user1", "2024-07-01")).thenReturn("Entry deleted");
        ResponseEntity<String> response = hrController.deleteEntry("user1", "2024-07-01");
        assertEquals("Entry deleted", response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testAddEntry() {
        AddLeaveRequest request = new AddLeaveRequest();
        request.setDate("2024-07-01");
        request.setLeaveType("Sick Leave");
        request.setReason("Flu");
        when(hrService.addEntry("user1", "2024-07-01", "Sick Leave", "Flu")).thenReturn("Leave added");
        ResponseEntity<String> response = hrController.addEntry("user1", request);
        assertEquals("Leave added", response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetAllUsers() {
        Page<List<String>> mockPage = new PageImpl<>(Collections.singletonList(Arrays.asList("1", "test@mail.com", "JohnDoe")));
        when(hrService.getAllUsers("user1", 0, 2, "test")).thenReturn(mockPage);
        ResponseEntity<List<GetUserResponse>> response = hrController.getAllUsers("user1", "test", 1, 2);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetTotalUserCount() {
        when(hrService.getTotalEmployeesCount("test")).thenReturn(10L);
        ResponseEntity<Map<String, Object>> response = hrController.getTotalUserCount("user1", "test", 2);
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().get("totalEmployees"));
        assertEquals(5, response.getBody().get("totalPages"));
    }

    @Test
    void testStreamEvents() {
        SseEmitter emitter = new SseEmitter();
        when(helper.handleStreamEvents("user1", "07", "08")).thenReturn(emitter);
        assertEquals(emitter, hrController.streamEvents("user1", "07", "08"));
    }

    @Test
    void testDownloadReport() {
        Resource mockResource = mock(Resource.class);
        when(helper.handleDownloadReport("user1", "07", "08")).thenReturn(ResponseEntity.ok(mockResource));
        assertEquals(ResponseEntity.ok(mockResource), hrController.downloadReport("user1", "07", "08"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetByMonth() {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("employees", List.of("John", "Doe"));
        when(monthBasedService.getAttendanceReportForHR("07-2024", 1, 5)).thenReturn(mockData);
        ResponseEntity<Map<String, Object>> response = hrController.getByMonth("07-2024", 1, 5);
        assertEquals(mockData, response.getBody());
    }

    @Test
    @WithMockUser(roles = "HR")
    void testGetByMonthReport() throws IOException, ParseException {
        byte[] mockFile = new byte[]{1, 2, 3};
        when(monthBasedService.generateAttendanceReport("07-2024", "manager1")).thenReturn(mockFile);
        ResponseEntity<byte[]> response = hrController.getByMonth("07-2024", "manager1");
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().length);
    }



    @Test
    void testStreamEventsForMonth() {
        String fromMonth = "2023-01";
        String toMonth = "2023-02";
        String managerId = "M123";
        SseEmitter emitter = new SseEmitter();
        when(helper.handleStreamEventsForMonth(fromMonth, toMonth, managerId)).thenReturn(emitter);

        SseEmitter response = hrController.streamEventsForMonth(fromMonth, toMonth, managerId);

        assertNotNull(response);
        verify(helper, times(1)).handleStreamEventsForMonth(fromMonth, toMonth, managerId);
    }

    @Test
    void testGetUserDetails_WithMonth() {
        String userId = "U123";
        String month = "2023-01";
        Map<String, Map<String, String>> mockResponse = new HashMap<>();
        when(helper.handleGetUserDetails(userId, month)).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = hrController.getUserDetails(userId, month);

        assertNotNull(response);
        verify(helper, times(1)).handleGetUserDetails(userId, month);
    }

    @Test
    void testGetUserDetails_WithoutMonth() {
        String userId = "U123";
        Map<String, Map<String, String>> mockResponse = new HashMap<>();
        when(helper.handleGetUserDetails(userId)).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = hrController.getUserDetails(userId);

        assertNotNull(response);
        verify(helper, times(1)).handleGetUserDetails(userId);
    }

    @Test
    void testDownloadMonthReport() {
        String fromMonth = "2023-01";
        String toMonth = "2023-02";
        Resource mockResource = mock(Resource.class);
        when(helper.handleDownloadMonthReport(fromMonth, toMonth)).thenReturn(ResponseEntity.ok(mockResource));

        ResponseEntity<Resource> response = hrController.downloadMonthReport(fromMonth, toMonth);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(helper, times(1)).handleDownloadMonthReport(fromMonth, toMonth);
    }

}
