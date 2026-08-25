package com.securityintel.controller;

import com.securityintel.dto.ServiceDto;
import com.securityintel.service.ServiceManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceManagementService serviceManagementService;

    public ServiceController(ServiceManagementService serviceManagementService) {
        this.serviceManagementService = serviceManagementService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        List<ServiceDto> services = serviceManagementService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable String id) {
        ServiceDto service = serviceManagementService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/by-name/{serviceName}")
    public ResponseEntity<ServiceDto> getServiceByName(@PathVariable String serviceName) {
        ServiceDto service = serviceManagementService.getServiceByName(serviceName);
        return ResponseEntity.ok(service);
    }

    @PostMapping
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceDto serviceDto) {
        ServiceDto createdService = serviceManagementService.createService(serviceDto);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDto> updateService(@PathVariable String id, 
                                                   @Valid @RequestBody ServiceDto serviceDto) {
        ServiceDto updatedService = serviceManagementService.updateService(id, serviceDto);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        serviceManagementService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ServiceDto>> searchServices(@RequestParam String query) {
        List<ServiceDto> services = serviceManagementService.searchServices(query);
        return ResponseEntity.ok(services);
    }
}