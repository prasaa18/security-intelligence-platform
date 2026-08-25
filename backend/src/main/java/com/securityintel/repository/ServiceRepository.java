package com.securityintel.repository;

import com.securityintel.model.Service;
import com.securityintel.model.Environment;
import com.securityintel.model.BusinessCriticality;
import com.securityintel.model.DataSensitivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends MongoRepository<Service, String> {
    
    Optional<Service> findByServiceName(String serviceName);
    
    List<Service> findByTeamName(String teamName);
    
    List<Service> findByEnvironment(Environment environment);
    
    List<Service> findByBusinessCriticality(BusinessCriticality businessCriticality);
    
    List<Service> findByDataSensitivity(DataSensitivity dataSensitivity);
    
    List<Service> findByInternetExposed(boolean internetExposed);
    
    List<Service> findByOwner(String owner);
    
    List<Service> findByRepository(String repository);
    
    @Query("{ 'serviceName': { $regex: ?0, $options: 'i' } }")
    List<Service> findByServiceNameContainingIgnoreCase(String serviceName);
    
    @Query("{ 'teamName': { $regex: ?0, $options: 'i' } }")
    List<Service> findByTeamNameContainingIgnoreCase(String teamName);
    
    List<Service> findByServiceNameAndEnvironment(String serviceName, Environment environment);
    
    @Query("{ 'businessCriticality': { $in: ['HIGH', 'CRITICAL'] } }")
    List<Service> findHighCriticalityServices();
    
    @Query("{ 'internetExposed': true, 'dataSensitivity': { $in: ['CONFIDENTIAL', 'SENSITIVE'] } }")
    List<Service> findInternetExposedSensitiveServices();
    
    List<Service> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    boolean existsByServiceName(String serviceName);
    
    // Aggregation methods for analytics
    @Query(value = "{}", count = true)
    long countAllServices();
    
    long countByBusinessCriticality(BusinessCriticality businessCriticality);
    
    long countByDataSensitivity(DataSensitivity dataSensitivity);
    
    long countByInternetExposed(boolean internetExposed);
    
    long countByEnvironment(Environment environment);
}