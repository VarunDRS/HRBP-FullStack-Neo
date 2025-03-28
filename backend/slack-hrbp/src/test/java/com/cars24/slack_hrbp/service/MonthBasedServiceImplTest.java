package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
import com.cars24.slack_hrbp.data.Pair;
import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;

import org.apache.poi.sl.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class MonthBasedServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private MonthBasedDaoImpl monthBasedDao;

    @InjectMocks
    private MonthBasedServiceImpl monthBasedService;

    private List<AttendanceEntity> mockAttendanceList;
    private List<String> mockEmployeeIds;

    @BeforeEach
    public void setUp() {
        // Prepare mock data
        mockAttendanceList = createMockAttendanceList();
        mockEmployeeIds = Arrays.asList("emp1", "emp2");
    }

    private List<AttendanceEntity> createMockAttendanceList() {
        List<AttendanceEntity> attendanceList = new ArrayList<>();

        AttendanceEntity attendance1 = new AttendanceEntity();
        attendance1.setUserid("emp1");
        attendance1.setUsername("John Doe");
        attendance1.setDate("2024-01-15");
        attendance1.setType("Work From Home");

        AttendanceEntity attendance2 = new AttendanceEntity();
        attendance2.setUserid("emp2");
        attendance2.setUsername("Jane Smith");
        attendance2.setDate("2024-01-16");
        attendance2.setType("Planned Leave");

        attendanceList.add(attendance1);
        attendanceList.add(attendance2);

        return attendanceList;
    }

    @Test
    public void testGetRequestTypeCode() {
        assertEquals("W", monthBasedService.getRequestTypeCode("Work From Home"));
        assertEquals("P", monthBasedService.getRequestTypeCode("Planned Leave"));
        assertEquals("U", monthBasedService.getRequestTypeCode("Unplanned Leave"));
        assertEquals("S", monthBasedService.getRequestTypeCode("Sick Leave"));
        assertEquals("", monthBasedService.getRequestTypeCode(null));
        assertEquals("", monthBasedService.getRequestTypeCode("Unknown Type"));
    }

    @Test
    public void testProcessAttendanceData() throws ParseException {
        Map<String, Map<String, String>> result = monthBasedService.processAttendanceData(mockAttendanceList);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("John Doe"));
        assertTrue(result.containsKey("Jane Smith"));
    }

    @Test
    public void testFormatAttendanceData() {
        // Setup mock employee repository
        EmployeeEntity employee1 = new EmployeeEntity();
        employee1.setUserId("emp1");
        employee1.setUsername("John Doe");

        EmployeeEntity employee2 = new EmployeeEntity();
        employee2.setUserId("emp2");
        employee2.setUsername("Jane Smith");

        when(employeeRepository.findByUserId("emp1")).thenReturn(Optional.of(employee1));
        when(employeeRepository.findByUserId("emp2")).thenReturn(Optional.of(employee2));

        Map<Pair, Map<String, String>> result = monthBasedService.formatAttendanceData(
                mockAttendanceList,
                Arrays.asList("emp1", "emp2")
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetAttendanceReportForHR() {
        // Mock page of employees
        List<String> employeeIds = Arrays.asList("emp1", "emp2");
        Page<String> mockPage = new PageImpl<>(employeeIds, PageRequest.of(0, 10), 2);

        // Mock dependencies
        when(monthBasedDao.getPaginatedEmployeesForHr(0, 10)).thenReturn(mockPage);
        when(monthBasedDao.getAttendanceForEmployees(anyString(), anyList())).thenReturn(mockAttendanceList);

        Map<String, Object> result = monthBasedService.getAttendanceReportForHR("2024-01", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.get("currentPage"));
        assertEquals(10, result.get("pageSize"));
    }

    @Test
    public void testGetAttendanceReportForManager() {
        // Mock page of employees
        List<String> employeeIds = Arrays.asList("emp1", "emp2");
        Page<String> mockPage = new PageImpl<>(employeeIds, PageRequest.of(0, 10), 2);

        // Mock dependencies
        when(monthBasedDao.getPaginatedEmployeesForManager(anyString(), anyInt(), anyInt())).thenReturn(mockPage);
        when(monthBasedDao.getAttendanceForEmployees(anyString(), anyList())).thenReturn(mockAttendanceList);

        Map<String, Object> result = monthBasedService.getAttendanceReportForManager("2024-01", "manager1", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.get("currentPage"));
        assertEquals(10, result.get("pageSize"));
    }

    @Test
    public void testGenerateAttendanceReport() throws IOException, ParseException {
        // Mock employee ids
        List<String> employeeIds = Arrays.asList("emp1", "emp2");

        // Mock repository calls
        when(attendanceRepository.findByDateStartingWith(anyString())).thenReturn(mockAttendanceList);
        when(monthBasedDao.getAllEmployeesUnderManager(anyString())).thenReturn(employeeIds);

        // Mock employee repository
        EmployeeEntity employee1 = new EmployeeEntity();
        employee1.setUsername("John Doe");
        when(employeeRepository.findAllByUserId("emp1")).thenReturn(Collections.singletonList(employee1));

        // Generate report
        byte[] reportBytes = monthBasedService.generateAttendanceReport("2024-01", "manager1");

        assertNotNull(reportBytes);
        assertTrue(reportBytes.length > 0);
    }

    @Test
    public void testGenerateAttendanceReports() throws IOException, ParseException {
        // Mock report generation for multiple months
        when(monthBasedDao.getAllEmployeesUnderManager(anyString())).thenReturn(mockEmployeeIds);
        when(attendanceRepository.findByDateStartingWith(anyString())).thenReturn(mockAttendanceList);

        // Mock employee repository
        EmployeeEntity employee1 = new EmployeeEntity();
        employee1.setUsername("John Doe");
        when(employeeRepository.findAllByUserId("emp1")).thenReturn(Collections.singletonList(employee1));

        // Generate reports for multiple months
        byte[] combinedReportBytes = monthBasedService.generateAttendanceReports("2024-01", "2024-03", "manager1");

        assertNotNull(combinedReportBytes);
        assertTrue(combinedReportBytes.length > 0);
    }

    // Adding edge case tests
    @Test
    public void testGetRequestTypeCodeEdgeCases() {
        assertEquals("", monthBasedService.getRequestTypeCode(""));
        assertEquals("P*", monthBasedService.getRequestTypeCode("Planned Leave (Second Half)"));
        assertEquals("P**", monthBasedService.getRequestTypeCode("Planned Leave (First Half)"));
        assertEquals("T", monthBasedService.getRequestTypeCode("Travelling to HQ"));
        assertEquals("H", monthBasedService.getRequestTypeCode("Holiday"));
    }

}