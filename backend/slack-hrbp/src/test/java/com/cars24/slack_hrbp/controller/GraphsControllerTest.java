package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.response.GraphResponse;
import com.cars24.slack_hrbp.service.impl.GraphServicesImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphsControllerTest {

    @Mock
    private GraphServicesImpl graphServices;

    @InjectMocks
    private GraphsController graphsController;

    @Test
    void testGetGraph() {
        // Arrange
        String userid = "user123";
        String month = "Feb-2025";
        GraphResponse mockResponse = new GraphResponse(); // Assuming GraphResponse is a simple DTO

        when(graphServices.getGraph(userid, month)).thenReturn(mockResponse);

        // Act
        ResponseEntity<GraphResponse> response = graphsController.getGraph(userid, month);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
        verify(graphServices, times(1)).getGraph(userid, month);
    }
}
