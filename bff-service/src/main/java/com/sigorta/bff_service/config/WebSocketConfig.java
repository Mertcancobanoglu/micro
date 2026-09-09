package com.sigorta.bff_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Konfigürasyon Sınıfı
 * 
 * BFF service için WebSocket konfigürasyonları:
 * - Real-time frontend communication
 * - Kafka event'lerini WebSocket üzerinden yayınlar
 * - Chatbot için real-time chat
 * - Dashboard real-time updates
 * 
 * WebSocket Endpoints:
 * - /ws/chat: Chatbot için
 * - /ws/dashboard: Dashboard updates için
 * 
 * Message Broker:
 * - /topic/claims/updates: Hasar güncellemeleri
 * - /topic/policies/updates: Poliçe güncellemeleri
 * - /topic/chatbot: Chatbot mesajları
 * 
 * @author Insurance Development Team
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        registry.addEndpoint("/ws/dashboard")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefix("/app");
    }
}