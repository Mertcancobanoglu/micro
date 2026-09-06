package com.sigorta.claim_service.controller;

import com.sigorta.claim_service.model.Claim;
import com.sigorta.claim_service.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Claim Controller (Hasar Yönetim API)
 * 
 * Bu controller, sağlık sigortası hasar yönetimi için REST API endpoint'leri sağlar.
 * Swagger/OpenAPI dokümantasyonu ile tam entegre çalışır.
 * 
 * API Endpoint'leri:
 * - GET /api/claims/{id}: ID ile hasar getirme
 * - GET /api/claims/number/{claimNumber}: Numara ile hasar getirme
 * - GET /api/claims/policy/{policyId}: Poliçe hasarlarını getirme
 * - GET /api/claims/user/{username}: Kullanıcı hasarlarını getirme
 * - POST /api/claims: Yeni hasar oluşturma
 * - PUT /api/claims/{id}: Hasar güncelleme
 * - PATCH /api/claims/{id}/status: Hasar durumu güncelleme
 * - GET /api/claims/statistics: Hasar istatistikleri
 * 
 * Güvenlik:
 * - JWT authentication gerektirir
 * - Role-based access control
 * - Spring Security ile entegre
 * 
 * Swagger Dokümantasyonu:
 * - http://localhost:8081/swagger-ui.html
 * - Otomatik API dokümantasyonu
 * - Interactive API testing
 * 
 * @author Insurance Development Team
 */
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claim Management", description = "APIs for managing health insurance claims")
public class ClaimController {

    private final ClaimService claimService;

    /**
     * ID ile hasar getirme
     * 
     * @param id Hasar UUID'si
     * @return Hasar detayları
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID", description = "Retrieve a specific claim by its UUID")
    public ResponseEntity<Claim> getClaimById(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    /**
     * Hasar numarası ile hasar getirme
     * 
     * @param claimNumber Benzersiz hasar numarası (format: CLM-{timestamp}-{random})
     * @return Hasar detayları
     */
    @GetMapping("/number/{claimNumber}")
    @Operation(summary = "Get claim by claim number", description = "Retrieve a claim by its unique claim number")
    public ResponseEntity<Claim> getClaimByNumber(@PathVariable String claimNumber) {
        return ResponseEntity.ok(claimService.getClaimByNumber(claimNumber));
    }

    /**
     * Poliçe ID'sine göre hasarları getirme
     * 
     * @param policyId Poliçe UUID'si
     * @return Hasar listesi
     */
    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get claims by policy ID", description = "Retrieve all claims associated with a specific policy")
    public ResponseEntity<List<Claim>> getClaimsByPolicyId(@PathVariable UUID policyId) {
        return ResponseEntity.ok(claimService.getClaimsByPolicyId(policyId));
    }

    /**
     * Kullanıcıya göre hasarları getirme
     * 
     * @param username Kullanıcı adı (reportedBy field)
     * @return Hasar listesi
     */
    @GetMapping("/user/{username}")
    @Operation(summary = "Get claims by user", description = "Retrieve all claims reported by a specific user")
    public ResponseEntity<List<Claim>> getClaimsByUser(@PathVariable String username) {
        return ResponseEntity.ok(claimService.getClaimsByUser(username));
    }

    /**
     * Yeni hasar oluşturma
     * 
     * Bu endpoint:
     * - Otomatik hasar numarası oluşturur
     * - Durumu SUBMITTED olarak ayarlar
     * - Saga orchestration başlatır
     * - Policy verification tetikler
     * 
     * @param claim Oluşturulacak hasar bilgileri
     * @return Oluşturulan hasar (generated claim number ile)
     */
    @PostMapping
    @Operation(summary = "Create new claim", description = "Create a new insurance claim and trigger policy verification saga")
    public ResponseEntity<Claim> createClaim(@RequestBody Claim claim) {
        return ResponseEntity.ok(claimService.createClaim(claim));
    }

    /**
     * Hasar güncelleme
     * 
     * Güncellenebilir alanlar:
     * - Tanı (diagnosis)
     * - Hastane adı
     * - Talep edilen tutar
     * - Açıklama
     * 
     * @param id Hasar UUID'si
     * @param claim Güncellenecek hasar bilgileri
     * @return Güncellenmiş hasar
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update claim", description = "Update existing claim details")
    public ResponseEntity<Claim> updateClaim(@PathVariable UUID id, @RequestBody Claim claim) {
        return ResponseEntity.ok(claimService.updateClaim(id, claim));
    }

    /**
     * Hasar durumu güncelleme
     * 
     * Durum geçişleri:
     * - SUBMITTED -> UNDER_REVIEW
     * - UNDER_REVIEW -> APPROVED/REJECTED
     * - APPROVED -> PAYMENT_PROCESSING
     * - PAYMENT_PROCESSING -> PAYMENT_COMPLETED
     * 
     * @param id Hasar UUID'si
     * @param status Yeni durum (enum value)
     * @return Güncellenmiş hasar
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update claim status", description = "Update the status of a claim")
    public ResponseEntity<Claim> updateClaimStatus(@PathVariable UUID id, @RequestBody Map<String, String> status) {
        Claim.ClaimStatus newStatus = Claim.ClaimStatus.valueOf(status.get("status"));
        return ResponseEntity.ok(claimService.updateClaimStatus(id, newStatus));
    }

    /**
     * Hasar istatistikleri
     * 
     * İstatistikler:
     * - submitted: Gönderilen hasar sayısı
     * - under_review: İncelenen hasar sayısı
     * - approved: Onaylanan hasar sayısı
     * - rejected: Reddedilen hasar sayısı
     * - payment_completed: Ödemesi tamamlanan hasar sayısı
     * 
     * @return Durum bazlı hasar sayıları
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get claim statistics", description = "Retrieve claim statistics grouped by status")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = Map.of(
                "submitted", claimService.getClaimCountByStatus(Claim.ClaimStatus.SUBMITTED),
                "under_review", claimService.getClaimCountByStatus(Claim.ClaimStatus.UNDER_REVIEW),
                "approved", claimService.getClaimCountByStatus(Claim.ClaimStatus.APPROVED),
                "rejected", claimService.getClaimCountByStatus(Claim.ClaimStatus.REJECTED),
                "payment_completed", claimService.getClaimCountByStatus(Claim.ClaimStatus.PAYMENT_COMPLETED)
        );
        return ResponseEntity.ok(stats);
    }
}