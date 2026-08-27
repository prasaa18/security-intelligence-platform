package com.securityintel.repository;

import com.securityintel.model.ScanExecution;
import com.securityintel.model.Tool;
import com.securityintel.model.ScanType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScanExecutionRepository extends MongoRepository<ScanExecution, String> {
    
    List<ScanExecution> findByServiceNameOrderByCreatedAtDesc(String serviceName);
    
    @Query("{ 'serviceName': ?0, 'tool': ?1, 'scanType': ?2, 'status': ?3 }")
    Optional<ScanExecution> findFirstByServiceNameAndToolAndScanTypeAndStatusOrderByCreatedAtDesc(
        String serviceName, Tool tool, ScanType scanType, com.securityintel.model.Status status);
    
    List<ScanExecution> findByStatusOrderByCreatedAtDesc(com.securityintel.model.Status status);
    
    List<ScanExecution> findByReceivedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);
    
    List<ScanExecution> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);
    
    @Query("{ 'serviceName': ?0, 'status': 'SUCCESS' }")
    List<ScanExecution> findSuccessfulScansByService(String serviceName);
    
    @Query("{ 'serviceName': ?0, 'status': 'SUCCESS' }")
    Optional<ScanExecution> findLatestSuccessfulScanByService(String serviceName);
    
    long countByServiceNameAndStatus(String serviceName, com.securityintel.model.Status status);
}