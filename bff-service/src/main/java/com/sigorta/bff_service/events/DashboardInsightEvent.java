package com.sigorta.bff_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dashboard Insight Event
 * 
 * Employee dashboard için analytics event'i:
 * - Policy validation sonuçları
 * - Document recommendations
 * - Quick check sonuçları
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInsightEvent {
    
    private UUID eventId;
    private String userId;
    private String actionType; // VALIDATION, RECOMMENDATION, QUICK_CHECK
    private String policyId;
    private String policyNumber;
    private String result; // COVERED, NOT_COVERED, PARTIALLY_COVERED
    private String analysisSource; // AI, RULE_BASED
    private LocalDateTime timestamp;
    private String correlationId;
}