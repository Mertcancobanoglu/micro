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
public class PolicyVerifiedEvent {
    private UUID policyId;
    private String policyNumber;
    private String policyType;
    private String policyStatus;
    private BigDecimal coverageLimit;
    private BigDecimal deductible;
    private Integer coPaymentPercentage;
    private boolean isValid;
    private String rejectionReason;
    private LocalDateTime timestamp;
    private String correlationId;
}