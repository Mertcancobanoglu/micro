package com.sigorta.bff_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Claim Updated Event
 * 
 * Claim service tarafından gönderilen event.
 * BFF service bunu dinler ve frontend'e gerçek zamanlı update gönderir.
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimUpdatedEvent {
    
    private UUID claimId;
    private String claimNumber;
    private String status;
    private BigDecimal approvedAmount;
    private BigDecimal customerPayment;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String correlationId;
}