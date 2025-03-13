package com.cars24.slack_hrbp.data.dao.impl;

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

@ExtendWith(MockitoExtension.class) // ✅ No need for @SpringBootTest
class UseridAndMonthDaoImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private UseridAndMonthDaoImpl useridAndMonthDao;

    private List<AttendanceEntity> mockAttendanceList;

    @BeforeEach
    void setUp() {
        mockAttendanceList = new ArrayList<>();

        AttendanceEntity entity1 = new AttendanceEntity();
        entity1.setUserid("123");
        entity1.setUsername("John Doe");
        entity1.setDate("2025-02-10"); // Format: YYYY-MM-DD
        entity1.setType("Planned Leave");

        AttendanceEntity entity2 = new AttendanceEntity();
        entity2.setUserid("123");
        entity2.setUsername("John Doe");
        entity2.setDate("2025-02-15");
        entity2.setType("Sick Leave");

        mockAttendanceList.add(entity1);
        mockAttendanceList.add(entity2);
    }

    @Test
    void testGetUserDetails() {
        when(attendanceRepository.findByUserid("123")).thenReturn(mockAttendanceList);

        System.out.println("Mock Data Passed to DAO: " + mockAttendanceList);

        Map<String, Map<String, String>> result = useridAndMonthDao.getUserDetails("123");

        System.out.println("Result Map: " + result); // Debugging Output

        assertNotNull(result);
        assertEquals(1, result.size()); // Should contain "John Doe"
        assertTrue(result.containsKey("John Doe"));

        Map<String, String> leaveMap = result.get("John Doe");
        assertEquals(2, leaveMap.size()); // Should have 2 leave records

        // ✅ Fix date format to match DAO transformation
        assertEquals("P", leaveMap.get("Feb-10")); // Correct key
        assertEquals("S", leaveMap.get("Feb-15"));

    }

    @Test
    void testGetUserDetails_EmptyResult() {
        when(attendanceRepository.findByUserid("999")).thenReturn(Collections.emptyList());

        Map<String, Map<String, String>> result = useridAndMonthDao.getUserDetails("999");

        System.out.println("Empty Result Map: " + result); // Debugging Output

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
