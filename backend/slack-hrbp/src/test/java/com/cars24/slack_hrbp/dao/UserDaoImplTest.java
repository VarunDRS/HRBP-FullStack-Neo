package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.UserDaoImpl;
import com.cars24.slack_hrbp.data.dto.UserDto;
import com.cars24.slack_hrbp.data.entity.EmployeeEntity;
import com.cars24.slack_hrbp.data.repository.EmployeeRepository;
import com.cars24.slack_hrbp.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Utils utils;

    @InjectMocks
    private UserDaoImpl userDao;

    private EmployeeEntity mockEmployeeEntity;
    private static final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
        // Create a mock EmployeeEntity for testing
        mockEmployeeEntity = new EmployeeEntity();
        mockEmployeeEntity.setUserId(USER_ID);
        mockEmployeeEntity.setEmail("test@example.com");
        mockEmployeeEntity.setUsername("Test User");
    }

    @Test
    void testCreateUser_Success() {
        // Prepare
        when(employeeRepository.save(any(EmployeeEntity.class))).thenReturn(mockEmployeeEntity);

        // Execute
        UserDto result = userDao.createUser(mockEmployeeEntity);

        // Verify
        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertEquals("test@example.com", result.getEmail());
        verify(employeeRepository).save(mockEmployeeEntity);
    }

    @Test
    void testDisplayCustomer_UserExists() {
        // Prepare
        when(employeeRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockEmployeeEntity));

        // Execute
        UserDto result = userDao.displayCustomer(USER_ID);

        // Verify
        assertNotNull(result);
    }

    @Test
    void testDisplayCustomer_UserNotFound() {
        // Prepare
        when(employeeRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Execute
        UserDto result = userDao.displayCustomer(USER_ID);

        // Verify
        assertNotNull(result);
        verify(employeeRepository).findByUserId(USER_ID);
    }

    @Test
    void testDeleteUser_Success() {
        // Prepare
        when(employeeRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockEmployeeEntity));
        doNothing().when(employeeRepository).deleteByUserId(USER_ID);

        // Execute
        UserDto result = userDao.deleteUser(USER_ID);

        // Verify
        assertNotNull(result);
        verify(employeeRepository).findByUserId(USER_ID);
        verify(employeeRepository).deleteByUserId(USER_ID);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Prepare
        when(employeeRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Execute
        UserDto result = userDao.deleteUser(USER_ID);

        // Verify
        assertNotNull(result);
        verify(employeeRepository).findByUserId(USER_ID);
        verify(employeeRepository).deleteByUserId(USER_ID);
    }

    // Additional test to verify exception handling or edge cases can be added here
}