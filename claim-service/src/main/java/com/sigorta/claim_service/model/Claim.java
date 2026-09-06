package com.sigorta.claim_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Claim (Hasar) Entity Sınıfı
 * 
 * Bu sınıf, sigortacılık hasar modülünün temel veri modelidir.
 * Sigorta hasarlarının tüm bilgilerini ve durumunu tutar.
 * 
 * Temel Özellikler:
 * - UUID tabanlı benzersiz kimlik
 * - Otomatik audit trail (createdAt, updatedAt)
 * - Optimistic locking (version field)
 * - Zengin hasar tipi ve durum enum'ları
 * 
 * Hasar Yaşam Döngüsü (ClaimStatus):
 * DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED/REJECTED -> PAYMENT_PROCESSING -> PAYMENT_COMPLETED -> CLOSED
 * 
 * Hasar Tipleri (ClaimType):
 * - INPATIENT: Yatarak tedavi (ameliyat, yoğun bakım vb.)
 * - OUTPATIENT: Ayakta tedavi (doktor muayenesi)
 * - EMERGENCY: Acil durum
 * - CHECKUP: Check-up ve kontroller
 * - SURGERY: Cerrahi müdahaleler
 * - MEDICAL_IMAGING: Görüntüleme (MR, CT, röntgen)
 * - PHYSIOTHERAPY: Fizik tedavi
 * - DENTAL: Diş tedavileri
 * 
 * @author Insurance Development Team
 */
@Entity
@Table(name = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @Column(nullable = false)
    private UUID policyId;

    @Column(nullable = false)
    private String insuredPersonName;

    @Column(nullable = false)
    private String insuredPersonTcNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(nullable = false)
    private LocalDateTime incidentDate;

    @Column(nullable = false)
    private LocalDateTime reportDate;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    private String diagnosis;

    @Column(nullable = false)
    private BigDecimal claimedAmount;

    private BigDecimal approvedAmount;

    private BigDecimal rejectedAmount;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String reportedBy;

    @Column(nullable = false)
    private String assignedTo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * Hasar Tipleri Enum
     * 
     * Turkish sigortacılık sisteminde kullanılan standart hasar kategorileri
     */
    public enum ClaimType {
        INPATIENT,           // Yatarak tedavi
        OUTPATIENT,          // Ayakta tedavi
        EMERGENCY,           // Acil servis
        CHECKUP,             // Check-up
        SURGERY,             // Cerrahi
        MEDICAL_IMAGING,     // Görüntüleme
        PHYSIOTHERAPY,       // Fizik tedavi
        DENTAL               // Diş tedavisi
    }

    /**
     * Hasar Durumları Enum
     * 
     * Hasarın yaşam döngüsünü temsil eder
     */
    public enum ClaimStatus {
        DRAFT,                      // Taslak - henüz gönderilmedi
        SUBMITTED,                  // Gönderildi - inceleme bekliyor
        UNDER_REVIEW,               // İnceleniyor
        ADDITIONAL_INFO_REQUIRED,   // Ek bilgi gerekli
        APPROVED,                   // Onaylandı
        PARTIALLY_APPROVED,         // Kısmen onaylandı
        REJECTED,                   // Reddedildi
        PAYMENT_PROCESSING,         // Ödeme işleniyor
        PAYMENT_COMPLETED,          // Ödeme tamamlandı
        CLOSED                      // Kapatıldı
    }
}