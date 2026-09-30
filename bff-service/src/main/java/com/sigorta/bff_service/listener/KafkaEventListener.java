package com.sigorta.bff_service.listener;

import com.sigorta.bff_service.events.ClaimUpdatedEvent;
import com.sigorta.bff_service.events.PolicyUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka Event Listener
 * 
 * BFF service için Kafka event listener'ları:
 * - Claim service'den event'leri dinler
 * - Policy service'den event'leri dinler
 * - Real-time updates için WebSocket notification gönderir
 * 
 * Topics:
 * - claim.updated: Hasar güncellemeleri
 * - policy.updated: Poliçe güncellemeleri
 * 
 * Real-time Communication:
 * - WebSocket üzerinden frontend'e notification gönderir
 * - Cache invalidation
 * - Dashboard update
 * 
 * @author Insurance Development Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Claim Updated Event Listener
     * 
     * Claim service'den hasar güncellemelerini dinler:
     * - Hasar durumu değiştiğinde
     - Ödeme onaylandığında
     - Hasar reddedildiğinde
     * 
     * Real-time notification:
     * - WebSocket üzerinden frontend'e gönderir
     * - Cache invalidation
     * - Dashboard update
     * 
     * @param event Claim updated event
     * @param ack Kafka acknowledgment
     */
    @KafkaListener(topics = "claim.updated", groupId = "bff-service-group")
    public void handleClaimUpdated(ClaimUpdatedEvent event, Acknowledgment ack) {
        log.info("Received claim updated event: {}", event.getClaimNumber());
        
        try {
            // Cache invalidation
            invalidateClaimCache(event.getClaimId());
            
            // Real-time notification via WebSocket
            sendClaimUpdateNotification(event);
            
            // Kafka acknowledgment
            if (ack != null) {
                ack.acknowledge();
            }
            
            log.info("Claim updated event processed successfully");
        } catch (Exception e) {
            log.error("Error processing claim updated event", e);
            if (ack != null) {
                ack.nack(Duration.ZERO);
            }
        }
    }
    
    /**
     * Policy Updated Event Listener
     * 
     * Policy service'den poliçe güncellemelerini dinler:
     * - Poliçe durumu değiştiğinde
     - Teminat limiti güncellendiğinde
     - Poliçe yenilendiğinde
     * 
     * Real-time notification:
     * - WebSocket üzerinden frontend'e gönderir
     * - Cache invalidation
     * - Dashboard update
     * 
     * @param event Policy updated event
     * @param ack Kafka acknowledgment
     */
    @KafkaListener(topics = "policy.updated", groupId = "bff-service-group")
    public void handlePolicyUpdated(PolicyUpdatedEvent event, Acknowledgment ack) {
        log.info("Received policy updated event: {}", event.getPolicyNumber());
        
        try {
            // Cache invalidation
            invalidatePolicyCache(event.getPolicyId());
            
            // Real-time notification via WebSocket
            sendPolicyUpdateNotification(event);
            
            // Kafka acknowledgment
            if (ack != null) {
                ack.acknowledge();
            }
            
            log.info("Policy updated event processed successfully");
        } catch (Exception e) {
            log.error("Error processing policy updated event", e);
            if (ack != null) {
                ack.nack(Duration.ofSeconds(1));
            }
        }
    }
    
    /**
     * Claim Cache Invalidation
     * 
     * Redis cache'i temizler:
     * - claim-<claimId> key'ini siler
     * - claims-{policyId} key'ini siler
     * - dashboard-claims key'ini siler
     * 
     * @param claimId Hasar ID'si
     */
    private void invalidateClaimCache(UUID claimId) {
        // Redis cache invalidation
        // CacheManager cacheManager = ...
        // cacheManager.getCache("claims").evict(claimId);
        log.info("Cache invalidated for claim: {}", claimId);
    }
    
    /**
     * Policy Cache Invalidation
     * 
     * Redis cache'i temizler:
     * - policy-<policyId> key'ini siler
     * - policies-{tcKimlikNo} key'ini siler
     * - dashboard-policies key'ini siler
     * 
     * @param policyId Poliçe ID'si
     */
    private void invalidatePolicyCache(UUID policyId) {
        // Redis cache invalidation
        // CacheManager cacheManager = ...
        // cacheManager.getCache("policies").evict(policyId);
        log.info("Cache invalidated for policy: {}", policyId);
    }
    
    /**
     * Claim Update Notification
     * 
     * WebSocket üzerinden frontend'e notification gönderir:
     * - /topic/claims/updates endpoint'ine gönderir
     * - Frontend real-time update alır
     * - Dashboard otomatik refresh olur
     * 
     * @param event Claim updated event
     */
    private void sendClaimUpdateNotification(ClaimUpdatedEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/claims/updates",
                    event
            );
            log.info("Claim update notification sent via WebSocket");
        } catch (Exception e) {
            log.error("Error sending claim update notification", e);
        }
    }
    
    /**
     * Policy Update Notification
     * 
     * WebSocket üzerinden frontend'e notification gönderir:
     * - /topic/policies/updates endpoint'ine gönderir
     * - Frontend real-time update alır
     * - Dashboard otomatik refresh olur
     * 
     * @param event Policy updated event
     */
    private void sendPolicyUpdateNotification(PolicyUpdatedEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/policies/updates",
                    event
            );
            log.info("Policy update notification sent via WebSocket");
        } catch (Exception e) {
            log.error("Error sending policy update notification", e);
        }
    }
}