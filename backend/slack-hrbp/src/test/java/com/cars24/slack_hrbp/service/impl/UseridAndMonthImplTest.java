package com.cars24.slack_hrbp.service.impl;

import com.cars24.slack_hrbp.data.dao.impl.UseridAndMonthDaoImpl;
import com.cars24.slack_hrbp.service.UseridAndMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseridAndMonthImplTest {

    @Mock
    private UseridAndMonthDaoImpl useridAndMonthDao;

    @InjectMocks
    private UseridAndMonthImpl useridAndMonthService;

    private Map<String, Map<String, String>> mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = Map.of(
                "John Doe", Map.of(
                        "Feb-10", "P",
                        "Feb-15", "S"
                )
        );
    }

    @Test
    void testGetCustomerDetails_ByUserId() {
        String userId = "123";

        when(useridAndMonthDao.getUserDetails(userId)).thenReturn(mockResponse);

        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("John Doe"));
        assertEquals("P", result.get("John Doe").get("Feb-10"));
        assertEquals("S", result.get("John Doe").get("Feb-15"));

        verify(useridAndMonthDao, times(1)).getUserDetails(userId);
    }

    @Test
    void testGetCustomerDetails_ByUserIdAndMonth() {
        String userId = "123";
        String month = "Feb";

        when(useridAndMonthDao.getUserDetails(userId, month)).thenReturn(mockResponse);

        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails(userId, month);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("John Doe"));
        assertEquals("P", result.get("John Doe").get("Feb-10"));
        assertEquals("S", result.get("John Doe").get("Feb-15"));

        verify(useridAndMonthDao, times(1)).getUserDetails(userId, month);
    }

    @Test
    void testGetCustomerDetails_EmptyResponse() {
        String userId = "999";

        when(useridAndMonthDao.getUserDetails(userId)).thenReturn(Collections.emptyMap());

        Map<String, Map<String, String>> result = useridAndMonthService.getCustomerDetails(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(useridAndMonthDao, times(1)).getUserDetails(userId);
    }
}
