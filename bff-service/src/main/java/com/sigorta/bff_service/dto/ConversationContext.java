package com.sigorta.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Konuşma Bağlamı DTO
 * 
 * Chatbot konuşma durumunu ve bağlam bilgilerini tutar.
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    
    /**
     * Konuşma ID'si
     */
    private String conversationId;
    
    /**
     * Kullanıcı ID'si
     */
    private String userId;
    
    /**
     * Oturum ID'si
     */
    private String sessionId;
    
    /**
     * Konuşma başlangıç zamanı
     */
    private LocalDateTime startTime;
    
    /**
     * Son aktivite zamanı
     */
    private LocalDateTime lastActivity;
    
    /**
     * Mevcut intent
     */
    private String currentIntent;
    
    /**
     * Mevcut intent confidence
     */
    private Double currentIntentConfidence;
    
    /**
     * Mevcut adım (multi-step işlemler için)
     */
    private String currentStep;
    
    /**
     * Toplanan bilgiler
     * Örnek: policyId, tcKimlikNo, medicalProcedure
     */
    private Map<String, Object> collectedInfo;
    
    /**
     * Kayıp bilgiler (yapılması gereken sorular)
     */
    private List<String> missingInfo;
    
    /**
     * Konuşma geçmişi
     */
    private List<ChatMessage> messageHistory;
    
    /**
     * Konuşma durumu
     * ACTIVE, COMPLETED, EXPIRED
     */
    private String conversationState;
    
    /**
     * Dil tercihi
     */
    private String language;
    
    /**
     * Konuşma priority
     * NORMAL, HIGH, URGENT
     */
    private String priority;
    
    /**
     * Ek metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Mesaj Geçmişi Inner Class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String messageId;
        private String sender; // USER, BOT
        private String message;
        private String intent;
        private Double confidence;
        private LocalDateTime timestamp;
        private Map<String, Object> context;
    }
    
    /**
     * Konuşma başlatma
     */
    public static ConversationContext startConversation(String conversationId, String userId) {
        return ConversationContext.builder()
                .conversationId(conversationId)
                .userId(userId)
                .startTime(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .collectedInfo(new HashMap<>())
                .conversationState("ACTIVE")
                .language("tr")
                .priority("NORMAL")
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * Bilgi ekleme
     */
    public void addCollectedInfo(String key, Object value) {
        if (this.collectedInfo == null) {
            this.collectedInfo = new HashMap<>();
        }
        this.collectedInfo.put(key, value);
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Intent güncelleme
     */
    public void updateIntent(String intent, Double confidence) {
        this.currentIntent = intent;
        this.currentIntentConfidence = confidence;
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Adım güncelleme
     */
    public void updateStep(String step) {
        this.currentStep = step;
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Konuşmayı tamamlama
     */
    public void completeConversation() {
        this.conversationState = "COMPLETED";
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Konuşmayı sonlandırma
     */
    public void expireConversation() {
        this.conversationState = "EXPIRED";
        this.lastActivity = LocalDateTime.now();
    }
}