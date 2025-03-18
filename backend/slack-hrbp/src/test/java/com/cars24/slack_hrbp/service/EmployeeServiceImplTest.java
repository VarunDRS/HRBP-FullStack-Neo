package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dao.impl.EmployeeDaoImpl;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.data.request.PasswordUpdateRequest;
import com.cars24.slack_hrbp.excpetion.UserServiceException;
import com.cars24.slack_hrbp.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeDaoImpl employeeDao;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private PasswordUpdateRequest passwordUpdateRequest;

    @BeforeEach
    void setUp() {
        passwordUpdateRequest = new PasswordUpdateRequest();
        passwordUpdateRequest.setUserId("user123");
        passwordUpdateRequest.setNewPassword("newSecurePassword");
    }

    @Test
    void testUpdatePassword_Success() {
        when(employeeRepository.existsByUserId("user123")).thenReturn(true);
        when(employeeDao.updatePassword(passwordUpdateRequest)).thenReturn("Password updated successfully");

        String result = employeeService.updatePassword(passwordUpdateRequest);

        assertEquals("Password updated successfully", result);
        verify(employeeDao, times(1)).updatePassword(passwordUpdateRequest);
    }

    @Test
    void testUpdatePassword_UserNotFound() {
        when(employeeRepository.existsByUserId("user123")).thenReturn(false);

        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            employeeService.updatePassword(passwordUpdateRequest);
        });

        assertEquals("User not valid", exception.getMessage());
        verify(employeeDao, never()).updatePassword(any());
    }
}
