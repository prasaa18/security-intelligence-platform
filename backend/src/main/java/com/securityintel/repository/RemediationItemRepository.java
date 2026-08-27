package com.securityintel.repository;

import com.securityintel.model.Priority;
import com.securityintel.model.RemediationItem;
import com.securityintel.model.RemediationStatus;
import com.securityintel.model.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemediationItemRepository extends MongoRepository<RemediationItem, String> {
    
    List<RemediationItem> findByServiceNameOrderByPriorityDesc(String serviceName);

    List<RemediationItem> findByServiceName(String serviceName);
    
    List<RemediationItem> findByPriorityOrderByRiskScoreDesc(Priority priority);
    
    List<RemediationItem> findByRemediationStatus(RemediationStatus remediationStatus);
    
    List<RemediationItem> findByTeamName(String teamName);
    
    Optional<RemediationItem> findByFindingId(String findingId);
    
    long countByPriority(Priority priority);
    
    long countByRemediationStatus(RemediationStatus remediationStatus);
    
    long countByStatus(Status status);
    
    @Query("{ 'remediationStatus': ?0, 'status': 'OPEN' }")
    long countByRemediationStatusAndStatus(RemediationStatus remediationStatus, Status status);
    
    @Query("{ 'priority': ?0, 'status': 'OPEN' }")
    List<RemediationItem> findOpenByPriority(Priority priority);
    
    @Query("{ 'remediationStatus': ?0, 'status': 'OPEN' }")
    List<RemediationItem> findOpenByRemediationStatus(RemediationStatus remediationStatus);
    
    @Query(value = "{ 'status': 'OPEN' }", sort = "{ 'riskScore': -1 }")
    List<RemediationItem> findTopOpenRemediationItems(org.springframework.data.domain.Pageable pageable);
    
    @Query("{ 'priority': ?0, 'status': 'OPEN' }")
    long countByPriorityAndStatus(Priority priority, Status status);
}