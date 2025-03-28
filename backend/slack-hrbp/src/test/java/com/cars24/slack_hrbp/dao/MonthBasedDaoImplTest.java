package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthBasedDaoImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private MonthBasedDaoImpl monthBasedDao;

    private static final String MANAGER_ID = "manager123";
    private static final String MONTH_YEAR = "2024-03";
    private static final int PAGE = 0;
    private static final int LIMIT = 10;

    @Test
    void testGetAllEmployeesUnderManager_Success() {
        // Prepare mock employees
        List<EmployeeEntity> mockEmployees = Arrays.asList(
                createMockEmployeeEntity("user1"),
                createMockEmployeeEntity("user2")
        );

        when(employeeRepository.findByManagerId(MANAGER_ID)).thenReturn(mockEmployees);

        // Execute
        List<String> result = monthBasedDao.getAllEmployeesUnderManager(MANAGER_ID);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("user1"));
        assertTrue(result.contains("user2"));
        verify(employeeRepository).findByManagerId(MANAGER_ID);
    }

    @Test
    void testGetAttendanceForEmployees_Success() {
        // Prepare
        List<String> userIds = Arrays.asList("user1", "user2");
        List<AttendanceEntity> mockAttendance = new ArrayList<>();
        AttendanceEntity attendance1 = new AttendanceEntity();
        attendance1.setUsername("user1");
        mockAttendance.add(attendance1);

        when(attendanceRepository.findByDateStartingWithAndUseridIn(MONTH_YEAR, userIds))
                .thenReturn(mockAttendance);

        // Execute
        List<AttendanceEntity> result = monthBasedDao.getAttendanceForEmployees(MONTH_YEAR, userIds);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    void testGenerateAttendanceReport_Success() throws IOException, ParseException {
        // Prepare
        List<AttendanceEntity> mockAttendance = new ArrayList<>();

        AttendanceEntity attendance1 = createMockAttendanceEntity("user1", "2024-03-15", "Work From Home");
        AttendanceEntity attendance2 = createMockAttendanceEntity("user1", "2024-03-16", "Planned Leave");
        mockAttendance.add(attendance1);
        mockAttendance.add(attendance2);

        when(attendanceRepository.findByDateStartingWith(MONTH_YEAR))
                .thenReturn(mockAttendance);

        // Execute
        Map<String, Map<String, String>> result = monthBasedDao.generateAttendanceReport(MONTH_YEAR);

        // Verify
        assertNotNull(result);
        assertTrue(result.containsKey("user1"));
        Map<String, String> userData = result.get("user1");
        assertEquals("1", userData.get("Total WFH"));
        assertEquals("1", userData.get("Total Leaves"));
    }

    @Test
    void testGetPaginatedEmployees_Success() {
        // Prepare
        List<EmployeeEntity> mockEmployees = Arrays.asList(
                createMockEmployeeEntity("user1"),
                createMockEmployeeEntity("user2")
        );

        Page<EmployeeEntity> mockPage = new PageImpl<>(mockEmployees, PageRequest.of(PAGE, LIMIT), mockEmployees.size());
        when(employeeRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        // Execute
        Page<String> result = monthBasedDao.getPaginatedEmployees(MONTH_YEAR, PAGE, LIMIT);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains("user1"));
        assertTrue(result.getContent().contains("user2"));
    }

    // Helper methods to create mock objects
    private EmployeeEntity createMockEmployeeEntity(String userId) {
        EmployeeEntity employee = mock(EmployeeEntity.class);
        when(employee.getUserId()).thenReturn(userId);
        return employee;
    }

    private AttendanceEntity createMockAttendanceEntity(String username, String date, String type) {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setUsername(username);
        attendance.setDate(date);
        attendance.setType(type);
        return attendance;
    }
}