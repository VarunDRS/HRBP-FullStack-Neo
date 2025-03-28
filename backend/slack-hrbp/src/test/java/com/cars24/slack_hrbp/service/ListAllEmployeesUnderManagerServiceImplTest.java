package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.service.impl.ListAllEmployeesUnderManagerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListAllEmployeesUnderManagerServiceImplTest {

    @Mock
    private ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;

    @InjectMocks
    private ListAllEmployeesUnderManagerServiceImpl service;

    private Page<List<String>> mockPage;
    private String userId;
    private int page;
    private int limit;
    private String searchTag;

    @BeforeEach
    public void setUp() {
        userId = "manager123";
        page = 0;
        limit = 10;
        searchTag = "engineering";

        // Create a mock Page to simulate the DAO response
        List<String> employeeNames = Arrays.asList("John Doe", "Jane Smith");
        mockPage = new PageImpl<>(Arrays.asList(employeeNames));
    }

    @Test
    public void testGetAllEmployeesUnderManager_WithValidParameters() {
        // Arrange
        when(listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(userId, page, limit, searchTag))
                .thenReturn(mockPage);

        // Act
        Page<List<String>> result = service.getAllEmployeesUnderManager(userId, page, limit, searchTag);

        // Assert
        assertEquals(mockPage, result);

        // Verify that the DAO method was called with correct parameters
        verify(listAllEmployeesUnderManagerDao)
                .getAllEmployeesUnderManager(userId, page, limit, searchTag);
    }

    @Test
    public void testGetAllEmployeesUnderManager_WithEmptySearchTag() {
        // Arrange
        String emptySearchTag = "";
        when(listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(userId, page, limit, emptySearchTag))
                .thenReturn(mockPage);

        // Act
        Page<List<String>> result = service.getAllEmployeesUnderManager(userId, page, limit, emptySearchTag);

        // Assert
        assertEquals(mockPage, result);

        // Verify that the DAO method was called with correct parameters
        verify(listAllEmployeesUnderManagerDao)
                .getAllEmployeesUnderManager(userId, page, limit, emptySearchTag);
    }

    @Test
    public void testGetAllEmployeesUnderManager_WithNullSearchTag() {
        // Arrange
        String nullSearchTag = null;
        when(listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(userId, page, limit, nullSearchTag))
                .thenReturn(mockPage);

        // Act
        Page<List<String>> result = service.getAllEmployeesUnderManager(userId, page, limit, nullSearchTag);

        // Assert
        assertEquals(mockPage, result);

        // Verify that the DAO method was called with correct parameters
        verify(listAllEmployeesUnderManagerDao)
                .getAllEmployeesUnderManager(userId, page, limit, nullSearchTag);
    }
}