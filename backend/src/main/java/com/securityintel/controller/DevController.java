package com.securityintel.controller;

import com.securityintel.dto.ServiceDto;
import com.securityintel.model.*;
import com.securityintel.service.ServiceManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dev")
@CrossOrigin(origins = "*")
public class DevController {

    private final ServiceManagementService serviceManagementService;

    public DevController(ServiceManagementService serviceManagementService) {
        this.serviceManagementService = serviceManagementService;
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seedSampleServices() {
        try {
            List<ServiceDto> sampleServices = createSampleServices();
            List<ServiceDto> createdServices = new ArrayList<>();

            for (ServiceDto serviceDto : sampleServices) {
                try {
                    // Check if service already exists
                    try {
                        serviceManagementService.getServiceByName(serviceDto.getServiceName());
                        // Service exists, skip
                        continue;
                    } catch (Exception e) {
                        // Service doesn't exist, create it
                        ServiceDto created = serviceManagementService.createService(serviceDto);
                        createdServices.add(created);
                    }
                } catch (Exception e) {
                    // Skip this service if there's an error
                    continue;
                }
            }

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Sample services seeded successfully",
                "servicesCreated", createdServices.size(),
                "totalSampleServices", sampleServices.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Failed to seed sample services: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private List<ServiceDto> createSampleServices() {
        List<ServiceDto> services = new ArrayList<>();

        // payment-service - Critical production service
        services.add(new ServiceDto(
            "payment-service",
            "Payments Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.SENSITIVE,
            "payment-service",
            "john.smith@company.com"
        ));

        // order-service - High business criticality
        services.add(new ServiceDto(
            "order-service",
            "Commerce Team",
            Environment.PRODUCTION,
            BusinessCriticality.HIGH,
            true,
            DataSensitivity.CONFIDENTIAL,
            "order-service",
            "jane.doe@company.com"
        ));

        // inventory-service
        services.add(new ServiceDto(
            "inventory-service",
            "Commerce Team",
            Environment.PRODUCTION,
            BusinessCriticality.HIGH,
            false,
            DataSensitivity.INTERNAL,
            "inventory-service",
            "mike.wilson@company.com"
        ));

        // user-service
        services.add(new ServiceDto(
            "user-service",
            "Identity Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.SENSITIVE,
            "user-service",
            "sarah.johnson@company.com"
        ));

        // auth-service
        services.add(new ServiceDto(
            "auth-service",
            "Identity Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.SENSITIVE,
            "auth-service",
            "alex.brown@company.com"
        ));

        // gateway-service
        services.add(new ServiceDto(
            "gateway-service",
            "Platform Team",
            Environment.PRODUCTION,
            BusinessCriticality.CRITICAL,
            true,
            DataSensitivity.INTERNAL,
            "gateway-service",
            "emily.davis@company.com"
        ));

        // catalog-service
        services.add(new ServiceDto(
            "catalog-service",
            "Commerce Team",
            Environment.PRODUCTION,
            BusinessCriticality.MEDIUM,
            true,
            DataSensitivity.PUBLIC,
            "catalog-service",
            "chris.taylor@company.com"
        ));

        // shipping-service
        services.add(new ServiceDto(
            "shipping-service",
            "Logistics Team",
            Environment.PRODUCTION,
            BusinessCriticality.HIGH,
            false,
            DataSensitivity.CONFIDENTIAL,
            "shipping-service",
            "lisa.anderson@company.com"
        ));

        // notification-service
        services.add(new ServiceDto(
            "notification-service",
            "Platform Team",
            Environment.PRODUCTION,
            BusinessCriticality.MEDIUM,
            false,
            DataSensitivity.INTERNAL,
            "notification-service",
            "david.martinez@company.com"
        ));

        // reporting-service
        services.add(new ServiceDto(
            "reporting-service",
            "Analytics Team",
            Environment.PRODUCTION,
            BusinessCriticality.LOW,
            false,
            DataSensitivity.INTERNAL,
            "reporting-service",
            "jennifer.garcia@company.com"
        ));

        return services;
    }
}