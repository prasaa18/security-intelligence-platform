package com.securityintel.controller;

import com.securityintel.dto.DashboardSummaryDto;
import com.securityintel.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    @DisplayName("Should return dashboard summary via GET /dashboard/summary")
    void shouldGetDashboardSummary() throws Exception {
        DashboardSummaryDto summaryDto = new DashboardSummaryDto();
        summaryDto.setTotalFindings(50);
        summaryDto.setUniqueFindings(42);
        summaryDto.setCritical(5);
        summaryDto.setP0(4);

        when(dashboardService.getDashboardSummary()).thenReturn(summaryDto);

        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFindings").value(50))
                .andExpect(jsonPath("$.uniqueFindings").value(42))
                .andExpect(jsonPath("$.critical").value(5))
                .andExpect(jsonPath("$.p0").value(4));
    }
}

