package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.EmployeeDaoImpl;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeDaoImplTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeDaoImpl employeeDao;

    private PasswordUpdateRequest passwordUpdateRequest;
    private EmployeeEntity employeeEntity;

    @BeforeEach
    void setUp() {
        passwordUpdateRequest = new PasswordUpdateRequest();
        passwordUpdateRequest.setUserId("user123");
        passwordUpdateRequest.setNewPassword("newPassword123");

        employeeEntity = new EmployeeEntity();
        employeeEntity.setUserId("user123");
        employeeEntity.setEmail("test@example.com");
    }

    @Test
    void testUpdatePassword_Success() {
        // Prepare
        when(employeeRepository.findByUserId("user123"))
                .thenReturn(Optional.of(employeeEntity));

        when(bCryptPasswordEncoder.encode(passwordUpdateRequest.getNewPassword()))
                .thenReturn("encodedNewPassword");

        // Execute
        String result = employeeDao.updatePassword(passwordUpdateRequest);

        // Verify
        assertEquals("Password updated successfully", result);
        verify(employeeRepository).findByUserId("user123");
        verify(bCryptPasswordEncoder).encode(passwordUpdateRequest.getNewPassword());
        verify(employeeRepository).updatePassword("user123", "encodedNewPassword");
    }

    @Test
    void testUpdatePassword_UserNotFound() {
        // Prepare
        when(employeeRepository.findByUserId("user123"))
                .thenReturn(Optional.empty());

        // Execute and Verify
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            employeeDao.updatePassword(passwordUpdateRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(employeeRepository).findByUserId("user123");
        verify(employeeRepository, never()).updatePassword(anyString(), anyString());
        verify(bCryptPasswordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdatePassword_NullUserId() {
        // Prepare
        passwordUpdateRequest.setUserId(null);

        verify(employeeRepository, never()).findByUserId(anyString());
        verify(employeeRepository, never()).updatePassword(anyString(), anyString());
        verify(bCryptPasswordEncoder, never()).encode(anyString());
    }
}