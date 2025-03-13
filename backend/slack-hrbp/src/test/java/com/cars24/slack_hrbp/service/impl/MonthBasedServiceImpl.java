package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthBasedServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private MonthBasedServiceImpl monthBasedService;

    private List<AttendanceEntity> mockAttendanceList;

    @BeforeEach
    void setUp() {
        AttendanceEntity entry1 = new AttendanceEntity();
        entry1.setId("1");
        entry1.setUserid("user1");
        entry1.setUsername("John Doe");
        entry1.setDate("2024-02-01");
        entry1.setType("Planned Leave");

        AttendanceEntity entry2 = new AttendanceEntity();
        entry2.setId("2");
        entry2.setUserid("user1");
        entry2.setUsername("John Doe");
        entry2.setDate("2024-02-02");
        entry2.setType("Work From Home");

        AttendanceEntity entry3 = new AttendanceEntity();
        entry3.setId("3");
        entry3.setUserid("user2");
        entry3.setUsername("Jane Smith");
        entry3.setDate("2024-02-01");
        entry3.setType("Sick Leave");

        mockAttendanceList = Arrays.asList(entry1, entry2, entry3);
    }

    @Test
    void testGenerateAttendanceReport() throws IOException, ParseException {
        String monthYear = "2024-02";
        when(attendanceRepository.findByDateStartingWith(monthYear)).thenReturn(mockAttendanceList);

        Map<String, Map<String, String>> result = monthBasedService.generateAttendanceReport(monthYear);

        assertNotNull(result);
        assertEquals(2, result.size()); // Two unique users
        assertEquals("P", result.get("John Doe").get("Feb-01")); // Planned Leave
        assertEquals("W", result.get("John Doe").get("Feb-02")); // Work From Home
        assertEquals("S", result.get("Jane Smith").get("Feb-01")); // Sick Leave
    }

    @Test
    void testGenerateAttendanceReport_EmptyData() throws IOException, ParseException {
        when(attendanceRepository.findByDateStartingWith("2024-03")).thenReturn(List.of());

        Map<String, Map<String, String>> result = monthBasedService.generateAttendanceReport("2024-03");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateAttendanceReport_ExceptionHandling() throws IOException, ParseException {
        when(attendanceRepository.findByDateStartingWith(anyString())).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> monthBasedService.generateAttendanceReport("2024-02"));

        assertEquals("Database error", exception.getMessage());
    }
}
