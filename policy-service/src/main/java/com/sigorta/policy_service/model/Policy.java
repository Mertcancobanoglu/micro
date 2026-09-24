package com.sigorta.policy_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Policy (Poliçe) Entity Sınıfı
 * 
 * Bu sınıf, sağlık sigortası poliçelerinin temel veri modelidir.
 * Türk sigortacılık sisteminde kullanılan poliçe tiplerini ve durumlarını içerir.
 * 
 * Temel Özellikler:
 * - UUID tabanlı benzersiz kimlik
 * - Otomatik audit trail (createdAt, updatedAt)
 * - Optimistic locking (version field)
 * - Zengin poliçe tipi ve durum enum'ları
 * - SGK entegrasyonu için alanlar
 * 
 * Poliçe Tipleri (PolicyType):
 * - ELEMENTARY_HEALTH: Elementer sağlık sigortası (temel sağlık güvencesi)
 * - COMPLEMENTARY_HEALTH: Tamamlayıcı sağlık sigortası (SGK farkı karşılayan)
 * - PRIVATE_HEALTH: Özel sağlık sigortası (kapsamlı özel güvence)
 * - FAMILY_HEALTH: Aile sağlık sigortası
 * - SENIOR_HEALTH: Yaşlı sağlık sigortası
 * - STUDENT_HEALTH: Öğrenci sağlık sigortası
 * 
 * Finansal Özellikler:
 * - annualPremium: Yıllık prim tutarı
 * - coverageLimit: Teminat limiti
 * - deductible: Muafiyet tutarı
 * - coPaymentPercentage: Katılım payı yüzdesi
 * 
 * @author Insurance Development Team
 */
@Entity
@Table(name = "policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @Column(nullable = false)
    private String insuredPersonName;

    @Column(nullable = false)
    private String insuredPersonTcNo;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType policyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal annualPremium;

    @Column(nullable = false)
    private BigDecimal coverageLimit;

    @Column(nullable = false)
    private BigDecimal usedCoverage;

    @Column(nullable = false)
    private BigDecimal deductible;

    @Column(nullable = false)
    private Integer coPaymentPercentage;

    @Column(length = 1000)
    private String coverageDetails;

    @Column(length = 1000)
    private String exclusions;

    @Column(nullable = false)
    private String beneficiaryName;

    @Column(nullable = false)
    private String beneficiaryTcNo;

    @Column(nullable = false)
    private String agentId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * Poliçe Tipleri Enum
     * 
     * Türk sigortacılık sisteminde kullanılan standart sağlık sigortası kategorileri
     */
    public enum PolicyType {
        ELEMENTARY_HEALTH,      // Elementer sağlık sigortası - temel sağlık güvencesi
        COMPLEMENTARY_HEALTH,   // Tamamlayıcı sağlık sigortası - SGK farkını karşılar
        PRIVATE_HEALTH,        // Özel sağlık sigortası - kapsamlı özel güvence
        FAMILY_HEALTH,         // Aile sağlık sigortası - aile birimi bazında
        SENIOR_HEALTH,          // Yaşlı sağlık sigortası - 65+ yaş grubu için
        STUDENT_HEALTH          // Öğrenci sağlık sigortası - öğrenciler için uygun fiyatlı
    }

    /**
     * Poliçe Durumları Enum
     * 
     * Poliçenin yaşam döngüsünü temsil eder
     */
    public enum PolicyStatus {
        ACTIVE,              // Aktif - geçerli poliçe
        SUSPENDED,           // Askıya alındı - geçici olarak durduruldu
        CANCELLED,           // İptal edildi - kalıcı olarak iptal
        EXPIRED,             // Süresi doldu - geçerlilik süresi bitti
        PENDING_RENEWAL,     // Yenileme bekliyor - yenileme sürecinde
        RENEWED              // Yenilendi - yeni poliçe ile değiştirildi
    }
}