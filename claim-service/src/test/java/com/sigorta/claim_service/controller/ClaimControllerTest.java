package com.sigorta.claim_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigorta.claim_service.model.Claim;
import com.sigorta.claim_service.service.ClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getClaimById_ShouldReturnClaim() throws Exception {
        when(claimService.getClaimById(testId)).thenReturn(testClaim);

        mockMvc.perform(get("/api/claims/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimNumber").value(testClaim.getClaimNumber()))
                .andExpect(jsonPath("$.insuredPersonName").value(testClaim.getInsuredPersonName()));
    }

    @Test
    void getClaimByNumber_ShouldReturnClaim() throws Exception {
        String claimNumber = "CLM-1234567890-123";
        when(claimService.getClaimByNumber(claimNumber)).thenReturn(testClaim);

        mockMvc.perform(get("/api/claims/number/{claimNumber}", claimNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()));
    }

    @Test
    void getClaimsByPolicyId_ShouldReturnClaims() throws Exception {
        UUID policyId = UUID.randomUUID();
        when(claimService.getClaimsByPolicyId(policyId)).thenReturn(Arrays.asList(testClaim));

        mockMvc.perform(get("/api/claims/policy/{policyId}", policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claimNumber").value(testClaim.getClaimNumber()));
    }

    @Test
    void createClaim_ShouldReturnCreatedClaim() throws Exception {
        when(claimService.createClaim(any(Claim.class))).thenReturn(testClaim);

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClaim)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimNumber").value(testClaim.getClaimNumber()));
    }

    @Test
    void updateClaim_ShouldReturnUpdatedClaim() throws Exception {
        when(claimService.updateClaim(eq(testId), any(Claim.class))).thenReturn(testClaim);

        mockMvc.perform(put("/api/claims/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClaim)))
                .andExpect(status().isOk());
    }

    @Test
    void updateClaimStatus_ShouldReturnUpdatedClaim() throws Exception {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "APPROVED");

        when(claimService.updateClaimStatus(eq(testId), any(Claim.ClaimStatus.class))).thenReturn(testClaim);

        mockMvc.perform(patch("/api/claims/{id}/status", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void getStatistics_ShouldReturnStatistics() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("submitted", 5L);
        stats.put("under_review", 3L);
        stats.put("approved", 10L);
        stats.put("rejected", 2L);
        stats.put("payment_completed", 8L);

        when(claimService.getClaimCountByStatus(Claim.ClaimStatus.SUBMITTED)).thenReturn(5L);
        when(claimService.getClaimCountByStatus(Claim.ClaimStatus.UNDER_REVIEW)).thenReturn(3L);
        when(claimService.getClaimCountByStatus(Claim.ClaimStatus.APPROVED)).thenReturn(10L);
        when(claimService.getClaimCountByStatus(Claim.ClaimStatus.REJECTED)).thenReturn(2L);
        when(claimService.getClaimCountByStatus(Claim.ClaimStatus.PAYMENT_COMPLETED)).thenReturn(8L);

        mockMvc.perform(get("/api/claims/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submitted").value(5))
                .andExpect(jsonPath("$.approved").value(10));
    }
}