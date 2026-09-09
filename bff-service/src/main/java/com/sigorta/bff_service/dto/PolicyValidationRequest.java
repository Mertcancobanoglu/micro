package com.sigorta.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Poliçe Validasyon İsteği DTO
 * 
 * Sigorta çalışanlarının müşterinin poliçe kapsamını sorgulaması için kullanılır.
 * 
 * Örnek Senaryo:
 * "Müşteri hastanede x işlemini gerçekleştiriyor, bu kullanıcının poliçesi buna uygun mu?"
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyValidationRequest {
    
    /**
     * Poliçe ID
     */
    private String policyId;
    
    /**
     * Poliçe numarası (ID yoksa kullanılır)
     */
    private String policyNumber;
    
    /**
     * Müşteri TC kimlik numarası
     */
    private String tcKimlikNo;
    
    /**
     * Yapılan tıbbi işlem veya tedavi açıklaması
     * Örnek: "Hasta acil servise girdi, kalp krizi teşhisi konuldu, stent takıldı"
     */
    private String medicalProcedure;
    
    /**
     * Hastane adı
     */
    private String hospitalName;
    
    /**
     * İşlem tarihi
     */
    private LocalDate procedureDate;
    
    /**
     * Tahmini işlem maliyeti
     */
    private BigDecimal estimatedCost;
    
    /**
     * İşlem kategorisi
     * Örnek: "HASTANE_DOKTOR", "LABORATUVAR", "ILAÇ", "CERRAHI", "CHECKUP"
     */
    private String procedureCategory;
    
    /**
     * Tedavi alan kişi (sigortalı değilse)
     */
    private String treatedPerson;
    
    /**
     * Acil durum mu
     */
    private Boolean isEmergency;
    
    /**
     * Ek doküman açıklamaları
     */
    private List<String> additionalInfo;
    
    /**
     * Çalışan sorusu
     * Örnek: "Bu kullanıcının poliçesi buna uygun mu?"
     */
    private String employeeQuestion;
}