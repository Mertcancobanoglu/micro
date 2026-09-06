package com.sigorta.claim_service.saga.events;

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
public class ClaimEvent {
    private UUID claimId;
    private String claimNumber;
    private UUID policyId;
    private String eventType;
    private BigDecimal claimedAmount;
    private LocalDateTime timestamp;
    private String correlationId;
}