package com.sigorta.policy_service.saga;

import com.sigorta.policy_service.model.Policy;
import com.sigorta.policy_service.repository.PolicyRepository;
import com.sigorta.policy_service.saga.events.ClaimSubmittedEvent;
import com.sigorta.policy_service.saga.events.PolicyVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Policy Saga Handler
 * 
 * Bu sınıf, Saga pattern'in participant (katılımcı) rolünü oynar.
 * Claim service'den gelen hasar doğrulama isteklerini işler ve poliçe validasyonu yapar.
 * 
 * Saga Pattern Rolü:
 * - Claim service'den gelen event'leri dinler
 * - Policy validasyonu gerçekleştirir
 * - Doğrulama sonucunu claim service'e geri gönderir
 * 
 * Policy Validasyon Kuralları:
 * 1. Poliçe durumu ACTIVE olmalı
 * 2. Hasar tarihi poliçe geçerlilik süresi içinde olmalı
 * 3. Sigortalı TC numarası eşleşmeli
 * 4. Talep edilen tutar teminat limitini aşmamalı
 * 
 * Kafka Topic'leri:
 * - claim.submitted: Hasar gönderildi event'i (dinler)
 * - policy.verified: Poliçe doğrulama sonucu (yayınlar)
 * 
 * Hata Yönetimi:
 * - Policy bulunamazsa error event gönderilir
 * - Validasyon başarısız olursa neden belirtilir
 * - Tüm hatalar log'lanır
 * 
 * @author Insurance Development Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PolicySagaHandler {

    private final PolicyRepository policyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String POLICY_VERIFIED_TOPIC = "policy.verified";

    /**
     * Claim submitted event handler
     * 
     * Claim service'den gelen hasar event'ini işler:
     * - Policy'ı veritabanından bulur
     * - Policy validasyonu yapar
     * - Sonuç event'ini claim service'e gönderir
     * 
     * @param event Hasar gönderildi event'i
     */
    @KafkaListener(topics = "claim.submitted", groupId = "policy-service-group")
    public void handleClaimSubmitted(ClaimSubmittedEvent event) {
        log.info("Received claim submitted event for claim {} with correlationId {}", 
                event.getClaimNumber(), event.getCorrelationId());

        try {
            Policy policy = policyRepository.findById(event.getPolicyId())
                    .orElseThrow(() -> new RuntimeException("Policy not found"));

            PolicyVerifiedEvent verifiedEvent = verifyPolicy(policy, event);
            kafkaTemplate.send(POLICY_VERIFIED_TOPIC, event.getClaimNumber(), verifiedEvent);

            log.info("Policy verification completed for policy {} with result: {}", 
                    policy.getPolicyNumber(), verifiedEvent.isValid());

        } catch (Exception e) {
            log.error("Error processing claim submitted event", e);

            PolicyVerifiedEvent errorEvent = PolicyVerifiedEvent.builder()
                    .policyId(event.getPolicyId())
                    .policyNumber("UNKNOWN")
                    .policyType("UNKNOWN")
                    .policyStatus("UNKNOWN")
                    .coverageLimit(BigDecimal.ZERO)
                    .deductible(BigDecimal.ZERO)
                    .coPaymentPercentage(0)
                    .isValid(false)
                    .rejectionReason("Policy not found: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .correlationId(event.getCorrelationId())
                    .build();

            kafkaTemplate.send(POLICY_VERIFIED_TOPIC, event.getClaimNumber(), errorEvent);
        }
    }

    /**
     * Policy validasyonu
     * 
     * Poliçeyi hasar talebine göre doğrular:
     * 1. Durum kontrolü (ACTIVE olmalı)
     * 2. Tarih kontrolü (geçerlilik süresi içinde olmalı)
     * 3. Sigortalı kontrolü (TC numarası eşleşmeli)
     * 4. Limit kontrolü (tutar aşmamalı)
     * 
     * @param policy Doğrulanacak poliçe
     * @param claimEvent Hasar event'i
     * @return Doğrulama sonucu event'i
     */
    private PolicyVerifiedEvent verifyPolicy(Policy policy, ClaimSubmittedEvent claimEvent) {
        boolean isValid = true;
        String rejectionReason = null;

        LocalDate currentDate = LocalDate.now();

        // 1. Durum kontrolü
        if (policy.getStatus() != Policy.PolicyStatus.ACTIVE) {
            isValid = false;
            rejectionReason = "Policy is not active. Current status: " + policy.getStatus();
        } 
        // 2. Tarih kontrolü
        else if (currentDate.isBefore(policy.getStartDate()) || currentDate.isAfter(policy.getEndDate())) {
            isValid = false;
            rejectionReason = "Policy is not valid on the incident date. Valid period: " + 
                    policy.getStartDate() + " to " + policy.getEndDate();
        } 
        // 3. Sigortalı kontrolü
        else if (!policy.getInsuredPersonTcNo().equals(claimEvent.getInsuredPersonTcNo())) {
            isValid = false;
            rejectionReason = "Insured person TC number does not match";
        } 
        // 4. Limit kontrolü
        else if (claimEvent.getClaimedAmount().compareTo(policy.getCoverageLimit()) > 0) {
            isValid = false;
            rejectionReason = "Claimed amount exceeds coverage limit. Claimed: " + 
                    claimEvent.getClaimedAmount() + ", Limit: " + policy.getCoverageLimit();
        }

        return PolicyVerifiedEvent.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyType(policy.getPolicyType().name())
                .policyStatus(policy.getStatus().name())
                .coverageLimit(policy.getCoverageLimit())
                .deductible(policy.getDeductible())
                .coPaymentPercentage(policy.getCoPaymentPercentage())
                .isValid(isValid)
                .rejectionReason(rejectionReason)
                .timestamp(LocalDateTime.now())
                .correlationId(claimEvent.getCorrelationId())
                .build();
    }
}