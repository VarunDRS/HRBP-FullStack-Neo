package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.GraphDaoImpl;
import com.cars24.slack_hrbp.data.repository.AttendanceRepository;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.response.GraphResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GraphServicesImplTest {

    @InjectMocks
    private GraphServicesImpl graphServices;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GraphDaoImpl graphDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetGraph() {
        GraphResponse mockResponse = new GraphResponse(); // Create a mock response object
        when(graphDao.getGraph(anyString(), anyString())).thenReturn(mockResponse);

        GraphResponse response = graphServices.getGraph("user123", "2024-03");

        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(graphDao, times(1)).getGraph("user123", "2024-03");
    }
}
