package com.sigorta.policy_service.controller;

import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "APIs for managing health insurance policies")
public class PolicyController {

    private final PolicyService policyService;

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
        return ResponseEntity.ok(policyService.updatePolicy(id, policy));
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
}