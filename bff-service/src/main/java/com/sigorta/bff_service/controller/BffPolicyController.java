package com.sigorta.bff_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/bff/policies")
@RequiredArgsConstructor
@Slf4j
public class BffPolicyController {

    private final RestTemplate restTemplate;
    private static final String POLICY_SERVICE_URL = "http://localhost:8082/api/policies";

    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicy(@PathVariable String id) {
        try {
            String url = POLICY_SERVICE_URL + "/" + id;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching policy", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Policy service unavailable"));
        }
    }

    @GetMapping("/number/{policyNumber}")
    public ResponseEntity<?> getPolicyByNumber(@PathVariable String policyNumber) {
        try {
            String url = POLICY_SERVICE_URL + "/number/" + policyNumber;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching policy by number", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Policy service unavailable"));
        }
    }

    @GetMapping("/tc/{tcNo}")
    public ResponseEntity<?> getPoliciesByTcNo(@PathVariable String tcNo) {
        try {
            String url = POLICY_SERVICE_URL + "/tc/" + tcNo;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching policies by TC no", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Policy service unavailable"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody Object policy) {
        try {
            ResponseEntity<?> response = restTemplate.postForEntity(POLICY_SERVICE_URL, policy, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error creating policy", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Policy service unavailable"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePolicy(@PathVariable String id, @RequestBody Object policy) {
        try {
            String url = POLICY_SERVICE_URL + "/" + id;
            restTemplate.put(url, policy);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating policy", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Policy service unavailable"));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updatePolicyStatus(@PathVariable String id, @RequestBody Map<String, String> status) {
        try {
            String url = POLICY_SERVICE_URL + "/" + id + "/status";
            restTemplate.patchForObject(url, status, Object.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating policy status", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Policy service unavailable"));
        }
    }
}