package com.sigorta.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chatbot Cevabı DTO
 * 
 * Chatbot yanıt formatını içerir.
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * Chatbot cevabı
     */
    private String message;
    
    /**
     * Konuşma ID'si
     */
    private String conversationId;
    
    /**
     * Mesaj ID'si
     */
    private String messageId;
    
    /**
     * Yanıt tipi
     * TEXT, CARD, ACTION, ERROR
     */
    private String responseType;
    
    /**
     * Tespit edilen intent
     * POLICY_VALIDATION, DOCUMENT_RECOMMENDATION, QUICK_CHECK, etc.
     */
    private String intent;
    
    /**
     * Confidence skoru (0-1)
     */
    private Double confidence;
    
    /**
     * Ek bağlam bilgileri
     */
    private Map<String, Object> context;
    
    /**
     * Önerilen aksiyonlar
     */
    private List<SuggestedAction> suggestedActions;
    
    /**
     * Ek veriler (policy validation result, etc.)
     */
    private Map<String, Object> data;
    
    /**
     * Takip edilebilir sorular
     */
    private List<String> followUpQuestions;
    
    /**
     * Durum
     * SUCCESS, ERROR, REQUIRE_MORE_INFO
     */
    private String status;
    
    /**
     * Hata mesajı (varsa)
     */
    private String errorMessage;
    
    /**
     * Zaman damgası
     */
    private Long timestamp;
    
    /**
     * İşlem süresi (ms)
     */
    private Long processingTime;
    
    /**
     * Önerilen Aksiyon Inner Class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedAction {
        private String type; // BUTTON, LINK, INPUT
        private String label;
        private String action;
        private Map<String, Object> parameters;
        private String icon;
    }
}