package com.sigorta.policy_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getPolicyById_ShouldReturnPolicy() throws Exception {
        when(policyService.getPolicyById(testId)).thenReturn(testPolicy);

        mockMvc.perform(get("/api/policies/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNumber").value(testPolicy.getPolicyNumber()))
                .andExpect(jsonPath("$.insuredPersonName").value(testPolicy.getInsuredPersonName()));
    }

    @Test
    void getPolicyByNumber_ShouldReturnPolicy() throws Exception {
        String policyNumber = "POL-1234567890-123";
        when(policyService.getPolicyByNumber(policyNumber)).thenReturn(testPolicy);

        mockMvc.perform(get("/api/policies/number/{policyNumber}", policyNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()));
    }

    @Test
    void getPoliciesByTcNo_ShouldReturnPolicies() throws Exception {
        String tcNo = "12345678901";
        when(policyService.getPoliciesByTcNo(tcNo)).thenReturn(Arrays.asList(testPolicy));

        mockMvc.perform(get("/api/policies/tc/{tcNo}", tcNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyNumber").value(testPolicy.getPolicyNumber()));
    }

    @Test
    void getActivePolicies_ShouldReturnActivePolicies() throws Exception {
        when(policyService.getActivePolicies(any(LocalDate.class))).thenReturn(Arrays.asList(testPolicy));

        mockMvc.perform(get("/api/policies/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyNumber").value(testPolicy.getPolicyNumber()));
    }

    @Test
    void getExpiringPolicies_ShouldReturnExpiringPolicies() throws Exception {
        when(policyService.getExpiringPolicies(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testPolicy));

        mockMvc.perform(get("/api/policies/expiring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].policyNumber").value(testPolicy.getPolicyNumber()));
    }

    @Test
    void createPolicy_ShouldReturnCreatedPolicy() throws Exception {
        when(policyService.createPolicy(any(Policy.class))).thenReturn(testPolicy);

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPolicy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNumber").value(testPolicy.getPolicyNumber()));
    }

    @Test
    void updatePolicy_ShouldReturnUpdatedPolicy() throws Exception {
        when(policyService.updatePolicy(eq(testId), any(Policy.class))).thenReturn(testPolicy);

        mockMvc.perform(put("/api/policies/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPolicy)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePolicyStatus_ShouldReturnUpdatedPolicy() throws Exception {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "CANCELLED");

        when(policyService.updatePolicyStatus(eq(testId), any(Policy.PolicyStatus.class))).thenReturn(testPolicy);

        mockMvc.perform(patch("/api/policies/{id}/status", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void getStatistics_ShouldReturnStatistics() throws Exception {
        when(policyService.getPolicyCountByStatus(Policy.PolicyStatus.ACTIVE)).thenReturn(10L);
        when(policyService.getPolicyCountByStatus(Policy.PolicyStatus.EXPIRED)).thenReturn(5L);
        when(policyService.getPolicyCountByStatus(Policy.PolicyStatus.CANCELLED)).thenReturn(2L);

        mockMvc.perform(get("/api/policies/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(10))
                .andExpect(jsonPath("$.expired").value(5))
                .andExpect(jsonPath("$.cancelled").value(2));
    }
}