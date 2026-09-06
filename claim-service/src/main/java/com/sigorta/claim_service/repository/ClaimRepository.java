package com.sigorta.claim_service.repository;

import com.sigorta.claim_service.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Claim Repository (Hasar Veri Erişim Katmanı)
 * 
 * Bu repository, hasar verilerinin veritabanı işlemlerini yönetir:
 * - Spring Data JPA ile otomatik CRUD operasyonları
 * - Custom query metodları
 * - JPQL query'leri
 * 
 * Standart Metodlar (JpaRepository):
 * - save(): Hasar kaydetme/güncelleme
 * - findById(): ID ile hasar getirme
 * - findAll(): Tüm hasarları getirme
 * - delete(): Hasar silme
 * - count(): Hasar sayısı
 * 
 * Custom Metodlar:
 * - findByClaimNumber: Hasar numarası ile sorgulama
 * - findByPolicyId: Poliçe ID'si ile sorgulama
 * - findByStatus: Durum ile sorgulama
 * - findByAssignedTo: Atanan kişi ile sorgulama
 * - findByReportedBy: Raporlayan kişi ile sorgulama
 * - findByIncidentDateBetween: Tarih aralığı ile sorgulama
 * - countByStatus: Durum bazlı sayım
 * 
 * Query Optimization:
 * - Index'li alanlar için özel metodlar
 * - N+1 query probleminden kaçınma
 * - Lazy loading stratejisi
 * 
 * @author Insurance Development Team
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    /**
     * Hasar numarası ile hasar bulma
     * 
     * @param claimNumber Benzersiz hasar numarası
     * @return Hasar (bulunamazsa empty)
     */
    Optional<Claim> findByClaimNumber(String claimNumber);

    /**
     * Poliçe ID'sine göre hasarları bulma
     * 
     * @param policyId Poliçe UUID'si
     * @return Hasar listesi
     */
    List<Claim> findByPolicyId(UUID policyId);

    /**
     * Duruma göre hasarları bulma
     * 
     * @param status Hasar durumu
     * @return Hasar listesi
     */
    List<Claim> findByStatus(Claim.ClaimStatus status);

    /**
     * Atanan kişiye göre hasarları bulma
     * 
     * @param assignedTo Atanan kişinin kullanıcı adı
     * @return Hasar listesi
     */
    List<Claim> findByAssignedTo(String assignedTo);

    /**
     * Raporlayan kişiye göre hasarları bulma
     * Rapor tarihine göre sıralı (en yeni önce)
     * 
     * @param reportedBy Raporlayan kişinin kullanıcı adı
     * @return Hasar listesi (reportDate desc)
     */
    List<Claim> findByReportedByOrderByReportDateDesc(String reportedBy);

    /**
     * Tarih aralığına göre hasarları bulma
     * 
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return Hasar listesi
     */
    @Query("SELECT c FROM Claim c WHERE c.incidentDate BETWEEN :startDate AND :endDate")
    List<Claim> findByIncidentDateBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Duruma göre hasar sayısı
     * 
     * İstatistik ve raporlama için kullanılır:
     * - Dashboard gösterimleri
     * - Performance monitoring
     * - KPI hesaplamaları
     * 
     * @param status Hasar durumu
     * @return Hasar sayısı
     */
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = :status")
    long countByStatus(@Param("status") Claim.ClaimStatus status);
}