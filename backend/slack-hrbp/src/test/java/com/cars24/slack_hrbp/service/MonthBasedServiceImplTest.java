//package com.cars24.slack_hrbp.service;
//
//import com.cars24.slack_hrbp.data.Pair;
//import com.cars24.slack_hrbp.data.dao.impl.MonthBasedDaoImpl;
//import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
//import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
//import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
//import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
//import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class MonthBasedServiceImplTest {
//
//    @Mock
//    private AttendanceRepository attendanceRepository;
//
//    @Mock
//    private EmployeeRepository employeeRepository;
//
//    @Mock
//    private MonthBasedDaoImpl monthBasedDao;
//
//    @InjectMocks
//    private MonthBasedServiceImpl monthBasedService;
//
//    @Test
//    void generateAttendanceReport_ShouldReturnExcelBytes() throws IOException, ParseException {
//        // Arrange
//        String monthYear = "2023-10";
//        String managerId = "mgr123";
//
//        List<String> employeeIds = Arrays.asList("emp1", "emp2");
//        List<AttendanceEntity> mockAttendance = Arrays.asList(
//                createMockAttendance("emp1", "John Doe", "2023-10-01", "Work From Home")
//        );
//
//        when(monthBasedDao.getAllEmployeesUnderManager(managerId)).thenReturn(employeeIds);
//        when(attendanceRepository.findByDateStartingWith(monthYear)).thenReturn(mockAttendance);
//        when(employeeRepository.findAllByUserId(anyString())).thenReturn(Collections.emptyList());
//
//        // Act
//        byte[] result = monthBasedService.generateAttendanceReport(monthYear, managerId);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.length > 0);
//    }
//
//    @Test
//    void generateAttendanceReport_ShouldHandleEmptyAttendance() throws IOException, ParseException {
//        // Arrange
//        String monthYear = "2023-10";
//        String managerId = "mgr123";
//
//        when(monthBasedDao.getAllEmployeesUnderManager(managerId)).thenReturn(Collections.emptyList());
//
//        // Act
//        byte[] result = monthBasedService.generateAttendanceReport(monthYear, managerId);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.length > 0);
//    }
//
//    @Test
//    void getRequestTypeCode_ShouldReturnCorrectCodes() {
//        assertEquals("W", monthBasedService.getRequestTypeCode("Work From Home"));
//        assertEquals("W", monthBasedService.getRequestTypeCode("WFH"));
//        assertEquals("P", monthBasedService.getRequestTypeCode("Planned Leave"));
//        assertEquals("U", monthBasedService.getRequestTypeCode("Unplanned Leave"));
//        assertEquals("S", monthBasedService.getRequestTypeCode("Sick Leave"));
//        assertEquals("", monthBasedService.getRequestTypeCode("Unknown Type"));
//        assertEquals("", monthBasedService.getRequestTypeCode(null));
//    }
//
//    @Test
//    void getAttendanceReportForHR_ShouldReturnPaginatedData() {
//        // Arrange
//        String monthYear = "2023-10";
//        int page = 1;
//        int limit = 10;
//
//        List<String> employeeIds = Arrays.asList("emp1", "emp2");
//        Page<String> employeePage = new PageImpl<>(employeeIds, PageRequest.of(0, limit), 2);
//
//        when(monthBasedDao.getPaginatedEmployeesForHr(0, limit)).thenReturn(employeePage);
//        when(monthBasedDao.getAttendanceForEmployees(monthYear, employeeIds)).thenReturn(Collections.emptyList());
//
//        // Act
//        Map<String, Object> result = monthBasedService.getAttendanceReportForHR(monthYear, page, limit);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.get("currentPage"));
//        assertEquals(10, result.get("pageSize"));
//    }
//
//    @Test
//    void getAttendanceReportForManager_ShouldReturnPaginatedData() {
//        // Arrange
//        String monthYear = "2023-10";
//        String managerId = "mgr123";
//        int page = 1;
//        int limit = 10;
//
//        List<String> employeeIds = Arrays.asList("emp1");
//        Page<String> employeePage = new PageImpl<>(employeeIds, PageRequest.of(0, limit), 1);
//
//        when(monthBasedDao.getPaginatedEmployeesForManager(managerId, 0, limit)).thenReturn(employeePage);
//        when(monthBasedDao.getAttendanceForEmployees(monthYear, employeeIds)).thenReturn(Collections.emptyList());
//
//        // Act
//        Map<String, Object> result = monthBasedService.getAttendanceReportForManager(monthYear, managerId, page, limit);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.get("currentPage"));
//    }
//
//    @Test
//    void generateAttendanceReports_ShouldCombineMultipleMonths() throws IOException, ParseException {
//        // Arrange
//        String fromMonth = "2023-10";
//        String toMonth = "2023-12";
//        String managerId = "mgr123";
//
//        when(monthBasedDao.getAllEmployeesUnderManager(managerId)).thenReturn(Collections.emptyList());
//
//        // Act
//        byte[] result = monthBasedService.generateAttendanceReports(fromMonth, toMonth, managerId);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.length > 0);
//    }
//
//    private AttendanceEntity createMockAttendance(String userId, String username, String date, String type) {
//        AttendanceEntity attendance = mock(AttendanceEntity.class);
//        when(attendance.getUserid()).thenReturn(userId);
//        when(attendance.getUsername()).thenReturn(username);
//        when(attendance.getDate()).thenReturn(date);
//        when(attendance.getType()).thenReturn(type);
//        return attendance;
//    }
//}