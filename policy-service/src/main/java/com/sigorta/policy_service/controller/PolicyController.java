package com.sigorta.policy_service.controller;

import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.service.PolicyService;
import com.sigorta.policy_service.events.PolicyUpdatedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Policy Management", description = "APIs for managing health insurance policies")
public class PolicyController {

    private final PolicyService policyService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<Policy> getPolicyById(@PathVariable UUID id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping("/number/{policyNumber}")
    @Operation(summary = "Get policy by policy number")
    public ResponseEntity<Policy> getPolicyByNumber(@PathVariable String policyNumber) {
        return ResponseEntity.ok(policyService.getPolicyByNumber(policyNumber));
    }

    @GetMapping("/tc/{tcNo}")
    @Operation(summary = "Get policies by TC number")
    public ResponseEntity<List<Policy>> getPoliciesByTcNo(@PathVariable String tcNo) {
        return ResponseEntity.ok(policyService.getPoliciesByTcNo(tcNo));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active policies for current date")
    public ResponseEntity<List<Policy>> getActivePolicies() {
        return ResponseEntity.ok(policyService.getActivePolicies(LocalDate.now()));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get policies expiring in next 30 days")
    public ResponseEntity<List<Policy>> getExpiringPolicies() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        return ResponseEntity.ok(policyService.getExpiringPolicies(today, thirtyDaysLater));
    }

    @PostMapping
    @Operation(summary = "Create new policy")
    public ResponseEntity<Policy> createPolicy(@RequestBody Policy policy) {
        return ResponseEntity.ok(policyService.createPolicy(policy));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update policy")
    public ResponseEntity<Policy> updatePolicy(@PathVariable UUID id, @RequestBody Policy policy) {
        Policy updatedPolicy = policyService.updatePolicy(id, policy);
        
        // Kafka event gönder - poliçe güncellendi
        publishPolicyUpdatedEvent(updatedPolicy);
        
        return ResponseEntity.ok(updatedPolicy);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update policy status")
    public ResponseEntity<Policy> updatePolicyStatus(@PathVariable UUID id, @RequestBody Map<String, String> status) {
        Policy.PolicyStatus newStatus = Policy.PolicyStatus.valueOf(status.get("status"));
        return ResponseEntity.ok(policyService.updatePolicyStatus(id, newStatus));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get policy statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = Map.of(
                "active", policyService.getPolicyCountByStatus(Policy.PolicyStatus.ACTIVE),
                "expired", policyService.getPolicyCountByStatus(Policy.PolicyStatus.EXPIRED),
                "cancelled", policyService.getPolicyCountByStatus(Policy.PolicyStatus.CANCELLED)
        );
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Poliçe güncelleme event'i Kafka'ya gönderir
     * 
     * Real-time notification için Kafka event'i yayınlar:
     * - policy.updated topic'ine gönderir
     * - BFF KafkaEventListener bunu dinler
     * - WebSocket üzerinden frontend'e notification gönderir
     * 
     * @param policy Güncellenmiş poliçe objesi
     */
    private void publishPolicyUpdatedEvent(Policy policy) {
        try {
            PolicyUpdatedEvent event = PolicyUpdatedEvent.builder()
                    .policyId(policy.getId())
                    .policyNumber(policy.getPolicyNumber())
                    .status(policy.getStatus().name())
                    .coverageLimit(policy.getCoverageLimit())
                    .remainingCoverage(policy.getCoverageLimit().subtract(policy.getUsedCoverage()))
                    .endDate(policy.getEndDate())
                    .updatedBy("system")
                    .correlationId(policy.getPolicyNumber())
                    .build();
            
            kafkaTemplate.send("policy.updated", policy.getPolicyNumber(), event);
            log.info("Policy updated event published to Kafka for policy: {}", policy.getPolicyNumber());
        } catch (Exception e) {
            log.error("Error publishing policy updated event", e);
        }
    }
}