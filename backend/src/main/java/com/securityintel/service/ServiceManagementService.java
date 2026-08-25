package com.securityintel.service;

import com.securityintel.dto.ServiceDto;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.exception.ServiceAlreadyExistsException;
import com.securityintel.exception.DatabaseException;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.Service;
import com.securityintel.repository.ServiceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final EntityMapper entityMapper;

    public ServiceManagementService(ServiceRepository serviceRepository, EntityMapper entityMapper) {
        this.serviceRepository = serviceRepository;
        this.entityMapper = entityMapper;
    }

    public List<ServiceDto> getAllServices() {
        try {
            List<Service> services = serviceRepository.findAll();
            return entityMapper.toServiceDtos(services);
        } catch (Exception e) {
            throw new DatabaseException("Failed to retrieve services", e);
        }
    }

    public ServiceDto getServiceById(String id) {
        Service service = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return entityMapper.toDto(service);
    }

    public ServiceDto getServiceByName(String serviceName) {
        Service service = serviceRepository.findByServiceName(serviceName)
            .orElseThrow(() -> new ResourceNotFoundException("Service not found with name: " + serviceName));
        return entityMapper.toDto(service);
    }

    public ServiceDto createService(ServiceDto serviceDto) {
        // Check if service with same name already exists
        if (serviceRepository.existsByServiceName(serviceDto.getServiceName())) {
            throw new ServiceAlreadyExistsException("Service already exists with name: " + serviceDto.getServiceName());
        }

        try {
            Service service = entityMapper.toEntity(serviceDto);
            service.setCreatedAt(LocalDateTime.now());
            service.setUpdatedAt(LocalDateTime.now());
            
            Service savedService = serviceRepository.save(service);
            return entityMapper.toDto(savedService);
        } catch (Exception e) {
            throw new DatabaseException("Failed to create service", e);
        }
    }

    public ServiceDto updateService(String id, ServiceDto serviceDto) {
        Service existingService = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        // Check if trying to update service name to an existing one
        if (!existingService.getServiceName().equals(serviceDto.getServiceName()) &&
            serviceRepository.existsByServiceName(serviceDto.getServiceName())) {
            throw new ServiceAlreadyExistsException("Service already exists with name: " + serviceDto.getServiceName());
        }

        try {
            // Update fields
            existingService.setServiceName(serviceDto.getServiceName());
            existingService.setTeamName(serviceDto.getTeamName());
            existingService.setEnvironment(serviceDto.getEnvironment());
            existingService.setBusinessCriticality(serviceDto.getBusinessCriticality());
            existingService.setInternetExposed(serviceDto.isInternetExposed());
            existingService.setDataSensitivity(serviceDto.getDataSensitivity());
            existingService.setRepository(serviceDto.getRepository());
            existingService.setDeploymentPlatform(serviceDto.getDeploymentPlatform());
            existingService.setOwner(serviceDto.getOwner());
            existingService.setUpdatedAt(LocalDateTime.now());

            Service savedService = serviceRepository.save(existingService);
            return entityMapper.toDto(savedService);
        } catch (Exception e) {
            throw new DatabaseException("Failed to update service", e);
        }
    }

    public void deleteService(String id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service not found with id: " + id);
        }

        try {
            serviceRepository.deleteById(id);
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete service", e);
        }
    }

    public List<ServiceDto> searchServices(String query) {
        try {
            List<Service> services = serviceRepository.findByServiceNameContainingIgnoreCase(query);
            return entityMapper.toServiceDtos(services);
        } catch (Exception e) {
            throw new DatabaseException("Failed to search services", e);
        }
    }

    public Optional<Service> findServiceEntityByName(String serviceName) {
        return serviceRepository.findByServiceName(serviceName);
    }
}