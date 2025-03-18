package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseridAndMonthDaoImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private UseridAndMonthDaoImpl useridAndMonthDao;

    private List<AttendanceEntity> mockAttendanceData;

    @BeforeEach
    void setUp() {
        mockAttendanceData = new ArrayList<>();

        AttendanceEntity entry1 = new AttendanceEntity();
        entry1.setUserid("user123");
        entry1.setUsername("John Doe");
        entry1.setDate("2025-02-15");
        entry1.setType("Planned Leave");

        AttendanceEntity entry2 = new AttendanceEntity();
        entry2.setUserid("user123");
        entry2.setUsername("John Doe");
        entry2.setDate("2025-02-20");
        entry2.setType("Sick Leave");

        mockAttendanceData.add(entry1);
        mockAttendanceData.add(entry2);
    }

    @Test
    void testGetUserDetails_WithUserId() {
        when(attendanceRepository.findByUserid("user123")).thenReturn(mockAttendanceData);

        Map<String, Map<String, String>> result = useridAndMonthDao.getUserDetails("user123");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("John Doe"));
        assertEquals("P", result.get("John Doe").get("Feb-15"));
        assertEquals("S", result.get("John Doe").get("Feb-20"));
    }

    @Test
    void testGetUserDetails_WithInvalidUserId() {
        when(attendanceRepository.findByUserid("invalidUser")).thenReturn(Collections.emptyList());

        Map<String, Map<String, String>> result = useridAndMonthDao.getUserDetails("invalidUser");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserDetails_WithUserIdAndMonth() {
        when(attendanceRepository.findByUserid("user123")).thenReturn(mockAttendanceData);

        Map<String, Map<String, String>> result = useridAndMonthDao.getUserDetails("user123", "Feb-2025");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("John Doe"));
        assertEquals("P", result.get("John Doe").get("Feb-15"));
        assertEquals("S", result.get("John Doe").get("Feb-20"));
    }

    @Test
    void testGetUserDetails_WithNoMatchingMonth() {
        when(attendanceRepository.findByUserid("user123")).thenReturn(mockAttendanceData);

        Map<String, Map<String, String>> result = useridAndMonthDao.getUserDetails("user123", "Jan-2025");
        assertNotNull(result);
        assertTrue(result.containsKey("John Doe"));
        assertTrue(result.get("John Doe").isEmpty());
    }
}
