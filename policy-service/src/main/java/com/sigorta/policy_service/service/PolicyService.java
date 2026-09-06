package com.sigorta.policy_service.service;

import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Policy Service (Poliçe Servisi)
 * 
 * Bu sınıf, poliçe yönetimi iş mantığını içerir:
 * - CRUD operasyonları
 * - Redis caching ile performans optimizasyonu
 * - Elementer sağlık sigortası yönetimi
 * - Transaction yönetimi
 * 
 * Caching Stratejisi:
 * - @Cacheable: Okuma işlemlerinde cache kullanılır
 * - @CacheEvict: Yazma işlemlerinde cache temizlenir
 * - Cache türleri: policies, personPolicies, activePolicies, expiringPolicies, policyStatistics
 * 
 * Transaction Yönetimi:
 * - @Transactional: Veri tutarlılığı için transaction guarantee
 * - Create/update işlemlerinde atomicity
 * 
 * Ana Metodlar:
 * - getPolicyById: ID ile poliçe getirme (cached)
 * - getPolicyByNumber: Numara ile poliçe getirme (cached)
 * - getPoliciesByTcNo: TC no'ya göre poliçeler (cached)
 * - getActivePolicies: Aktif poliçeler (cached)
 * - getExpiringPolicies: Süresi dolacak poliçeler (cached)
 * - createPolicy: Yeni poliçe oluşturma
 * - updatePolicy: Poliçe güncelleme
 * - updatePolicyStatus: Poliçe durumu güncelleme
 * 
 * @author Insurance Development Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    /**
     * ID ile poliçe getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "policies:{id}"
     * - TTL: 2 saat (config'de tanımlı)
     * - Cache miss: Veritabanından sorgular
     * 
     * @param id Poliçe ID'si
     * @return Poliçe detayları
     */
    @Cacheable(value = "policies", key = "#id")
    public Policy getPolicyById(UUID id) {
        log.info("Fetching policy with id: {}", id);
        return policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + id));
    }

    /**
     * Poliçe numarası ile poliçe getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "policies:{policyNumber}"
     * - Benzersiz poliçe numarası ile sorgulama
     * 
     * @param policyNumber Poliçe numarası
     * @return Poliçe detayları
     */
    @Cacheable(value = "policies", key = "#policyNumber")
    public Policy getPolicyByNumber(String policyNumber) {
        log.info("Fetching policy with number: {}", policyNumber);
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Policy not found with number: " + policyNumber));
    }

    /**
     * TC numarasına göre poliçeleri getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "personPolicies:{tcNo}"
     * - Bir kişinin tüm poliçeleri
     * - Elementer sağlık sigortası sorguları için önemli
     * 
     * @param tcNo TC kimlik numarası
     * @return Poliçe listesi
     */
    @Cacheable(value = "personPolicies", key = "#tcNo")
    public List<Policy> getPoliciesByTcNo(String tcNo) {
        log.info("Fetching policies for TC no: {}", tcNo);
        return policyRepository.findByInsuredPersonTcNo(tcNo);
    }

    /**
     * Belirli bir tarihteki aktif poliçeleri getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "activePolicies:{date}"
     * - O tarihte geçerli olan poliçeler
     * - Hasar validasyonu için kritik
     * 
     * @param currentDate Sorgulama tarihi
     * @return Aktif poliçe listesi
     */
    @Cacheable(value = "activePolicies", key = "#currentDate.toString()")
    public List<Policy> getActivePolicies(LocalDate currentDate) {
        log.info("Fetching active policies for date: {}", currentDate);
        return policyRepository.findActivePoliciesOnDate(currentDate);
    }

    /**
     * Süresi dolacak poliçeleri getirme
     * 
     * Redis cache kullanılır:
     * - Cache key: "expiringPolicies:{startDate}-{endDate}"
     * - Yenileme hatırlatmaları için
     * - Customer retention için önemli
     * 
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Süresi dolacak poliçe listesi
     */
    @Cacheable(value = "expiringPolicies", key = "#startDate.toString() + '-' + #endDate.toString()")
    public List<Policy> getExpiringPolicies(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching policies expiring between {} and {}", startDate, endDate);
        return policyRepository.findPoliciesExpiringBetween(startDate, endDate);
    }

    /**
     * Yeni poliçe oluşturma
     * 
     * İş akışı:
     * 1. Poliçe numarası oluşturur
     * 2. Durumu ACTIVE olarak ayarlar
     * 3. Veritabanına kaydeder
     * 4. Tüm cache'leri temizler
     * 
     * Poliçe Tipleri:
     * - ELEMENTARY_HEALTH: Elementer sağlık
     * - COMPLEMENTARY_HEALTH: Tamamlayıcı sağlık
     * - PRIVATE_HEALTH: Özel sağlık
     * 
     * Transaction: Atomic işlem guarantee
     * Cache: Tüm cache'ler temizlenir
     * 
     * @param policy Oluşturulacak poliçe
     * @return Kaydedilen poliçe
     */
    @Transactional
    @CacheEvict(value = {"policies", "personPolicies", "activePolicies", "expiringPolicies"}, allEntries = true)
    public Policy createPolicy(Policy policy) {
        log.info("Creating new policy for person: {}", policy.getInsuredPersonName());

        policy.setPolicyNumber(generatePolicyNumber());
        policy.setStatus(Policy.PolicyStatus.ACTIVE);

        return policyRepository.save(policy);
    }

    /**
     * Poliçe güncelleme
     * 
     * Güncellenebilir alanlar:
     * - Sigortalı adı
     * - Adres
     * - Telefon
     * - E-posta
     * - Teminat detayları
     * 
     * Transaction: Atomic işlem guarantee
     * Cache: Tüm cache'ler temizlenir
     * 
     * @param id Poliçe ID'si
     * @param policyDetails Güncellenecek detaylar
     * @return Güncellenmiş poliçe
     */
    @Transactional
    @CacheEvict(value = {"policies", "personPolicies", "activePolicies", "expiringPolicies"}, allEntries = true)
    public Policy updatePolicy(UUID id, Policy policyDetails) {
        log.info("Updating policy with id: {}", id);

        Policy policy = getPolicyById(id);
        policy.setInsuredPersonName(policyDetails.getInsuredPersonName());
        policy.setAddress(policyDetails.getAddress());
        policy.setPhone(policyDetails.getPhone());
        policy.setEmail(policyDetails.getEmail());
        policy.setCoverageDetails(policyDetails.getCoverageDetails());

        return policyRepository.save(policy);
    }

    /**
     * Poliçe durumu güncelleme
     * 
     * Durum değişiklikleri:
     * - ACTIVE -> SUSPENDED (askıya alma)
     * - SUSPENDED -> ACTIVE (yeniden aktifleştirme)
     * - ACTIVE -> CANCELLED (iptal)
     * - ACTIVE -> EXPIRED (süre doldu)
     * - EXPIRED -> RENEWED (yenileme)
     * 
     * Transaction: Atomic işlem guarantee
     * Cache: Tüm cache'ler temizlenir
     * 
     * @param id Poliçe ID'si
     * @param status Yeni durum
     * @return Güncellenmiş poliçe
     */
    @Transactional
    @CacheEvict(value = {"policies", "personPolicies", "activePolicies", "expiringPolicies"}, allEntries = true)
    public Policy updatePolicyStatus(UUID id, Policy.PolicyStatus status) {
        log.info("Updating policy status to {} for policy id: {}", status, id);

        Policy policy = getPolicyById(id);
        policy.setStatus(status);

        return policyRepository.save(policy);
    }

    /**
     * Duruma göre poliçe sayısı
     * 
     * İstatistik amaçlı kullanılır:
     * - Dashboard gösterimleri
     * - Raporlama
     * - Performance monitoring
     * - Sales analytics
     * 
     * Cache: "policyStatistics" ile cached
     * 
     * @param status Poliçe durumu
     * @return Poliçe sayısı
     */
    @Cacheable(value = "policyStatistics", key = "'statusCount'")
    public long getPolicyCountByStatus(Policy.PolicyStatus status) {
        return policyRepository.countByStatus(status);
    }

    /**
     * Benzersiz poliçe numarası oluşturma
     * 
     * Format: POL-{timestamp}-{random}
     * Örnek: POL-1234567890-123
     * 
     * @return Benzersiz poliçe numarası
     */
    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}