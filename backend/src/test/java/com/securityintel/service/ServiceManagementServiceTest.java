package com.securityintel.service;

import com.securityintel.dto.ServiceDto;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.exception.ServiceAlreadyExistsException;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.BusinessCriticality;
import com.securityintel.model.DataSensitivity;
import com.securityintel.model.Environment;
import com.securityintel.model.Service;
import com.securityintel.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private EntityMapper entityMapper;

    private ServiceManagementService serviceManagementService;
    private Service testService;
    private ServiceDto testServiceDto;

    @BeforeEach
    void setUp() {
        serviceManagementService = new ServiceManagementService(serviceRepository, entityMapper);

        testService = new Service();
        testService.setId("service-1");
        testService.setServiceName("payment-service");
        testService.setTeamName("Payments Team");
        testService.setEnvironment(Environment.PRODUCTION);
        testService.setBusinessCriticality(BusinessCriticality.CRITICAL);
        testService.setInternetExposed(true);
        testService.setDataSensitivity(DataSensitivity.SENSITIVE);

        testServiceDto = new ServiceDto();
        testServiceDto.setId("service-1");
        testServiceDto.setServiceName("payment-service");
        testServiceDto.setTeamName("Payments Team");
        testServiceDto.setEnvironment(Environment.PRODUCTION);
        testServiceDto.setBusinessCriticality(BusinessCriticality.CRITICAL);
        testServiceDto.setInternetExposed(true);
        testServiceDto.setDataSensitivity(DataSensitivity.SENSITIVE);
    }

    @Test
    @DisplayName("Should create service successfully")
    void shouldCreateServiceSuccessfully() {
        when(serviceRepository.existsByServiceName("payment-service")).thenReturn(false);
        when(entityMapper.toEntity(testServiceDto)).thenReturn(testService);
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);
        when(entityMapper.toDto(testService)).thenReturn(testServiceDto);

        ServiceDto result = serviceManagementService.createService(testServiceDto);

        assertNotNull(result);
        assertEquals("payment-service", result.getServiceName());
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Should throw exception when creating service with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicateService() {
        when(serviceRepository.existsByServiceName("payment-service")).thenReturn(true);

        assertThrows(ServiceAlreadyExistsException.class, () ->
            serviceManagementService.createService(testServiceDto));

        verify(serviceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get service by ID successfully")
    void shouldGetServiceById() {
        when(serviceRepository.findById("service-1")).thenReturn(Optional.of(testService));
        when(entityMapper.toDto(testService)).thenReturn(testServiceDto);

        ServiceDto result = serviceManagementService.getServiceById("service-1");

        assertNotNull(result);
        assertEquals("payment-service", result.getServiceName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when service does not exist")
    void shouldThrowNotFoundWhenServiceMissing() {
        when(serviceRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            serviceManagementService.getServiceById("invalid-id"));
    }
}

