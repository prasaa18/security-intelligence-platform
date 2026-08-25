package com.securityintel.repository;

import com.securityintel.model.ScanReport;
import com.securityintel.model.Tool;
import com.securityintel.model.ScanType;
import com.securityintel.model.Environment;
import com.securityintel.model.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScanReportRepository extends MongoRepository<ScanReport, String> {
    
    List<ScanReport> findByServiceName(String serviceName);
    
    List<ScanReport> findByTool(Tool tool);
    
    List<ScanReport> findByScanType(ScanType scanType);
    
    List<ScanReport> findByEnvironment(Environment environment);
    
    List<ScanReport> findByStatus(Status status);
    
    List<ScanReport> findByServiceNameAndEnvironment(String serviceName, Environment environment);
    
    List<ScanReport> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'serviceName': ?0, 'tool': ?1, 'scanType': ?2, 'environment': ?3 }")
    List<ScanReport> findByServiceAndToolAndTypeAndEnvironment(
        String serviceName, Tool tool, ScanType scanType, Environment environment);
    
    // Aggregation methods for dashboard
    @Query(value = "{}", count = true)
    long countAllReports();
    
    long countByTool(Tool tool);
    
    long countByScanType(ScanType scanType);
    
    long countByStatus(Status status);
}