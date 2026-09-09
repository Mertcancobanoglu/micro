package com.sigorta.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Poliçe Validasyon Cevabı DTO
 * 
 * AI tarafından analiz edilmiş poliçe kapsamı detaylarını içerir.
 * 
 * Örnek Senaryo:
 * "Evet, bu poliçe bu işlemi kapsar. Kalp krizi tedavisi teminat dahilindedir.
 *  Tahmini ödeme: 12,000 TL, Ödemeniz gereken: 1,800 TL"
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyValidationResponse {
    
    /**
     * Validasyon sonucu
     * true: Poliçe işlemi kapsar
     * false: Poliçe işlemi kapsamaz
     */
    private Boolean isCovered;
    
    /**
     * AI tarafından verilen ana karar
     * Örnek: "Evet, bu poliçe bu işlemi kapsar"
     */
    private String decision;
    
    /**
     * Detaylı açıklama
     * Örnek: "Kalp krizi tedavisi teminat dahilindedir. Acil durum olarak değerlendirilmiştir."
     */
    private String explanation;
    
    /**
     * Teminat limiti
     */
    private BigDecimal coverageLimit;
    
    /**
     * Kullanılan teminat
     */
    private BigDecimal usedCoverage;
    
    /**
     * Kalan teminat
     */
    private BigDecimal remainingCoverage;
    
    /**
     * Tahmini ödeme miktarı
     */
    private BigDecimal estimatedPayment;
    
    /**
     * Müşterinin ödemesi gereken (muafiyet + katılım payı)
     */
    private BigDecimal customerPayment;
    
    /**
     * Teminat kategorisi
     * Örnek: "ACIL_DURUM", "YATAKLI_TEDAVI", "POLIKLINIK"
     */
    private String coverageCategory;
    
    /**
     * Muafiyet miktarı
     */
    private BigDecimal deductible;
    
    /**
     * Katılım payı yüzdesi
     */
    private BigDecimal coPaymentPercentage;
    
    /**
     * İşlem için uygulanacak kurallar
     */
    private List<String> applicableRules;
    
    /**
     * Olası riskler veya uyarılar
     * Örnek: ["Önceki hasarlar yoğun", "Teminat limitine yakın"]
     */
    private List<String> warnings;
    
    /**
     * Öneriler
     * Örnek: ["Ek doküman gerekli", "Önceden onay almalı"]
     */
    private List<String> recommendations;
    
    /**
     * Gerekli dokümanlar
     * Örnek: ["Hasta raporu", "Fatura", "Kimlik fotokopisi"]
     */
    private List<String> requiredDocuments;
    
    /**
     * AI güven oranı (0-1)
     */
    private Double confidence;
    
    /**
     * AI tarafından tespit edilen önemli anahtar kelimeler
     */
    private List<String> detectedKeywords;
    
    /**
     * İşlem kategorisi
     */
    private String procedureCategory;
    
    /**
     * Acil durum mu
     */
    private Boolean isEmergency;
    
    /**
     * İşlem öncesi onay gerekli mi
     */
    private Boolean preApprovalRequired;
    
    /**
     * Özel durumlar
     * Örnek: "Yurt dışı tedavi", "Premium teminat"
     */
    private List<String> specialConditions;
    
    /**
     * İlgili poliçe detayları
     */
    private PolicyDetails policyDetails;
    
    /**
     * Analiz tarihi
     */
    private LocalDate analysisDate;
    
    /**
     * Kaynak (AI/Rule-based)
     */
    private String analysisSource;
    
    /**
     * Ek bilgiler
     */
    private Map<String, Object> additionalInfo;
    
    /**
     * Poliçe Detayları Inner Class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyDetails {
        private String policyNumber;
        private String policyType;
        private String policyStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal annualPremium;
        private String insuredPersonName;
        private String tcKimlikNo;
    }
}