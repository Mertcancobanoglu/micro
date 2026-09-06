package com.sigorta.claim_service.service;

import com.sigorta.claim_service.model.Claim;
import com.sigorta.claim_service.repository.ClaimRepository;
import com.sigorta.claim_service.saga.ClaimSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Claim Service (Hasar Servisi)
 * 
 * Bu sınıf, hasar yönetimi iş mantığını içerir:
 * - CRUD operasyonları
 * - Redis caching ile performans optimizasyonu
 * - Saga pattern entegrasyonu
 * - Transaction yönetimi
 * 
 * Caching Stratejisi:
 * - @Cacheable: Okuma işlemlerinde cache kullanılır
 * - @CacheEvict: Yazma işlemlerinde cache temizlenir
 * - Cache türleri: claims, policyClaims, userClaims, claimStatistics
 * 
 * Transaction Yönetimi:
 * - @Transactional: Veri tutarlılığı için transaction guarantee
 * - Saga başlatma: Transaction içinde saga orchestration
 * 
 * Ana Metodlar:
 * - getClaimById: ID ile hasar getirme (cached)
 * - getClaimByNumber: Numara ile hasar getirme (cached)
 * - getClaimsByPolicyId: Poliçe hasarlarını getirme (cached)
 * - getClaimsByUser: Kullanıcı hasarlarını getirme (cached)
 * - createClaim: Yeni hasar oluşturma (saga başlatır)
 * - updateClaim: Hasar güncelleme
 * - updateClaimStatus: Hasar durumu güncelleme
 * 
 * @author Insurance Development Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimSagaOrchestrator claimSagaOrchestrator;

    /**
     * ID ile hasar getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "claims:{id}"
     * - TTL: 1 saat (config'de tanımlı)
     * - Cache miss: Veritabanından sorgular
     * 
     * @param id Hasar ID'si
     * @return Hasar detayları
     */
    @Cacheable(value = "claims", key = "#id")
    public Claim getClaimById(UUID id) {
        log.info("Fetching claim with id: {}", id);
        return claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + id));
    }

    /**
     * Hasar numarası ile hasar getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "claims:{claimNumber}"
     * - Benzersiz hasar numarası ile sorgulama
     * 
     * @param claimNumber Hasar numarası
     * @return Hasar detayları
     */
    @Cacheable(value = "claims", key = "#claimNumber")
    public Claim getClaimByNumber(String claimNumber) {
        log.info("Fetching claim with number: {}", claimNumber);
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found with number: " + claimNumber));
    }

    /**
     * Poliçe ID'sine göre hasarları getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "policyClaims:{policyId}"
     * - Bir poliçeye ait tüm hasarlar
     * 
     * @param policyId Poliçe ID'si
     * @return Hasar listesi
     */
    @Cacheable(value = "policyClaims", key = "#policyId")
    public List<Claim> getClaimsByPolicyId(UUID policyId) {
        log.info("Fetching claims for policy: {}", policyId);
        return claimRepository.findByPolicyId(policyId);
    }

    /**
     * Kullanıcıya göre hasarları getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "userClaims:{username}"
     * - Bir kullanıcının raporladığı hasarlar
     * 
     * @param username Kullanıcı adı
     * @return Hasar listesi
     */
    @Cacheable(value = "userClaims", key = "#username")
    public List<Claim> getClaimsByUser(String username) {
        log.info("Fetching claims for user: {}", username);
        return claimRepository.findByReportedBy(username);
    }

    /**
     * Yeni hasar oluşturma
     * 
     * İş akışı:
     * 1. Hasar numarası oluşturur
     * 2. Durumu SUBMITTED olarak ayarlar
     * 3. Rapor tarihini set eder
     * 4. Veritabanına kaydeder
     * 5. Saga orchestration başlatır
     * 6. Tüm cache'leri temizler
     * 
     * Transaction: Atomic işlem guarantee
     * Cache: Tüm cache'ler temizlenir
     * Saga: Policy verification başlatılır
     * 
     * @param claim Oluşturulacak hasar
     * @return Kaydedilen hasar
     */
    @Transactional
    @CacheEvict(value = {"claims", "policyClaims", "userClaims"}, allEntries = true)
    public Claim createClaim(Claim claim) {
        log.info("Creating new claim for policy: {}", claim.getPolicyId());

        claim.setClaimNumber(generateClaimNumber());
        claim.setStatus(Claim.ClaimStatus.SUBMITTED);
        claim.setReportDate(java.time.LocalDateTime.now());

        Claim savedClaim = claimRepository.save(claim);

        claimSagaOrchestrator.startClaimSaga(savedClaim);

        return savedClaim;
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
     * Transaction: Atomic işlem guarantee
     * Cache: Tüm cache'ler temizlenir
     * 
     * @param id Hasar ID'si
     * @param claimDetails Güncellenecek detaylar
     * @return Güncellenmiş hasar
     */
    @Transactional
    @CacheEvict(value = {"claims", "policyClaims", "userClaims"}, allEntries = true)
    public Claim updateClaim(UUID id, Claim claimDetails) {
        log.info("Updating claim with id: {}", id);

        Claim claim = getClaimById(id);
        claim.setDiagnosis(claimDetails.getDiagnosis());
        claim.setHospitalName(claimDetails.getHospitalName());
        claim.setClaimedAmount(claimDetails.getClaimedAmount());
        claim.setDescription(claimDetails.getDescription());

        return claimRepository.save(claim);
    }

    /**
     * Hasar durumu güncelleme
     * 
     * Durum değişiklikleri:
     * - SUBMITTED -> UNDER_REVIEW
     * - UNDER_REVIEW -> APPROVED/REJECTED
     * - APPROVED -> PAYMENT_PROCESSING
     * - PAYMENT_PROCESSING -> PAYMENT_COMPLETED
     * 
     * Transaction: Atomic işlem guarantee
     * Cache: Tüm cache'ler temizlenir
     * 
     * @param id Hasar ID'si
     * @param status Yeni durum
     * @return Güncellenmiş hasar
     */
    @Transactional
    @CacheEvict(value = {"claims", "policyClaims", "userClaims"}, allEntries = true)
    public Claim updateClaimStatus(UUID id, Claim.ClaimStatus status) {
        log.info("Updating claim status to {} for claim id: {}", status, id);

        Claim claim = getClaimById(id);
        claim.setStatus(status);

        return claimRepository.save(claim);
    }

    /**
     * Duruma göre hasar sayısı
     * 
     * İstatistik amaçlı kullanılır:
     * - Dashboard gösterimleri
     * - Raporlama
     * - Performance monitoring
     * 
     * Cache: "claimStatistics" ile cached
     * 
     * @param status Hasar durumu
     * @return Hasar sayısı
     */
    @Cacheable(value = "claimStatistics", key = "'statusCount'")
    public long getClaimCountByStatus(Claim.ClaimStatus status) {
        return claimRepository.countByStatus(status);
    }

    /**
     * Benzersiz hasar numarası oluşturma
     * 
     * Format: CLM-{timestamp}-{random}
     * Örnek: CLM-1234567890-123
     * 
     * @return Benzersiz hasar numarası
     */
    private String generateClaimNumber() {
        return "CLM-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}