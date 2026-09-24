package com.sigorta.bff_service.controller;

import com.sigorta.bff_service.events.ClaimUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * BFF Claim Controller (Backend for Frontend - Hasar Yönetim API Gateway)
 * 
 * Bu controller, frontend uygulamaları için hasar yönetim API'larını sağlar.
 * claim-service microservisine proxy olarak çalışır ve istekleri yönlendirir.
 * 
 * BFF Görevleri:
 * - API Gateway olarak çalışma
 * - İstekleri claim-service'e yönlendirme
 * - Hata yönetimi ve graceful degradation
 * - Response transformation ve aggregation
 * - Service discovery ve load balancing
 * 
 * Avantajları:
 * - Frontend için tek bir endpoint
 * - Service soyutlama
 * - Merkezi hata yönetimi
 * - Caching ve rate limiting imkanı
 * - Request/response transformation
 * 
 * Hata Yönetimi:
 * - Service unavailable durumunda uygun HTTP response
 * - Detaylı hata loglama
 * - Graceful degradation
 * 
 * Yönlendirme:
 * - claim-service: http://localhost:8081/api/claims
 * - Tüm istekler claim-service'e forward edilir
 * 
 * @author Insurance Development Team
 */
@RestController
@RequestMapping("/api/bff/claims")
@RequiredArgsConstructor
@Slf4j
public class BffClaimController {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String CLAIM_SERVICE_URL = "http://localhost:8081/api/claims";

    /**
     * ID ile hasar getirme
     * 
     * claim-service'e proxy request gönderir:
     * - Hata durumunda SERVICE_UNAVAILABLE döner
     * - Response'ı frontend'e forward eder
     * 
     * @param id Hasar ID'si
     * @return Hasar detayları veya hata mesajı
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClaim(@PathVariable String id) {
        try {
            String url = CLAIM_SERVICE_URL + "/" + id;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching claim", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }

    /**
     * Hasar numarası ile hasar getirme
     * 
     * claim-service'e proxy request gönderir
     * 
     * @param claimNumber Hasar numarası
     * @return Hasar detayları veya hata mesajı
     */
    @GetMapping("/number/{claimNumber}")
    public ResponseEntity<?> getClaimByNumber(@PathVariable String claimNumber) {
        try {
            String url = CLAIM_SERVICE_URL + "/number/" + claimNumber;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching claim by number", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }

    /**
     * Poliçe ID'sine göre hasarları getirme
     * 
     * claim-service'e proxy request gönderir
     * 
     * @param policyId Poliçe ID'si
     * @return Hasar listesi veya hata mesajı
     */
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<?> getClaimsByPolicyId(@PathVariable String policyId) {
        try {
            String url = CLAIM_SERVICE_URL + "/policy/" + policyId;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching claims by policy id", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }

    /**
     * Kullanıcıya göre hasarları getirme
     * 
     * claim-service'e proxy request gönderir
     * 
     * @param username Kullanıcı adı
     * @return Hasar listesi veya hata mesajı
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getClaimsByUser(@PathVariable String username) {
        try {
            String url = CLAIM_SERVICE_URL + "/user/" + username;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching claims by user", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }

    /**
     * Yeni hasar oluşturma
     * 
     * claim-service'e proxy request gönderir:
     * - Saga orchestration tetiklenir
     * - Policy verification başlar
     * 
     * @param claim Hasar bilgileri
     * @return Oluşturulan hasar veya hata mesajı
     */
    @PostMapping
    public ResponseEntity<?> createClaim(@RequestBody Object claim) {
        try {
            ResponseEntity<?> response = restTemplate.postForEntity(CLAIM_SERVICE_URL, claim, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error creating claim", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }

    /**
     * Hasar güncelleme
     * 
     * claim-service'e proxy request gönderir
     * 
     * @param id Hasar ID'si
     * @param claim Güncellenecek hasar bilgileri
     * @return Başarı durumu veya hata mesajı
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClaim(@PathVariable String id, @RequestBody Object claim) {
        try {
            String url = CLAIM_SERVICE_URL + "/" + id;
            restTemplate.put(url, claim);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating claim", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }

    /**
     * Hasar durumu güncelleme
     * 
     * claim-service'e proxy request gönderir
     * 
     * @param id Hasar ID'si
     * @param status Yeni durum
     * @return Başarı durumu veya hata mesajı
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateClaimStatus(@PathVariable String id, @RequestBody Map<String, String> status) {
        try {
            String url = CLAIM_SERVICE_URL + "/" + id + "/status";
            restTemplate.patchForObject(url, status, Object.class);
            
            // Kafka event gönder - hasar güncellendi
            publishClaimUpdatedEvent(id, status.get("status"));
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating claim status", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Claim service unavailable"));
        }
    }
    
    /**
     * Hasar güncelleme event'i Kafka'ya gönderir
     * 
     * Real-time notification için Kafka event'i yayınlar:
     * - claim.updated topic'ine gönderir
     * - BFF KafkaEventListener bunu dinler
     * - WebSocket üzerinden frontend'e notification gönderir
     * 
     * @param claimId Hasar ID'si
     * @param status Yeni durum
     */
    private void publishClaimUpdatedEvent(String claimId, String status) {
        try {
            ClaimUpdatedEvent event = ClaimUpdatedEvent.builder()
                    .claimId(UUID.fromString(claimId))
                    .status(status)
                    .updatedAt(LocalDateTime.now())
                    .correlationId(UUID.randomUUID().toString())
                    .build();
            
            kafkaTemplate.send("claim.updated", claimId, event);
            log.info("Claim updated event published to Kafka for claim: {}", claimId);
        } catch (Exception e) {
            log.error("Error publishing claim updated event", e);
        }
    }
}