package com.sigorta.policy_service.service;

import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyService policyService;

    private Policy testPolicy;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testPolicy = Policy.builder()
                .id(testId)
                .policyNumber("POL-1234567890-123")
                .insuredPersonName("John Doe")
                .insuredPersonTcNo("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender("M")
                .address("Test Address")
                .phone("5551234567")
                .email("test@example.com")
                .policyType(Policy.PolicyType.ELEMENTARY_HEALTH)
                .status(Policy.PolicyStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .annualPremium(new BigDecimal("5000.00"))
                .coverageLimit(new BigDecimal("100000.00"))
                .deductible(new BigDecimal("500.00"))
                .coPaymentPercentage(20)
                .beneficiaryName("Jane Doe")
                .beneficiaryTcNo("09876543210")
                .agentId("agent1")
                .build();
    }

    @Test
    void getPolicyById_ShouldReturnPolicy_WhenPolicyExists() {
        when(policyRepository.findById(testId)).thenReturn(Optional.of(testPolicy));

        Policy result = policyService.getPolicyById(testId);

        assertNotNull(result);
        assertEquals(testPolicy.getPolicyNumber(), result.getPolicyNumber());
        verify(policyRepository, times(1)).findById(testId);
    }

    @Test
    void getPolicyById_ShouldThrowException_WhenPolicyNotFound() {
        when(policyRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> policyService.getPolicyById(testId));
        verify(policyRepository, times(1)).findById(testId);
    }

    @Test
    void getPolicyByNumber_ShouldReturnPolicy_WhenPolicyExists() {
        String policyNumber = "POL-1234567890-123";
        when(policyRepository.findByPolicyNumber(policyNumber)).thenReturn(Optional.of(testPolicy));

        Policy result = policyService.getPolicyByNumber(policyNumber);

        assertNotNull(result);
        assertEquals(testPolicy.getId(), result.getId());
        verify(policyRepository, times(1)).findByPolicyNumber(policyNumber);
    }

    @Test
    void getPoliciesByTcNo_ShouldReturnPolicies_WhenPoliciesExist() {
        String tcNo = "12345678901";
        List<Policy> policies = Arrays.asList(testPolicy);
        when(policyRepository.findByInsuredPersonTcNo(tcNo)).thenReturn(policies);

        List<Policy> result = policyService.getPoliciesByTcNo(tcNo);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(policyRepository, times(1)).findByInsuredPersonTcNo(tcNo);
    }

    @Test
    void getActivePolicies_ShouldReturnActivePolicies() {
        LocalDate currentDate = LocalDate.now();
        List<Policy> policies = Arrays.asList(testPolicy);
        when(policyRepository.findActivePoliciesOnDate(currentDate)).thenReturn(policies);

        List<Policy> result = policyService.getActivePolicies(currentDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(policyRepository, times(1)).findActivePoliciesOnDate(currentDate);
    }

    @Test
    void getExpiringPolicies_ShouldReturnExpiringPolicies() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        List<Policy> policies = Arrays.asList(testPolicy);
        when(policyRepository.findPoliciesExpiringBetween(startDate, endDate)).thenReturn(policies);

        List<Policy> result = policyService.getExpiringPolicies(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(policyRepository, times(1)).findPoliciesExpiringBetween(startDate, endDate);
    }

    @Test
    void createPolicy_ShouldSavePolicyWithGeneratedNumber() {
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);

        Policy result = policyService.createPolicy(testPolicy);

        assertNotNull(result);
        assertEquals(Policy.PolicyStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getPolicyNumber());
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void updatePolicy_ShouldUpdatePolicyDetails() {
        Policy updatedDetails = Policy.builder()
                .insuredPersonName("Updated Name")
                .address("Updated Address")
                .phone("Updated Phone")
                .email("updated@example.com")
                .coverageDetails("Updated coverage")
                .build();

        when(policyRepository.findById(testId)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);

        Policy result = policyService.updatePolicy(testId, updatedDetails);

        assertNotNull(result);
        verify(policyRepository, times(1)).findById(testId);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void updatePolicyStatus_ShouldUpdateStatus() {
        Policy.PolicyStatus newStatus = Policy.PolicyStatus.CANCELLED;
        when(policyRepository.findById(testId)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);

        Policy result = policyService.updatePolicyStatus(testId, newStatus);

        assertNotNull(result);
        verify(policyRepository, times(1)).findById(testId);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void getPolicyCountByStatus_ShouldReturnCount() {
        Policy.PolicyStatus status = Policy.PolicyStatus.ACTIVE;
        long expectedCount = 10L;
        when(policyRepository.countByStatus(status)).thenReturn(expectedCount);

        long result = policyService.getPolicyCountByStatus(status);

        assertEquals(expectedCount, result);
        verify(policyRepository, times(1)).countByStatus(status);
    }
}