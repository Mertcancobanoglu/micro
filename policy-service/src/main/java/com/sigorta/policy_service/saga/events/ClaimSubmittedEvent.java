package com.sigorta.policy_service.saga.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSubmittedEvent {
    private UUID claimId;
    private String claimNumber;
    private UUID policyId;
    private String insuredPersonName;
    private String insuredPersonTcNo;
    private String claimType;
    private LocalDateTime incidentDate;
    private BigDecimal claimedAmount;
    private String hospitalName;
    private String diagnosis;
    private LocalDateTime timestamp;
    private String correlationId;
}