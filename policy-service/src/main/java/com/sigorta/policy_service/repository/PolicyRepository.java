package com.sigorta.policy_service.repository;

import com.sigorta.policy_service.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Policy Repository (Poliçe Veri Erişim Katmanı)
 * 
 * Bu repository, poliçe verilerinin veritabanı işlemlerini yönetir:
 * - Spring Data JPA ile otomatik CRUD operasyonları
 * - Custom query metodları
 * - JPQL query'leri
 * - Elementer sağlık sigortası sorguları
 * 
 * Standart Metodlar (JpaRepository):
 * - save(): Poliçe kaydetme/güncelleme
 * - findById(): ID ile poliçe getirme
 * - findAll(): Tüm poliçeleri getirme
 * - delete(): Poliçe silme
 * - count(): Poliçe sayısı
 * 
 * Custom Metodlar:
 * - findByPolicyNumber: Poliçe numarası ile sorgulama
 * - findByInsuredPersonTcNo: TC no'ya göre sorgulama
 * - findByStatus: Durum ile sorgulama
 * - findByPolicyType: Poliçe tipi ile sorgulama
 * - findActivePoliciesOnDate: Tarih bazlı aktif poliçeler
 * - findPoliciesExpiringBetween: Süresi dolacak poliçeler
 * - findByAgentIdOrderByCreatedAtDesc: Acente bazlı sorgulama
 * - countByStatus: Durum bazlı sayım
 * - findByInsuredPersonNameContaining: Ada göre arama
 * 
 * Saga Pattern Entegrasyonu:
 * - Policy validasyonu için kritik
 * - Hasar doğrulama sorguları
 * - Active policy kontrolü
 * 
 * @author Insurance Development Team
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {

    /**
     * Poliçe numarası ile poliçe bulma
     * 
     * @param policyNumber Benzersiz poliçe numarası
     * @return Poliçe (bulunamazsa empty)
     */
    Optional<Policy> findByPolicyNumber(String policyNumber);

    /**
     * TC kimlik numarasına göre poliçeleri bulma
     * 
     * Elementer sağlık sigortası için kritik:
     * - Bir kişinin tüm poliçeleri
     * - Multi-policy senaryoları
     * - SGK entegrasyonu için
     * 
     * @param tcNo TC kimlik numarası
     * @return Poliçe listesi
     */
    List<Policy> findByInsuredPersonTcNo(String tcNo);

    /**
     * Duruma göre poliçeleri bulma
     * 
     * @param status Poliçe durumu
     * @return Poliçe listesi
     */
    List<Policy> findByStatus(Policy.PolicyStatus status);

    /**
     * Poliçe tipine göre poliçeleri bulma
     * 
     * @param policyType Poliçe tipi (ELEMENTARY_HEALTH, COMPLEMENTARY_HEALTH, vb.)
     * @return Poliçe listesi
     */
    List<Policy> findByPolicyType(Policy.PolicyType policyType);

    /**
     * Belirli bir tarihteki aktif poliçeleri bulma
     * 
     * Saga pattern için kritik:
     * - Hasar validasyonu
     * - Policy coverage kontrolü
     * - Geçerlilik tarihi kontrolü
     * 
     * Koşullar:
     * - startDate <= currentDate <= endDate
     * - status = ACTIVE
     * 
     * @param currentDate Sorgulama tarihi
     * @return Aktif poliçe listesi
     */
    @Query("SELECT p FROM Policy p WHERE p.startDate <= :currentDate AND p.endDate >= :currentDate AND p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesOnDate(@Param("currentDate") LocalDate currentDate);

    /**
     * Süresi dolacak poliçeleri bulma
     * 
     * Customer retention için:
     * - Yenileme hatırlatmaları
     * - İletişim kampanyaları
     * - Premium ödemeleri
     * 
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Süresi dolacak poliçe listesi
     */
    @Query("SELECT p FROM Policy p WHERE p.endDate BETWEEN :startDate AND :endDate AND p.status = 'ACTIVE'")
    List<Policy> findPoliciesExpiringBetween(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Acente ID'sine göre poliçeleri bulma
     * Oluşturulma tarihine göre sıralı (en yeni önce)
     * 
     * @param agentId Acente ID'si
     * @return Poliçe listesi (createdAt desc)
     */
    @Query("SELECT p FROM Policy p WHERE p.agentId = :agentId ORDER BY p.createdAt DESC")
    List<Policy> findByAgentIdOrderByCreatedAtDesc(@Param("agentId") String agentId);

    /**
     * Duruma göre poliçe sayısı
     * 
     * İstatistik ve raporlama için:
     * - Dashboard gösterimleri
     * - Sales analytics
     * - Performance monitoring
     * 
     * @param status Poliçe durumu
     * @return Poliçe sayısı
     */
    @Query("SELECT COUNT(p) FROM Policy p WHERE p.status = :status")
    long countByStatus(@Param("status") Policy.PolicyStatus status);

    /**
     * İsim içerme araması
     * 
     * Arama ve filtreleme için:
     * - Customer search
     * - Admin paneli aramaları
     * - Reporting araçları
     * 
     * @param name Aranan isim (parçalı eşleşme)
     * @return Poliçe listesi
     */
    @Query("SELECT p FROM Policy p WHERE p.insuredPersonName LIKE %:name%")
    List<Policy> findByInsuredPersonNameContaining(@Param("name") String name);
}