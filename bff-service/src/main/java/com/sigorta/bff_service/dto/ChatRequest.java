package com.sigorta.bff_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Chatbot İsteği DTO
 * 
 * Sigorta çalışanlarının chatbot ile iletişim kurması için kullanılır.
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * Kullanıcı mesajı
     * Örnek: "Müşteri kalp krizi geçirdi, poliçe kapsar mı?"
     */
    private String message;
    
    /**
     * Konuşma ID'si (konuşma takibi için)
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
     * Ek bağlam bilgileri
     * Örnek: policyId, claimId
     */
    private Map<String, Object> context;
    
    /**
     * İstemci bilgileri
     */
    private ClientInfo clientInfo;
    
    /**
     * Dil tercihi
     */
    private String language;
    
    /**
     * Zaman damgası
     */
    private Long timestamp;
    
    /**
     * İstemci Bilgileri Inner Class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private String platform; // web, mobile, desktop
        private String userAgent;
        private String ipAddress;
        private String location;
    }
}