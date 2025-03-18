package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
import com.cars24.slack_hrbp.service.UseridAndMonth;
import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        Map<String, String> dateMap = new HashMap<>();
        dateMap.put("Mar-01", "Present");
        dateMap.put("Mar-02", "Absent");
        mockData.put("User123", dateMap);
    }

    @Test
    void testGetCustomerDetails_WithUserId() {
        when(useridAndMonthDao.getUserDetails("User123")).thenReturn(mockData);

        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails("User123");

        assertNotNull(result);
        assertEquals(mockData, result);
        verify(useridAndMonthDao, times(1)).getUserDetails("User123");
    }

    @Test
    void testGetCustomerDetails_WithUserIdAndMonth() {
        when(useridAndMonthDao.getUserDetails("User123", "March")).thenReturn(mockData);

        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails("User123", "March");

        assertNotNull(result);
        assertEquals(mockData, result);
        verify(useridAndMonthDao, times(1)).getUserDetails("User123", "March");
    }

    @Test
    void testGenerateAttendanceExcel() throws IOException {
        when(useridAndMonthDao.getUserDetails("User123", "March")).thenReturn(mockData);

        byte[] excelBytes = useridAndMonthService.generateAttendanceExcel("User123", "March");

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        Workbook workbook = new XSSFWorkbook();
        assertDoesNotThrow(() -> workbook.close());
        verify(useridAndMonthDao, times(1)).getUserDetails("User123", "March");
    }
}