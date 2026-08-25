package com.securityintel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securityintel.dto.ServiceDto;
import com.securityintel.model.BusinessCriticality;
import com.securityintel.model.DataSensitivity;
import com.securityintel.model.Environment;
import com.securityintel.service.ServiceManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceManagementService serviceManagementService;

    @InjectMocks
    private ServiceController serviceController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ServiceDto testServiceDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceController).build();

        testServiceDto = new ServiceDto();
        testServiceDto.setId("service-id");
        testServiceDto.setServiceName("payment-service");
        testServiceDto.setTeamName("platform-team");
        testServiceDto.setEnvironment(Environment.PRODUCTION);
        testServiceDto.setBusinessCriticality(BusinessCriticality.HIGH);
        testServiceDto.setInternetExposed(true);
        testServiceDto.setDataSensitivity(DataSensitivity.SENSITIVE);
    }

    @Test
    @DisplayName("Should create service successfully via POST /services")
    void shouldCreateServiceSuccessfully() throws Exception {
        when(serviceManagementService.createService(any(ServiceDto.class))).thenReturn(testServiceDto);

        mockMvc.perform(post("/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testServiceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("service-id"))
                .andExpect(jsonPath("$.serviceName").value("payment-service"));

        verify(serviceManagementService).createService(any(ServiceDto.class));
    }

    @Test
    @DisplayName("Should get all services via GET /services")
    void shouldGetAllServices() throws Exception {
        when(serviceManagementService.getAllServices()).thenReturn(List.of(testServiceDto));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].serviceName").value("payment-service"));
    }

    @Test
    @DisplayName("Should get service by ID via GET /services/{id}")
    void shouldGetServiceById() throws Exception {
        when(serviceManagementService.getServiceById("service-id")).thenReturn(testServiceDto);

        mockMvc.perform(get("/services/service-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("service-id"))
                .andExpect(jsonPath("$.serviceName").value("payment-service"));
    }
}

