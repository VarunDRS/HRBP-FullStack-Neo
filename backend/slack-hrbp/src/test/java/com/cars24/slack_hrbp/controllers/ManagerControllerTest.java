package com.cars24.slack_hrbp.controller;

import com.cars24.slack_hrbp.service.impl.MonthBasedServiceImpl;
import com.cars24.slack_hrbp.service.impl.UseridAndMonthImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)  // Use Mockito Extension
class ManagerControllerTest {

    private MockMvc mockMvc;

    @Mock  // Use @Mock instead of @MockBean
    private MonthBasedServiceImpl monthBasedService;

    @Mock  // Use @Mock instead of @MockBean
    private UseridAndMonthImpl useridAndMonth;

    @InjectMocks
    private ManagerController managerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(managerController).build();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetByMonth() throws Exception {
        String monthYear = "2024-02";
        Map<String, Map<String, String>> reportData = new HashMap<>();
        when(monthBasedService.generateAttendanceReport(monthYear)).thenReturn(reportData);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/bymonth")
                        .param("monthYear", monthYear)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetUserDetailsByUserIdAndMonth() throws Exception {
        String userId = "123";
        String month = "2024-02";
        Map<String, Map<String, String>> response = new HashMap<>();
        when(useridAndMonth.getCustomerDetails(userId, month)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/{userid}/{month}", userId, month)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetUserDetailsByUserId() throws Exception {
        String userId = "123";
        Map<String, Map<String, String>> response = new HashMap<>();
        when(useridAndMonth.getCustomerDetails(userId)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
