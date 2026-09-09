package com.sigorta.bff_service.controller;

import com.sigorta.bff_service.dto.ChatRequest;
import com.sigorta.bff_service.dto.ChatResponse;
import com.sigorta.bff_service.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Chatbot Controller
 * 
 * Sigorta çalışanları için AI-powered chatbot endpoint'leri sağlar.
 * 
 * Endpoint'ler:
 * - REST API: Tekil mesaj işleme
 * - WebSocket: Real-time iletişim
 * 
 * @author Insurance Development Team
 */
@RestController
@RequestMapping("/api/bff/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chatbot", description = "AI-powered chatbot for insurance employees")
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    
    /**
     * REST API - Chatbot mesajını işle
     * 
     * Tekil mesajlar için kullanılır.
     * 
     * @param request Chatbot isteği
     * @return Chatbot cevabı
     */
    @PostMapping("/message")
    @Operation(summary = "Process chatbot message",
            description = "Send a message to the chatbot and get AI-powered response. " +
                    "Supports policy validation, document recommendations, and quick checks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ChatResponse> processMessage(
            @Parameter(description = "Chatbot message request", required = true)
            @RequestBody ChatRequest request) {
        
        log.info("REST API chatbot message: {}", request.getMessage());
        
        // Conversation ID yoksa oluştur
        if (request.getConversationId() == null) {
            request.setConversationId(UUID.randomUUID().toString());
        }
        
        try {
            ChatResponse response = chatbotService.processMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chatbot message", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * REST API - Yeni konuşma başlat
     * 
     * Fresh conversation başlatır.
     * 
     * @param userId Kullanıcı ID'si
     * @return Conversation ID
     */
    @PostMapping("/conversation/start")
    @Operation(summary = "Start new conversation",
            description = "Start a fresh conversation with the chatbot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation started successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ConversationStartResponse> startConversation(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId) {
        
        log.info("Starting new conversation for user: {}", userId);
        
        String conversationId = UUID.randomUUID().toString();
        
        ConversationStartResponse response = ConversationStartResponse.builder()
                .conversationId(conversationId)
                .userId(userId)
                .message("Yeni konuşma başlatıldı. Size nasıl yardımcı olabilirim?")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * REST API - Konuşmayı sonlandır
     * 
     * Mevcut konuşmayı sonlandırır.
     * 
     * @param conversationId Konuşma ID'si
     * @return Başarı durumu
     */
    @PostMapping("/conversation/end")
    @Operation(summary = "End conversation",
            description = "End the current conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation ended successfully"),
            @ApiResponse(responseCode = "404", description = "Conversation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> endConversation(
            @Parameter(description = "Conversation ID", required = true)
            @RequestParam String conversationId) {
        
        log.info("Ending conversation: {}", conversationId);
        
        // ChatbotService'te conversation cleanup logic
        // chatbotService.endConversation(conversationId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * WebSocket - Real-time chat
     * 
     * WebSocket üzerinden real-time iletişim.
     * 
     * @param request Chatbot isteği
     * @return Chatbot cevabı
     */
    @MessageMapping("/chat")
    @SendTo("/topic/chatbot")
    @Operation(summary = "WebSocket chat endpoint",
            description = "Real-time chat via WebSocket. Send messages to /app/chat and receive responses on /topic/chatbot")
    public ChatResponse processWebSocketMessage(ChatRequest request) {
        log.info("WebSocket chatbot message: {}", request.getMessage());
        
        // Conversation ID yoksa oluştur
        if (request.getConversationId() == null) {
            request.setConversationId(UUID.randomUUID().toString());
        }
        
        try {
            return chatbotService.processMessage(request);
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            return createErrorResponse(e.getMessage());
        }
    }
    
    /**
     * Conversation Start Response DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ConversationStartResponse {
        private String conversationId;
        private String userId;
        private String message;
    }
    
    /**
     * Error response oluştur
     */
    private ChatResponse createErrorResponse(String errorMessage) {
        return ChatResponse.builder()
                .message("❌ Bir hata oluştu: " + errorMessage)
                .responseType("ERROR")
                .status("ERROR")
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}