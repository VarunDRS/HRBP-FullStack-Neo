package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.data.request.PasswordVerificationRequest;
import com.cars24.slack_hrbp.service.UserService;
import com.cars24.slack_hrbp.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EmployeeServiceImpl employeeService;

    @InjectMocks
    private UserController userController;

    private EmployeeEntity employeeEntity;

    @BeforeEach
    void setUp() {
        employeeEntity = new EmployeeEntity();
        employeeEntity.setUserId("user123");
        employeeEntity.setUsername("John Doe");
        employeeEntity.setEncryptedPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhi8rFv6a8zvG1FPn/SyJK7yR8DaSgFG"); // Encrypted password for testing
    }

    @Test
    void testGetUserName_UserExists() {
        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.of(employeeEntity));

        ResponseEntity<String> response = userController.getUserName("user123");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John Doe", response.getBody());
    }

    @Test
    void testGetUserName_UserNotFound() {
        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.getUserName("user123");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testUpdatePassword_Success() {
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setUserId("user123");
        request.setNewPassword("newPassword123");

        when(employeeService.updatePassword(request)).thenReturn("Password updated successfully");

        ResponseEntity<?> response = userController.updatePassword(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(((java.util.Map<?, ?>) response.getBody()).containsKey("success"));
    }

    @Test
    void testVerifyPassword_CorrectPassword() {
        PasswordVerificationRequest request = new PasswordVerificationRequest();
        request.setUserId("user123");
        request.setPassword("correctPassword");

        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.of(employeeEntity));
        when(bCryptPasswordEncoder.matches("correctPassword", employeeEntity.getEncryptedPassword())).thenReturn(true);

        ResponseEntity<?> response = userController.verifyPassword(request, "Bearer sampleToken");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(((java.util.Map<?, ?>) response.getBody()).containsKey("success"));
    }

    @Test
    void testVerifyPassword_IncorrectPassword() {
        PasswordVerificationRequest request = new PasswordVerificationRequest();
        request.setUserId("user123");
        request.setPassword("wrongPassword");

        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.of(employeeEntity));
        when(bCryptPasswordEncoder.matches("wrongPassword", employeeEntity.getEncryptedPassword())).thenReturn(false);

        ResponseEntity<?> response = userController.verifyPassword(request, "Bearer sampleToken");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(((java.util.Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void testVerifyPassword_UserNotFound() {
        PasswordVerificationRequest request = new PasswordVerificationRequest();
        request.setUserId("user123");
        request.setPassword("somePassword");

        when(employeeRepository.findByUserId("user123")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.verifyPassword(request, "Bearer sampleToken");

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(((java.util.Map<?, ?>) response.getBody()).containsKey("error"));
    }
}
