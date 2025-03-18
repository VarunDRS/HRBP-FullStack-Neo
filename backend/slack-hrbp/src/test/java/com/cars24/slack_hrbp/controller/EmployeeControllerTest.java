package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.service.impl.EmployeeServiceImpl;
import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class EmployeeControllerTest {

    @InjectMocks
    EmployeeController employeeController;

    @Mock
    UseridAndMonthImpl useridandmonth;

    @Mock
    EmployeeServiceImpl employeeService;

    Map<String, Map<String, String>> mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        employeeController = new EmployeeController(useridandmonth, employeeService, useridandmonth);
        mockResponse = Collections.singletonMap("data", Collections.singletonMap("key", "value"));
    }

    @Test
    void testGetUserDetails() {
        when(useridandmonth.getCustomerDetails(anyString())).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = employeeController.getUserDetails("user123");

        assertNotNull(response);
        assertEquals("value", response.get("data").get("key"));
    }

    @Test
    void testGetUserDetailsByUserIdAndMonth() {
        when(useridandmonth.getCustomerDetails(anyString(), anyString())).thenReturn(mockResponse);

        Map<String, Map<String, String>> response = employeeController.getUserDetails("user123", "March");

        assertNotNull(response);
        assertEquals("value", response.get("data").get("key"));
    }
}
