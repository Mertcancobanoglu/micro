package com.sigorta.claim_service.service;

import com.sigorta.claim_service.model.Claim;
import com.sigorta.claim_service.repository.ClaimRepository;
import com.sigorta.claim_service.saga.ClaimSagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimSagaOrchestrator claimSagaOrchestrator;

    @InjectMocks
    private ClaimService claimService;

    private Claim testClaim;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testClaim = Claim.builder()
                .id(testId)
                .claimNumber("CLM-1234567890-123")
                .policyId(UUID.randomUUID())
                .insuredPersonName("John Doe")
                .insuredPersonTcNo("12345678901")
                .claimType(Claim.ClaimType.INPATIENT)
                .status(Claim.ClaimStatus.SUBMITTED)
                .incidentDate(LocalDateTime.now().minusDays(1))
                .reportDate(LocalDateTime.now())
                .hospitalName("Test Hospital")
                .diagnosis("Test Diagnosis")
                .claimedAmount(new BigDecimal("10000.00"))
                .reportedBy("user1")
                .assignedTo("processor1")
                .build();
    }

    @Test
    void getClaimById_ShouldReturnClaim_WhenClaimExists() {
        when(claimRepository.findById(testId)).thenReturn(Optional.of(testClaim));

        Claim result = claimService.getClaimById(testId);

        assertNotNull(result);
        assertEquals(testClaim.getClaimNumber(), result.getClaimNumber());
        verify(claimRepository, times(1)).findById(testId);
    }

    @Test
    void getClaimById_ShouldThrowException_WhenClaimNotFound() {
        when(claimRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> claimService.getClaimById(testId));
        verify(claimRepository, times(1)).findById(testId);
    }

    @Test
    void getClaimByNumber_ShouldReturnClaim_WhenClaimExists() {
        String claimNumber = "CLM-1234567890-123";
        when(claimRepository.findByClaimNumber(claimNumber)).thenReturn(Optional.of(testClaim));

        Claim result = claimService.getClaimByNumber(claimNumber);

        assertNotNull(result);
        assertEquals(testClaim.getId(), result.getId());
        verify(claimRepository, times(1)).findByClaimNumber(claimNumber);
    }

    @Test
    void getClaimsByPolicyId_ShouldReturnClaims_WhenClaimsExist() {
        UUID policyId = UUID.randomUUID();
        List<Claim> claims = Arrays.asList(testClaim);
        when(claimRepository.findByPolicyId(policyId)).thenReturn(claims);

        List<Claim> result = claimService.getClaimsByPolicyId(policyId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(claimRepository, times(1)).findByPolicyId(policyId);
    }

    @Test
    void createClaim_ShouldSaveClaimAndStartSaga() {
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);
        doNothing().when(claimSagaOrchestrator).startClaimSaga(any(Claim.class));

        Claim result = claimService.createClaim(testClaim);

        assertNotNull(result);
        assertEquals(Claim.ClaimStatus.SUBMITTED, result.getStatus());
        assertNotNull(result.getClaimNumber());
        verify(claimRepository, times(1)).save(any(Claim.class));
        verify(claimSagaOrchestrator, times(1)).startClaimSaga(any(Claim.class));
    }

    @Test
    void updateClaim_ShouldUpdateClaimDetails() {
        Claim updatedDetails = Claim.builder()
                .diagnosis("Updated Diagnosis")
                .hospitalName("Updated Hospital")
                .claimedAmount(new BigDecimal("15000.00"))
                .description("Updated description")
                .build();

        when(claimRepository.findById(testId)).thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);

        Claim result = claimService.updateClaim(testId, updatedDetails);

        assertNotNull(result);
        verify(claimRepository, times(1)).findById(testId);
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void updateClaimStatus_ShouldUpdateStatus() {
        Claim.ClaimStatus newStatus = Claim.ClaimStatus.APPROVED;
        when(claimRepository.findById(testId)).thenReturn(Optional.of(testClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);

        Claim result = claimService.updateClaimStatus(testId, newStatus);

        assertNotNull(result);
        verify(claimRepository, times(1)).findById(testId);
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void getClaimCountByStatus_ShouldReturnCount() {
        Claim.ClaimStatus status = Claim.ClaimStatus.SUBMITTED;
        long expectedCount = 5L;
        when(claimRepository.countByStatus(status)).thenReturn(expectedCount);

        long result = claimService.getClaimCountByStatus(status);

        assertEquals(expectedCount, result);
        verify(claimRepository, times(1)).countByStatus(status);
    }
}