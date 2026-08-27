package com.securityintel.controller;

import com.securityintel.dto.SecurityFindingDto;
import com.securityintel.model.Priority;
import com.securityintel.model.Severity;
import com.securityintel.service.SecurityFindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FindingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SecurityFindingService securityFindingService;

    @InjectMocks
    private FindingController findingController;

    private SecurityFindingDto findingDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(findingController).build();

        findingDto = new SecurityFindingDto();
        findingDto.setId("finding-1");
        findingDto.setCve("CVE-2024-1234");
        findingDto.setServiceName("payment-service");
        findingDto.setSeverity(Severity.CRITICAL);
        findingDto.setPriority(Priority.P0);
        findingDto.setRiskScore(96.0);
    }

    @Test
    @DisplayName("Should get all findings via GET /findings")
    void shouldGetAllFindings() throws Exception {
        when(securityFindingService.getAllFindings()).thenReturn(List.of(findingDto));

        mockMvc.perform(get("/findings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cve").value("CVE-2024-1234"));
    }

    @Test
    @DisplayName("Should get finding by ID via GET /findings/{id}")
    void shouldGetFindingById() throws Exception {
        when(securityFindingService.getFindingById("finding-1")).thenReturn(findingDto);

        mockMvc.perform(get("/findings/finding-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("finding-1"))
                .andExpect(jsonPath("$.riskScore").value(96));
    }

    @Test
    @DisplayName("Should get top prioritized findings via GET /findings/prioritized")
    void shouldGetPrioritizedFindings() throws Exception {
        when(securityFindingService.getTopPriorityFindings(10)).thenReturn(List.of(findingDto));

        mockMvc.perform(get("/findings/prioritized?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("P0"));
    }
}

