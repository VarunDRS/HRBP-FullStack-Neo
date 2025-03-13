package com.cars24.slack_hrbp.controllers;

import com.cars24.slack_hrbp.controller.HrController;
import com.cars24.slack_hrbp.data.request.CreateEmployeeRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class HrControllerTest {

    @InjectMocks
    HrController hrController;
    CreateEmployeeRequest createEmployeeRequest;

    @BeforeEach
    void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        createEmployeeRequest = new CreateEmployeeRequest();

        createEmployeeRequest.setEmail("abc@gmail.com");
        createEmployeeRequest.setRoles(List.of("HR"));
        createEmployeeRequest.setPassword("R4rathima");
        createEmployeeRequest.setUsername("abcdef");
        createEmployeeRequest.setUserId("UE12CS123");

    }

    @Test
    void emptyUserId() {
        createEmployeeRequest.setUserId("");

        assertThrows(NullPointerException.class,
                () -> {
                    hrController.createUser(createEmployeeRequest);
                }
        );
    }

}