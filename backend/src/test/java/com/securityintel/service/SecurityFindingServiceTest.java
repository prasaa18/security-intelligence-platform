package com.securityintel.service;

import com.securityintel.dto.SecurityFindingDto;
import com.securityintel.exception.ResourceNotFoundException;
import com.securityintel.mapper.EntityMapper;
import com.securityintel.model.Priority;
import com.securityintel.model.SecurityFinding;
import com.securityintel.model.Severity;
import com.securityintel.repository.SecurityFindingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFindingServiceTest {

    @Mock
    private SecurityFindingRepository securityFindingRepository;

    @Mock
    private EntityMapper entityMapper;

    private SecurityFindingService securityFindingService;
    private SecurityFinding testFinding;
    private SecurityFindingDto testFindingDto;

    @BeforeEach
    void setUp() {
        securityFindingService = new SecurityFindingService(securityFindingRepository, entityMapper);

        testFinding = new SecurityFinding();
        testFinding.setId("finding-1");
        testFinding.setCve("CVE-2024-1234");
        testFinding.setServiceName("payment-service");
        testFinding.setSeverity(Severity.CRITICAL);
        testFinding.setPriority(Priority.P0);

        testFindingDto = new SecurityFindingDto();
        testFindingDto.setId("finding-1");
        testFindingDto.setCve("CVE-2024-1234");
        testFindingDto.setServiceName("payment-service");
        testFindingDto.setSeverity(Severity.CRITICAL);
        testFindingDto.setPriority(Priority.P0);
    }

    @Test
    @DisplayName("Should get finding by ID")
    void shouldGetFindingById() {
        when(securityFindingRepository.findById("finding-1")).thenReturn(Optional.of(testFinding));
        when(entityMapper.toDto(testFinding)).thenReturn(testFindingDto);

        SecurityFindingDto result = securityFindingService.getFindingById("finding-1");

        assertNotNull(result);
        assertEquals("CVE-2024-1234", result.getCve());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding not found")
    void shouldThrowNotFound() {
        when(securityFindingRepository.findById("finding-x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            securityFindingService.getFindingById("finding-x"));
    }

    @Test
    @DisplayName("Should get prioritized findings with limit")
    void shouldGetPrioritizedFindings() {
        when(securityFindingRepository.findTopPriorityFindings(any(Pageable.class))).thenReturn(List.of(testFinding));
        when(entityMapper.toFindingDtos(List.of(testFinding))).thenReturn(List.of(testFindingDto));

        List<SecurityFindingDto> results = securityFindingService.getTopPriorityFindings(5);

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}

