package com.securityintel.controller;

import com.securityintel.dto.ScanReportDto;
import com.securityintel.model.Environment;
import com.securityintel.model.ScanType;
import com.securityintel.model.Tool;
import com.securityintel.service.SecurityReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SecurityReportService securityReportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    @DisplayName("Should upload report via POST /reports/upload")
    void shouldUploadReport() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "trivy.json", "application/json", "{}".getBytes());
        ScanReportDto reportDto = new ScanReportDto();
        reportDto.setId("rep-1");
        reportDto.setTool(Tool.TRIVY);
        reportDto.setServiceName("payment-service");

        SecurityReportService.ReportProcessingResult result = 
            new SecurityReportService.ReportProcessingResult(reportDto, Tool.TRIVY, 20, 15);

        when(securityReportService.processSecurityReport(any(), eq("payment-service"), eq(Environment.PRODUCTION)))
            .thenReturn(result);

        mockMvc.perform(multipart("/reports/upload")
                .file(file)
                .param("serviceName", "payment-service")
                .param("environment", "PRODUCTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tool").value("TRIVY"))
                .andExpect(jsonPath("$.rawFindings").value(20))
                .andExpect(jsonPath("$.uniqueFindings").value(15));
    }

    @Test
    @DisplayName("Should get all reports via GET /reports")
    void shouldGetAllReports() throws Exception {
        ScanReportDto reportDto = new ScanReportDto();
        reportDto.setId("rep-1");
        reportDto.setTool(Tool.TRIVY);
        when(securityReportService.getAllReports()).thenReturn(List.of(reportDto));

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rep-1"));
    }
}

