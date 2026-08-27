package com.securityintel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "services")
public class Service {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String serviceName;
    
    private String teamName;
    
    @Indexed
    private Environment environment;
    
    private BusinessCriticality businessCriticality;
    private boolean internetExposed;
    private DataSensitivity dataSensitivity;
    private String repository;
    private String deploymentPlatform;
    private String owner;
    
    @Indexed
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    public Service() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.internetExposed = false;
        this.businessCriticality = BusinessCriticality.MEDIUM;
        this.dataSensitivity = DataSensitivity.INTERNAL;
        this.environment = Environment.DEVELOPMENT;
    }

    public Service(String serviceName, String teamName, Environment environment,
                   BusinessCriticality businessCriticality, boolean internetExposed,
                   DataSensitivity dataSensitivity, String repository, String owner) {
        this();
        this.serviceName = serviceName;
        this.teamName = teamName;
        this.environment = environment;
        this.businessCriticality = businessCriticality;
        this.internetExposed = internetExposed;
        this.dataSensitivity = dataSensitivity;
        this.repository = repository;
        this.owner = owner;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setTeam(String team) {
        if (this.teamName == null || this.teamName.isBlank()) {
            this.teamName = team;
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public BusinessCriticality getBusinessCriticality() {
        return businessCriticality;
    }

    public void setBusinessCriticality(BusinessCriticality businessCriticality) {
        this.businessCriticality = businessCriticality;
    }

    public boolean isInternetExposed() {
        return internetExposed;
    }

    public void setInternetExposed(boolean internetExposed) {
        this.internetExposed = internetExposed;
    }

    public DataSensitivity getDataSensitivity() {
        return dataSensitivity;
    }

    public void setDataSensitivity(DataSensitivity dataSensitivity) {
        this.dataSensitivity = dataSensitivity;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getDeploymentPlatform() {
        return deploymentPlatform;
    }

    public void setDeploymentPlatform(String deploymentPlatform) {
        this.deploymentPlatform = deploymentPlatform;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}