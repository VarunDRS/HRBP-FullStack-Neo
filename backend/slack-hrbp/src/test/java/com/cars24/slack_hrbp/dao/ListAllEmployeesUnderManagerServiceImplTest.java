package com.cars24.slack_hrbp.dao;

import com.cars24.slack_hrbp.data.dao.impl.ListAllEmployeesUnderManagerDaoImpl;
import com.cars24.slack_hrbp.service.impl.ListAllEmployeesUnderManagerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAllEmployeesUnderManagerServiceImplTest {

    @Mock
    private ListAllEmployeesUnderManagerDaoImpl listAllEmployeesUnderManagerDao;

    @InjectMocks
    private ListAllEmployeesUnderManagerServiceImpl service;

    private static final String MANAGER_ID = "manager123";
    private static final String SEARCH_TAG = "active";
    private static final int PAGE = 0;
    private static final int LIMIT = 10;

    private Page mockEmployeesPage;

    @BeforeEach
    void setUp() {
        // Create a mock Page of employee lists
        mockEmployeesPage = mock(Page.class);
    }

    @Test
    void testGetAllEmployeesUnderManager_Success() {
        // Prepare
        when(listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(MANAGER_ID, PAGE, LIMIT, SEARCH_TAG))
                .thenReturn(mockEmployeesPage);

        // Execute
        Page<List<String>> result = service.getAllEmployeesUnderManager(MANAGER_ID, PAGE, LIMIT, SEARCH_TAG);

        // Verify
        assertNotNull(result);
        assertEquals(mockEmployeesPage, result);
        verify(listAllEmployeesUnderManagerDao).getAllEmployeesUnderManager(MANAGER_ID, PAGE, LIMIT, SEARCH_TAG);
    }

    @Test
    void testGetAllEmployeesUnderManager_EmptyResults() {
        // Prepare
        Page<List<String>> emptyPage = mock(Page.class);
        when(listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(MANAGER_ID, PAGE, LIMIT, SEARCH_TAG))
                .thenReturn(emptyPage);

        // Execute
        Page<List<String>> result = service.getAllEmployeesUnderManager(MANAGER_ID, PAGE, LIMIT, SEARCH_TAG);

        // Verify
        assertNotNull(result);
        verify(listAllEmployeesUnderManagerDao).getAllEmployeesUnderManager(MANAGER_ID, PAGE, LIMIT, SEARCH_TAG);
    }

    @Test
    void testGetAllEmployeesUnderManager_DifferentParameters() {
        // Prepare different parameters
        String differentManagerId = "manager456";
        int differentPage = 1;
        int differentLimit = 20;
        String differentSearchTag = "inactive";

        Page<List<String>> differentPage_result = mock(Page.class);
        when(listAllEmployeesUnderManagerDao.getAllEmployeesUnderManager(
                differentManagerId, differentPage, differentLimit, differentSearchTag))
                .thenReturn(differentPage_result);

        // Execute
        Page<List<String>> result = service.getAllEmployeesUnderManager(
                differentManagerId, differentPage, differentLimit, differentSearchTag);

        // Verify
        assertNotNull(result);
        assertEquals(differentPage_result, result);
        verify(listAllEmployeesUnderManagerDao).getAllEmployeesUnderManager(
                differentManagerId, differentPage, differentLimit, differentSearchTag);
    }
}