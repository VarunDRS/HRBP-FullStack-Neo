package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.GraphDaoImpl;
import com.cars24.slack_hrbp.data.entity.AttendanceEntity;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.response.GraphResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphDaoImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private GraphDaoImpl graphDao;

    @Test
    void testGetGraph() {
        // Arrange
        String userid = "user123";
        String month = "Feb-2025";

        AttendanceEntity entry1 = new AttendanceEntity();
        entry1.setUserid(userid);
        entry1.setType("Planned Leave");

        AttendanceEntity entry2 = new AttendanceEntity();
        entry2.setUserid(userid);
        entry2.setType("Sick Leave");

        List<AttendanceEntity> mockAttendanceList = Arrays.asList(entry1, entry2);

        when(attendanceRepository.findByUserid(userid)).thenReturn(mockAttendanceList);
        when(attendanceRepository.findByUseridAndDateStartingWith(userid, month)).thenReturn(mockAttendanceList);

        // Act
        GraphResponse response = graphDao.getGraph(userid, month);

        // Assert
        HashMap<String, Integer> expectedCounts = new HashMap<>();
        expectedCounts.put("Planned Leave", 1);
        expectedCounts.put("Sick Leave", 1);

        assertEquals(expectedCounts, response.getTypeCounts());
        verify(attendanceRepository, times(1)).findByUserid(userid);
        verify(attendanceRepository, times(1)).findByUseridAndDateStartingWith(userid, month);
    }
}
