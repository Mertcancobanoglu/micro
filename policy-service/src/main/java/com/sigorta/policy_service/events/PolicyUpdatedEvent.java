package com.sigorta.policy_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Policy Updated Event
 * 
 * Policy service tarafından BFF service'e gönderilen event.
 * BFF service bunu dinler ve frontend'e gerçek zamanlı update gönderir.
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyUpdatedEvent {
    
    private UUID policyId;
    private String policyNumber;
    private String status;
    private BigDecimal coverageLimit;
    private BigDecimal remainingCoverage;
    private LocalDate endDate;
    private String updatedBy;
    private String correlationId;
}