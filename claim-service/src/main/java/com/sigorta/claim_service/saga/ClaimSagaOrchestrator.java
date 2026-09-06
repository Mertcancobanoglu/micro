package com.sigorta.claim_service.saga;

import com.sigorta.claim_service.model.Claim;
import com.sigorta.claim_service.repository.ClaimRepository;
import com.sigorta.claim_service.saga.events.ClaimSubmittedEvent;
import com.sigorta.claim_service.saga.events.PolicyVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Claim Saga Orchestrator
 * 
 * Bu sınıf, Saga pattern'in orchestrator (orkestratör) rolünü oynar.
 * Hasar işleme sürecini koordine eder ve dağıtık işlemleri yönetir.
 * 
 * Saga Pattern Akışı:
 * 1. Claim oluşturulduğunda saga başlatılır
 * 2. Policy verification event'i Kafka'ya gönderilir
 * 3. Policy service'den response beklenir
 * 4. Policy validasyonuna göre claim onaylanır/reddedilir
 * 5. Sonuç event'i ilgili topic'lere yayınlanır
 * 
 * Kafka Topic'leri:
 * - claim.submitted: Hasar gönderildi event'i
 * - policy.verified: Poliçe doğrulama sonucu
 * - claim.approved: Hasar onaylandı event'i
 * - claim.rejected: Hasar reddedildi event'i
 * 
 * Ödeme Hesaplama:
 * - Onaylanan Tutar = Talep Edilen - Muafiyet - Katılım Payı
 * - Katılım Payı = Talep Edilen * (Katılım Yüzdesi / 100)
 * 
 * @author Insurance Development Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClaimSagaOrchestrator {

    private final ClaimRepository claimRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CLAIM_SUBMITTED_TOPIC = "claim.submitted";
    private static final String CLAIM_APPROVED_TOPIC = "claim.approved";
    private static final String CLAIM_REJECTED_TOPIC = "claim.rejected";

    /**
     * Claim Saga başlatma
     * 
     * Yeni bir hasar için saga sürecini başlatır:
     * - Correlation ID oluşturur
     * - ClaimSubmittedEvent hazırlar
     * - Kafka'ya event gönderir
     * 
     * @param claim İşlenecek hasar
     */
    public void startClaimSaga(Claim claim) {
        String correlationId = UUID.randomUUID().toString();

        ClaimSubmittedEvent event = ClaimSubmittedEvent.builder()
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicyId())
                .insuredPersonName(claim.getInsuredPersonName())
                .insuredPersonTcNo(claim.getInsuredPersonTcNo())
                .claimType(claim.getClaimType().name())
                .incidentDate(claim.getIncidentDate())
                .claimedAmount(claim.getClaimedAmount())
                .hospitalName(claim.getHospitalName())
                .diagnosis(claim.getDiagnosis())
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .build();

        kafkaTemplate.send(CLAIM_SUBMITTED_TOPIC, claim.getClaimNumber(), event);
        log.info("Claim saga started for claim {} with correlationId {}", claim.getClaimNumber(), correlationId);
    }

    /**
     * Policy verification event handler
     * 
     * Policy service'den gelen doğrulama sonucunu işler:
     * - Claim'ı bulur
     * - Policy validasyonuna göre işleme yapar
     * - Onay/red durumuna göre claim günceller
     * 
     * @param event Policy doğrulama event'i
     */
    @KafkaListener(topics = "policy.verified", groupId = "claim-service-group")
    public void handlePolicyVerified(com.sigorta.claim_service.saga.events.PolicyVerifiedEvent event) {
        log.info("Received policy verified event for policy {} with correlationId {}", 
                event.getPolicyId(), event.getCorrelationId());

        try {
            Claim claim = claimRepository.findByClaimNumber(event.getCorrelationId())
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            if (event.isValid()) {
                processApprovedClaim(claim, event);
            } else {
                processRejectedClaim(claim, event.getRejectionReason());
            }
        } catch (Exception e) {
            log.error("Error processing policy verified event", e);
        }
    }

    /**
     * Onaylanan claim işleme
     * 
     * Policy doğrulandığında:
     * - Ödeme tutarını hesaplar (muafiyet ve katılım payı düşülerek)
     * - Claim durumunu APPROVED olarak günceller
     * - Sonuç event'ini yayınlar
     * 
     * Ödeme Formülü:
     * Onaylanan = Talep - Muafiyet - (Talep * Katılım% / 100)
     */
    private void processApprovedClaim(Claim claim, com.sigorta.claim_service.saga.events.PolicyVerifiedEvent policyEvent) {
        BigDecimal deductible = policyEvent.getDeductible();
        BigDecimal coPaymentAmount = claim.getClaimedAmount()
                .multiply(BigDecimal.valueOf(policyEvent.getCoPaymentPercentage()))
                .divide(BigDecimal.valueOf(100));

        BigDecimal approvedAmount = claim.getClaimedAmount()
                .subtract(deductible)
                .subtract(coPaymentAmount);

        claim.setApprovedAmount(approvedAmount.max(BigDecimal.ZERO));
        claim.setRejectedAmount(BigDecimal.ZERO);
        claim.setStatus(Claim.ClaimStatus.APPROVED);
        claimRepository.save(claim);

        log.info("Claim {} approved with amount {}", claim.getClaimNumber(), approvedAmount);

        kafkaTemplate.send(CLAIM_APPROVED_TOPIC, claim.getClaimNumber(), 
                buildClaimEvent(claim, "APPROVED"));
    }

    /**
     * Reddedilen claim işleme
     * 
     * Policy doğrulanamadığında:
     * - Tüm tutarı reddedilir
     * - Claim durumunu REJECTED olarak günceller
     * - Sonuç event'ini yayınlar
     */
    private void processRejectedClaim(Claim claim, String rejectionReason) {
        claim.setApprovedAmount(BigDecimal.ZERO);
        claim.setRejectedAmount(claim.getClaimedAmount());
        claim.setStatus(Claim.ClaimStatus.REJECTED);
        claimRepository.save(claim);

        log.info("Claim {} rejected: {}", claim.getClaimNumber(), rejectionReason);

        kafkaTemplate.send(CLAIM_REJECTED_TOPIC, claim.getClaimNumber(), 
                buildClaimEvent(claim, "REJECTED"));
    }

    /**
     * Claim event oluşturma
     * 
     * Sonuç event'lerini oluşturur ve diğer servisler için yayınlar
     */
    private com.sigorta.claim_service.saga.events.ClaimEvent buildClaimEvent(Claim claim, String eventType) {
        return com.sigorta.claim_service.saga.events.ClaimEvent.builder()
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicyId())
                .eventType(eventType)
                .claimedAmount(claim.getClaimedAmount())
                .timestamp(LocalDateTime.now())
                .correlationId(claim.getClaimNumber())
                .build();
    }
}