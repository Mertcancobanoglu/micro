package com.sigorta.bff_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Chatbot Message Event
 * 
 * Chatbot mesajları için Kafka event'i:
 * - Employee chatbot kullandığında event gönderir
 * - Analytics için kullanılır
 * - Audit trail için
 * 
 * @author Insurance Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessageEvent {
    
    private UUID conversationId;
    private String userId;
    private String message;
    private String intent;
    private String response;
    private LocalDateTime timestamp;
    private String correlationId;
    private Map<String, Object> metadata;
}