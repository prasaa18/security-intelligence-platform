                                                                                                                                                            package com.securityintel.repository;

import com.securityintel.model.SecurityFinding;
import com.securityintel.model.Tool;
import com.securityintel.model.ScanType;
import com.securityintel.model.Environment;
import com.securityintel.model.Severity;
import com.securityintel.model.Status;
import com.securityintel.model.Priority;
import com.securityintel.model.DetectionState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityFindingRepository extends MongoRepository<SecurityFinding, String> {
    
    List<SecurityFinding> findByReportId(String reportId);
    
    List<SecurityFinding> findByServiceName(String serviceName);                                       
    
    List<SecurityFinding> findByTool(Tool tool);
    
    List<SecurityFinding> findByScanType(ScanType scanType);
    
    List<SecurityFinding> findByEnvironment(Environment environment);
    
    List<SecurityFinding> findBySeverity(Severity severity);
    
    List<SecurityFinding> findByStatus(Status status);
    
    List<SecurityFinding> findByPriority(Priority priority);
    
    List<SecurityFinding> findByCve(String cve);
    
    Optional<SecurityFinding> findByFingerprint(String fingerprint);
    
    List<SecurityFinding> findByServiceNameAndEnvironment(String serviceName, Environment environment);
    
    List<SecurityFinding> findByServiceNameAndStatus(String serviceName, Status status);
    
    List<SecurityFinding> findBySeverityAndStatus(Severity severity, Status status);
    
    List<SecurityFinding> findByPriorityAndStatus(Priority priority, Status status);
    
    List<SecurityFinding> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'serviceName': ?0, 'severity': ?1, 'status': ?2 }")
    List<SecurityFinding> findByServiceAndSeverityAndStatus(
        String serviceName, Severity severity, Status status);
    
    // For deduplication
    List<SecurityFinding> findByFingerprintIn(List<String> fingerprints);
    
    @Query("{ 'cve': ?0, 'serviceName': ?1, 'packageName': ?2, 'installedVersion': ?3 }")
    List<SecurityFinding> findPotentialDuplicatesByCve(
        String cve, String serviceName, String packageName, String installedVersion);
    
    @Query("{ 'tool': ?0, 'serviceName': ?1, 'title': ?2, 'packageName': ?3, 'filePath': ?4, 'lineNumber': ?5 }")
    List<SecurityFinding> findPotentialDuplicatesBySignature(
        Tool tool, String serviceName, String title, String packageName, String filePath, Integer lineNumber);
    
    // Dashboard aggregation methods
    @Query(value = "{}", count = true)
    long countAllFindings();
    
    long countBySeverity(Severity severity);
    
    long countByStatus(Status status);
    
    long countByPriority(Priority priority);
    
    long countByTool(Tool tool);
    
    long countByScanType(ScanType scanType);
    
    @Query(value = "{ 'status': 'OPEN' }", count = true)
    long countOpenFindings();
    
    @Query(value = "{ 'severity': { $in: ['CRITICAL', 'HIGH'] }, 'status': 'OPEN' }", count = true)
    long countCriticalAndHighOpenFindings();
    
    // Top priority findings for dashboard
    @Query(value = "{ 'status': 'OPEN' }", sort = "{ 'riskScore': -1, 'createdAt': -1 }")
    List<SecurityFinding> findTopPriorityFindings(org.springframework.data.domain.Pageable pageable);
    
    // Detection state tracking
    List<SecurityFinding> findByDetectionState(DetectionState detectionState);
    
    List<SecurityFinding> findByLatestScanId(String scanId);
    
    List<SecurityFinding> findByServiceNameAndDetectionState(String serviceName, DetectionState detectionState);
    
    @Query("{ 'serviceName': ?0, 'status': 'OPEN', 'detectionState': ?1 }")
    List<SecurityFinding> findOpenByServiceAndDetectionState(String serviceName, DetectionState detectionState);
    
    @Query("{ 'latestScanId': ?0, 'detectionState': 'NEW' }")
    List<SecurityFinding> findNewFindingsInScan(String scanId);
    
    @Query("{ 'latestScanId': ?0, 'detectionState': 'NOT_DETECTED_IN_LATEST_SCAN' }")
    List<SecurityFinding> findResolvedFindingsInScan(String scanId);
    
    @Query("{ 'latestScanId': ?0, 'detectionState': 'PRESENT' }")
    List<SecurityFinding> findUnchangedFindingsInScan(String scanId);
    
    @Query("{ 'serviceName': ?0, 'status': 'OPEN', 'detectionState': { $ne: 'NOT_DETECTED_IN_LATEST_SCAN' } }")
    List<SecurityFinding> findActiveFindingsByService(String serviceName);
    
    @Query("{ 'status': 'OPEN', 'detectionState': { $ne: 'NOT_DETECTED_IN_LATEST_SCAN' } }")
    List<SecurityFinding> findAllActiveFindings();
    
    long countByPriorityAndStatus(Priority priority, Status status);
    
    @Query("{ 'serviceName': ?0, 'priority': ?1, 'status': ?2 }")
    List<SecurityFinding> findByServiceNameAndPriorityAndStatus(String serviceName, Priority priority, Status status);
    
    List<SecurityFinding> findByScanExecutionId(String scanExecutionId);
}