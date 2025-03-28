package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseridAndMonthImplTest {

    @Mock
    private UseridAndMonthDaoImpl useridAndMonthDao;

    @InjectMocks
    private UseridAndMonthImpl useridAndMonthService;

    private Map<String, Map<String, String>> mockData;

    @BeforeEach
    void setUp() {
        mockData = new HashMap<>();
        Map<String, String> monthData = new HashMap<>();
        monthData.put("2025-02-01", "Sick Leave");
        mockData.put("Feb-2025", monthData);
    }

    @Test
    void testGetCustomerDetails_WithUserId() {
        when(useridAndMonthDao.getUserDetails("123")).thenReturn(mockData);
        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails("123");
        assertNotNull(result);
        assertEquals("Sick Leave", result.get("Feb-2025").get("2025-02-01"));
        verify(useridAndMonthDao, times(1)).getUserDetails("123");
    }

    @Test
    void testGetCustomerDetails_WithUserIdAndMonth() {
        when(useridAndMonthDao.getUserDetails("123", "Feb-2025")).thenReturn(mockData);
        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails("123", "Feb-2025");
        assertNotNull(result);
        assertEquals("Sick Leave", result.get("Feb-2025").get("2025-02-01"));
        verify(useridAndMonthDao, times(1)).getUserDetails("123", "Feb-2025");
    }

    @Test
    void testGenerateAttendanceExcel() throws IOException {
        when(useridAndMonthDao.getUserDetails("123", "Jan-2025", "Mar-2025")).thenReturn(mockData);
        byte[] excelData = useridAndMonthService.generateAttendanceExcel("123", "Jan-2025", "Mar-2025");
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        verify(useridAndMonthDao, times(1)).getUserDetails("123", "Jan-2025", "Mar-2025");
    }

    @Test
    void testGenerateAttendanceExcel_WithMonth() throws IOException {
        when(useridAndMonthDao.getUserDetails("123", "Feb-2025")).thenReturn(mockData);
        byte[] excelData = useridAndMonthService.generateAttendanceExcel("123", "Feb-2025");
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        verify(useridAndMonthDao, times(1)).getUserDetails("123", "Feb-2025");
    }

}
