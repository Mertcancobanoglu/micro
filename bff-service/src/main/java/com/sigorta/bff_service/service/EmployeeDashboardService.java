package com.sigorta.bff_service.service;

import com.sigorta.bff_service.dto.PolicyValidationRequest;
import com.sigorta.bff_service.dto.PolicyValidationResponse;
import com.sigorta.bff_service.events.DashboardInsightEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Employee Dashboard Service
 * 
 * Sigorta çalışanları için AI-powered policy validation ve document recommendation sağlar.
 * 
 * AI Entegrasyonu:
 * - OpenAI GPT-4 ile policy analysis
 * - Mock implementation (test için)
 * - Fallback logic (AI çökerse rule-based)
 * 
 * @author Insurance Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeDashboardService {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.model:gpt-4}")
    private String aiModel;

    @Value("${downstream.policy-service.url}")
    private String policyServiceUrl;

    @Value("${downstream.claim-service.url}")
    private String claimServiceUrl;
    
    /**
     * Poliçe Validasyon
     * 
     * 1. Policy bilgilerini al
     * 2. Medical procedure'ı analiz et
     * 3. Coverage kontrolü yap
     * 4. Ödeme hesapla
     * 5. Doküman önerisi
     */
    public PolicyValidationResponse validatePolicy(PolicyValidationRequest request) {
        log.info("Validating policy for procedure: {}", request.getMedicalProcedure());
        
        try {
            // 1. Policy bilgilerini al
            Map<String, Object> policyData = getPolicyData(request.getPolicyId(), request.getPolicyNumber());
            
            if (Objects.isNull(policyData)) {
                return createNotFoundResponse();
            }
            
            // 2. AI veya rule-based analysis
            PolicyValidationResponse response;
            String analysisSource;
            
            if (aiEnabled) {
                response = aiBasedValidation(request, policyData);
                analysisSource = "AI";
            } else {
                response = ruleBasedValidation(request, policyData);
                analysisSource = "RULE_BASED";
            }
            
            // 3. Kafka event gönder - analytics için
            publishDashboardInsightEvent("VALIDATION", request, response.getDecision(), analysisSource);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error in policy validation", e);
            return createErrorResponse(e.getMessage());
        }
    }
    
    /**
     * AI-Based Validation
     * 
     * OpenAI API çağrısı yaparak kapsamlı analiz sağlar
     */
    private PolicyValidationResponse aiBasedValidation(PolicyValidationRequest request, 
                                                        Map<String, Object> policyData) {
        
        log.info("Using AI-based validation");
        
        // AI prompt oluştur
        String prompt = buildAIPrompt(request, policyData);
        
        try {
            // OpenAI API çağrısı (simüle edilmiş)
            String aiResponse = callOpenAI(prompt);
            
            // AI response'u parse et
            return parseAIResponse(aiResponse, policyData);
            
        } catch (Exception e) {
            log.error("AI validation failed, falling back to rule-based", e);
            return ruleBasedValidation(request, policyData);
        }
    }
    
    /**
     * Rule-Based Validation
     * 
     * AI yoksa veya hata olduğunda kullanılır
     */
    private PolicyValidationResponse ruleBasedValidation(PolicyValidationRequest request, 
                                                         Map<String, Object> policyData) {
        
        log.info("Using rule-based validation");
        
        // Policy details al
        String policyType = (String) policyData.get("policyType");
        String policyStatus = (String) policyData.get("policyStatus");
        BigDecimal coverageLimit = getBigDecimal(policyData, "coverageLimit");
        BigDecimal deductible = getBigDecimal(policyData, "deductible");
        BigDecimal coPaymentPercentage = getBigDecimal(policyData, "coPaymentPercentage");
        
        // Basit validasyon kuralları
        boolean isCovered = isCoveredByRules(request, policyType, policyStatus);
        boolean isEmergency = Boolean.TRUE.equals(request.getIsEmergency());
        
        // Ödeme hesaplama
        BigDecimal estimatedCost = request.getEstimatedCost() != null ? 
                request.getEstimatedCost() : BigDecimal.ZERO;
        
        BigDecimal customerPayment = calculateCustomerPayment(
                estimatedCost, deductible, coPaymentPercentage, isEmergency);
        
        BigDecimal estimatedPayment = estimatedCost.subtract(customerPayment);
        
        // Teminat kontrolü
        BigDecimal usedCoverage = getBigDecimal(policyData, "usedCoverage");
        BigDecimal remainingCoverage = coverageLimit.subtract(usedCoverage);
        
        if (estimatedPayment.compareTo(remainingCoverage) > 0) {
            estimatedPayment = remainingCoverage;
            customerPayment = estimatedCost.subtract(estimatedPayment);
        }
        
        // Response oluştur
        return PolicyValidationResponse.builder()
                .isCovered(isCovered)
                .decision(isCovered ? "POLİÇE KAPSAR" : "POLİÇE KAPSAMAZ")
                .explanation(buildExplanation(request, policyType, isCovered, isEmergency))
                .coverageLimit(coverageLimit)
                .usedCoverage(usedCoverage)
                .remainingCoverage(remainingCoverage)
                .estimatedPayment(estimatedPayment)
                .customerPayment(customerPayment)
                .coverageCategory(determineCoverageCategory(request.getProcedureCategory(), isEmergency))
                .deductible(deductible)
                .coPaymentPercentage(coPaymentPercentage)
                .applicableRules(buildApplicableRules(request, policyType))
                .warnings(buildWarnings(request, policyData))
                .recommendations(buildRecommendations(request, isCovered))
                .requiredDocuments(buildRequiredDocuments(request, policyType))
                .confidence(0.7) // Rule-based daha düşük confidence
                .detectedKeywords(extractKeywords(request.getMedicalProcedure()))
                .procedureCategory(request.getProcedureCategory())
                .isEmergency(isEmergency)
                .preApprovalRequired(isPreApprovalRequired(request, policyType))
                .specialConditions(determineSpecialConditions(request, policyData))
                .policyDetails(buildPolicyDetails(policyData))
                .analysisDate(LocalDate.now())
                .analysisSource("RULE_BASED")
                .build();
    }
    
    /**
     * Doküman Önerisi
     */
    public Map<String, Object> recommendDocuments(PolicyValidationRequest request) {
        log.info("Recommending documents for procedure: {}", request.getMedicalProcedure());
        
        try {
            Map<String, Object> policyData = getPolicyData(request.getPolicyId(), request.getPolicyNumber());
            
            if (Objects.isNull(policyData)) {
                return Map.of("error", "Policy not found");
            }
            
            String policyType = (String) policyData.get("policyType");
            boolean isEmergency = Boolean.TRUE.equals(request.getIsEmergency());
            
            // AI veya rule-based doküman önerisi
            List<String> requiredDocs = aiEnabled ? 
                    aiBasedDocumentRecommendation(request, policyType) :
                    ruleBasedDocumentRecommendation(request, policyType);
            
            // Öncelik sıralama
            Map<String, Integer> priorityMap = assignDocumentPriority(requiredDocs, isEmergency);
            
            return Map.of(
                    "requiredDocuments", requiredDocs,
                    "priority", priorityMap,
                    "reason", buildDocumentReason(request, policyType),
                    "deadline", calculateDocumentDeadline(isEmergency),
                    "specialNotes", buildDocumentNotes(request, policyType)
            );
            
        } catch (Exception e) {
            log.error("Error recommending documents", e);
            return Map.of("error", "Document recommendation failed: " + e.getMessage());
        }
    }
    
    /**
     * Hızlı Policy Check
     */
    public Map<String, String> quickCheck(String question, String policyId) {
        log.info("Quick check for policy: {}, question: {}", policyId, question);
        
        try {
            Map<String, Object> policyData = getPolicyData(policyId, null);
            
            if (Objects.isNull(policyData)) {
                return Map.of("error", "Policy not found");
            }
            
            // AI ile quick answer
            if (aiEnabled) {
                String aiAnswer = getAIQuickAnswer(question, policyData);
                return Map.of("answer", aiAnswer, "source", "AI");
            } else {
                // Rule-based quick answer
                String ruleBasedAnswer = getRuleBasedQuickAnswer(question, policyData);
                return Map.of("answer", ruleBasedAnswer, "source", "RULE_BASED");
            }
            
        } catch (Exception e) {
            log.error("Error in quick check", e);
            return Map.of("error", "Quick check failed: " + e.getMessage());
        }
    }
    
    /**
     * Toplu Policy Validasyon
     */
    public List<PolicyValidationResponse> validatePoliciesBatch(List<PolicyValidationRequest> requests) {
        log.info("Batch validation for {} requests", requests.size());
        
        return requests.parallelStream()
                .map(this::validatePolicy)
                .collect(Collectors.toList());
    }
    
    /**
     * Policy Comparison
     */
    public Map<String, Object> comparePolicies(String policyId1, String policyId2) {
        log.info("Comparing policies: {} vs {}", policyId1, policyId2);
        
        try {
            Map<String, Object> policy1 = getPolicyData(policyId1, null);
            Map<String, Object> policy2 = getPolicyData(policyId2, null);
            
            if (Objects.isNull(policy1) || Objects.isNull(policy2)) {
                return Map.of("error", "One or both policies not found");
            }
            
            return Map.of(
                    "policy1", policy1,
                    "policy2", policy2,
                    "differences", comparePolicyFeatures(policy1, policy2),
                    "betterForEmergency", determineBetterForEmergency(policy1, policy2),
                    "betterForChronic", determineBetterForChronic(policy1, policy2),
                    "costComparison", compareCosts(policy1, policy2)
            );
            
        } catch (Exception e) {
            log.error("Error comparing policies", e);
            return Map.of("error", "Policy comparison failed: " + e.getMessage());
        }
    }
    
    // --- Helper Methods ---
    
    private Map<String, Object> getPolicyData(String policyId, String policyNumber) {
        try {
            String url = policyId != null ? 
                    policyServiceUrl + "/api/policies/" + policyId :
                    policyServiceUrl + "/api/policies/number/" + policyNumber;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching policy data", e);
            return null;
        }
    }
    
    private String buildAIPrompt(PolicyValidationRequest request, Map<String, Object> policyData) {
        return String.format("""
            Bir sağlık sigortası poliçe validasyonu yap.
            
            POLİÇE BİLGİLERİ:
            - Poliçe Tipi: %s
            - Poliçe Durumu: %s
            - Teminat Limiti: %s
            - Muafiyet: %s
            - Katılım Payı: %s%%
            
            MEDİKAL İŞLEM:
            - İşlem: %s
            - Kategori: %s
            - Tahmini Maliyet: %s
            - Acil Durum: %s
            - Hastane: %s
            
            Çalışan Sorusu: %s
            
            Lütfen şu bilgileri JSON formatında ver:
            - is_covered: true/false
            - decision: KARAR
            - explanation: DETAYLI AÇIKLAMA
            - coverage_category: TEMİNAT KATEGORİSİ
            - estimated_payment: TAHMİNİ ÖDEME
            - customer_payment: MÜŞTERİ ÖDEMESİ
            - warnings: UYARILAR (array)
            - recommendations: ÖNERİLER (array)
            - required_documents: GEREKLİ DOKÜMANLAR (array)
            - confidence: 0-1 arası güven oranı
            """,
                policyData.get("policyType"),
                policyData.get("policyStatus"),
                policyData.get("coverageLimit"),
                policyData.get("deductible"),
                policyData.get("coPaymentPercentage"),
                request.getMedicalProcedure(),
                request.getProcedureCategory(),
                request.getEstimatedCost(),
                request.getIsEmergency(),
                request.getHospitalName(),
                request.getEmployeeQuestion()
        );
    }
    
    private String callOpenAI(String prompt) {
        // Simüle edilmiş OpenAI API çağrısı
        // Gerçek implementasyonda: OpenAI SDK kullanılır
        log.info("Calling OpenAI API...");
        
        // Mock response
        return """
            {
                "is_covered": true,
                "decision": "POLİÇE KAPSAR",
                "explanation": "Kalp krizi tedavisi teminat dahilindedir. Acil durum olarak değerlendirilmiştir.",
                "coverage_category": "ACIL_DURUM",
                "estimated_payment": 12000.00,
                "customer_payment": 1800.00,
                "warnings": ["Önceki hasarlar yoğun"],
                "recommendations": ["Hasta raporu alınmalı"],
                "required_documents": ["Hasta raporu", "Fatura", "Kimlik"],
                "confidence": 0.9
            }
            """;
    }
    
    private PolicyValidationResponse parseAIResponse(String aiResponse, Map<String, Object> policyData) {
        // AI response'u parse et
        // Gerçek implementasyonda Jackson veya Gson kullanılır
        
        // Mock parse
        return PolicyValidationResponse.builder()
                .isCovered(true)
                .decision("POLİÇE KAPSAR")
                .explanation("AI analizi sonucu: Kalp krizi tedavisi teminat dahilindedir")
                .coverageCategory("ACIL_DURUM")
                .estimatedPayment(new BigDecimal("12000.00"))
                .customerPayment(new BigDecimal("1800.00"))
                .warnings(Arrays.asList("Önceki hasarlar yoğun"))
                .recommendations(Arrays.asList("Hasta raporu alınmalı"))
                .requiredDocuments(Arrays.asList("Hasta raporu", "Fatura", "Kimlik"))
                .confidence(0.9)
                .policyDetails(buildPolicyDetails(policyData))
                .analysisDate(LocalDate.now())
                .analysisSource("AI")
                .build();
    }
    
    private boolean isCoveredByRules(PolicyValidationRequest request, String policyType, String policyStatus) {
        // Basit validasyon kuralları
        if (!"ACTIVE".equals(policyStatus)) {
            return false;
        }
        
        if (request.getIsEmergency()) {
            return true; // Acil durumlar genelde kapsar
        }
        
        if ("ELEMENTARY_HEALTH".equals(policyType)) {
            return Arrays.asList("HASTANE_DOKTOR", "LABORATUVAR", "ILAÇ", "CERRAHI")
                    .contains(request.getProcedureCategory());
        }
        
        return true;
    }
    
    private BigDecimal calculateCustomerPayment(BigDecimal estimatedCost, BigDecimal deductible, 
                                                BigDecimal coPaymentPercentage, boolean isEmergency) {
        if (isEmergency) {
            // Acil durumda muafiyet düşebilir
            deductible = deductible.multiply(BigDecimal.valueOf(0.5));
        }
        
        BigDecimal afterDeductible = estimatedCost.subtract(deductible);
        if (afterDeductible.compareTo(BigDecimal.ZERO) < 0) {
            afterDeductible = BigDecimal.ZERO;
        }
        
        BigDecimal coPayment = afterDeductible.multiply(coPaymentPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return deductible.add(coPayment);
    }
    
    private String buildExplanation(PolicyValidationRequest request, String policyType, 
                                    boolean isCovered, boolean isEmergency) {
        if (!isCovered) {
            return "Bu işlem poliçe kapsamında değildir. Poliçe tipi: " + policyType;
        }
        
        String explanation = String.format("Bu işlem poliçe kapsamındadır. Poliçe tipi: %s", policyType);
        
        if (isEmergency) {
            explanation += ". Acil durum olarak değerlendirilmiştir, öncelikli işlem uygulanacaktır.";
        }
        
        return explanation;
    }
    
    private String determineCoverageCategory(String procedureCategory, boolean isEmergency) {
        if (isEmergency) {
            return "ACIL_DURUM";
        }
        
        return switch (procedureCategory) {
            case "HASTANE_DOKTOR" -> "YATAKLI_TEDAVI";
            case "LABORATUVAR" -> "TANI_VE_TEST";
            case "ILAÇ" -> "ILAÇ_GIDERLERI";
            case "CERRAHI" -> "CERRAHI_ISLEMLER";
            case "CHECKUP" -> "CHECKUP_VE_KORUYUCU";
            default -> "DIGER";
        };
    }
    
    private List<String> buildApplicableRules(PolicyValidationRequest request, String policyType) {
        List<String> rules = new ArrayList<>();
        
        if (request.getIsEmergency()) {
            rules.add("Acil durum kuralı: Öncelikli işlem");
        }
        
        if ("ELEMENTARY_HEALTH".equals(policyType)) {
            rules.add("Elementer sağlık kuralı: Temel teminatlar");
        }
        
        rules.add("Muafiyet uygulanır");
        rules.add("Katılım payı uygulanır");
        
        return rules;
    }
    
    private List<String> buildWarnings(PolicyValidationRequest request, Map<String, Object> policyData) {
        List<String> warnings = new ArrayList<>();
        
        BigDecimal usedCoverage = getBigDecimal(policyData, "usedCoverage");
        BigDecimal coverageLimit = getBigDecimal(policyData, "coverageLimit");
        
        if (usedCoverage.compareTo(coverageLimit.multiply(BigDecimal.valueOf(0.8))) > 0) {
            warnings.add("Teminat limitine yaklaşıyorsunuz");
        }
        
        if (request.getEstimatedCost() != null && 
            request.getEstimatedCost().compareTo(new BigDecimal("50000")) > 0) {
            warnings.add("Yüksek maliyetli işlem, ek onay gerekebilir");
        }
        
        return warnings;
    }
    
    private List<String> buildRecommendations(PolicyValidationRequest request, boolean isCovered) {
        List<String> recommendations = new ArrayList<>();
        
        if (isCovered) {
            recommendations.add("Hasta raporu alınmalı");
            recommendations.add("Fatura detayları kontrol edilmeli");
            
            if (request.getIsEmergency()) {
                recommendations.add("Acil durum belgesi talep edilmeli");
            }
        } else {
            recommendations.add("Alternatif teminatlar incelenmeli");
            recommendations.add("Müşteri bilgilendirilmeli");
        }
        
        return recommendations;
    }
    
    private List<String> buildRequiredDocuments(PolicyValidationRequest request, String policyType) {
        List<String> documents = new ArrayList<>();
        
        documents.add("Kimlik fotokopisi");
        documents.add("Poliçe fotokopisi");
        documents.add("Hasta raporu");
        documents.add("Fatura");
        
        if (request.getIsEmergency()) {
            documents.add("Acil durum belgesi");
        }
        
        if ("LABORATUVAR".equals(request.getProcedureCategory())) {
            documents.add("Laboratuvar sonuçları");
        }
        
        if ("CERRAHI".equals(request.getProcedureCategory())) {
            documents.add("Ameliyat raporu");
            documents.add("Anestezi raporu");
        }
        
        return documents;
    }
    
    private List<String> extractKeywords(String medicalProcedure) {
        // Basit keyword extraction
        List<String> keywords = new ArrayList<>();
        
        if (medicalProcedure != null) {
            String[] words = medicalProcedure.toLowerCase().split("\\s+");
            keywords.addAll(Arrays.asList(words));
        }
        
        return keywords;
    }
    
    private boolean isPreApprovalRequired(PolicyValidationRequest request, String policyType) {
        // Yüksek maliyetli işlemler için onay gerekli
        if (request.getEstimatedCost() != null && 
            request.getEstimatedCost().compareTo(new BigDecimal("100000")) > 0) {
            return true;
        }
        
        return "PREMIUM".equals(policyType);
    }
    
    private List<String> determineSpecialConditions(PolicyValidationRequest request, 
                                                       Map<String, Object> policyData) {
        List<String> conditions = new ArrayList<>();
        
        if (request.getIsEmergency()) {
            conditions.add("Acil durum önceliği");
        }
        
        if ("YURT_DISI".equals(request.getHospitalName())) {
            conditions.add("Yurt dışı tedavi");
        }
        
        return conditions;
    }
    
    private PolicyValidationResponse.PolicyDetails buildPolicyDetails(Map<String, Object> policyData) {
        return PolicyValidationResponse.PolicyDetails.builder()
                .policyNumber((String) policyData.get("policyNumber"))
                .policyType((String) policyData.get("policyType"))
                .policyStatus((String) policyData.get("policyStatus"))
                .startDate((LocalDate) policyData.get("startDate"))
                .endDate((LocalDate) policyData.get("endDate"))
                .annualPremium(getBigDecimal(policyData, "annualPremium"))
                .insuredPersonName((String) policyData.get("insuredPersonName"))
                .tcKimlikNo((String) policyData.get("tcKimlikNo"))
                .build();
    }
    
    private PolicyValidationResponse createNotFoundResponse() {
        return PolicyValidationResponse.builder()
                .isCovered(false)
                .decision("POLİÇE BULUNAMADI")
                .explanation("Belirtilen poliçe bulunamadı")
                .confidence(0.0)
                .analysisSource("ERROR")
                .build();
    }
    
    private PolicyValidationResponse createErrorResponse(String errorMessage) {
        return PolicyValidationResponse.builder()
                .isCovered(false)
                .decision("HATA")
                .explanation("Validasyon hatası: " + errorMessage)
                .confidence(0.0)
                .analysisSource("ERROR")
                .build();
    }
    
    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .map(value -> {
                    if (value instanceof BigDecimal) {
                        return (BigDecimal) value;
                    } else if (value instanceof Number) {
                        return BigDecimal.valueOf(((Number) value).doubleValue());
                    }
                    return BigDecimal.ZERO;
                })
                .orElse(BigDecimal.ZERO);
    }
    
    // --- Document Recommendation Methods ---
    
    private List<String> aiBasedDocumentRecommendation(PolicyValidationRequest request, String policyType) {
        // AI ile doküman önerisi
        String prompt = String.format("""
            Bu durum için hangi dokümanlar gerekli?
            - İşlem: %s
            - Kategori: %s
            - Acil durum: %s
            - Poliçe tipi: %s
            
            JSON formatında document listesi ver.
            """, request.getMedicalProcedure(), request.getProcedureCategory(), 
                request.getIsEmergency(), policyType);
        
        // AI çağrısı (mock)
        return Arrays.asList("Hasta raporu", "Fatura", "Kimlik", "Acil durum belgesi");
    }
    
    private List<String> ruleBasedDocumentRecommendation(PolicyValidationRequest request, String policyType) {
        List<String> docs = new ArrayList<>();
        
        docs.add("Kimlik fotokopisi");
        docs.add("Poliçe fotokopisi");
        docs.add("Hasta raporu");
        docs.add("Fatura");
        
        if (request.getIsEmergency()) {
            docs.add("Acil durum belgesi");
        }
        
        return docs;
    }
    
    private Map<String, Integer> assignDocumentPriority(List<String> documents, boolean isEmergency) {
        Map<String, Integer> priority = new HashMap<>();
        
        int basePriority = isEmergency ? 1 : 3;
        
        for (String doc : documents) {
            if (doc.contains("Kimlik") || doc.contains("Poliçe")) {
                priority.put(doc, basePriority);
            } else if (doc.contains("Acil")) {
                priority.put(doc, basePriority);
            } else {
                priority.put(doc, basePriority + 1);
            }
        }
        
        return priority;
    }
    
    private String buildDocumentReason(PolicyValidationRequest request, String policyType) {
        return String.format("%s kategorisindeki %s işlemi için standart dokümanlar gerekli.",
                request.getProcedureCategory(), request.getMedicalProcedure());
    }
    
    private String calculateDocumentDeadline(boolean isEmergency) {
        return isEmergency ? "48 saat" : "5 iş günü";
    }
    
    private List<String> buildDocumentNotes(PolicyValidationRequest request, String policyType) {
        List<String> notes = new ArrayList<>();

        boolean isEmergency = request.getIsEmergency() != null && request.getIsEmergency();
        notes.add("Acil durum belgesi en geç 48 saat içinde sunulmalı");

        return notes;
    }
    
    // --- Quick Check Methods ---
    
    private String getAIQuickAnswer(String question, Map<String, Object> policyData) {
        // AI ile quick answer
        String prompt = String.format("""
            Soru: %s
            Poliçe tipi: %s
            Teminat limiti: %s
            
            Kısa ve net yanıt ver.
            """, question, policyData.get("policyType"), policyData.get("coverageLimit"));
        
        // AI çağrısı (mock)
        return "Evet, bu poliçe check-up teminatını kapsar. Yıllık 1 check-up hakkınız var.";
    }
    
    private String getRuleBasedQuickAnswer(String question, Map<String, Object> policyData) {
        String policyType = (String) policyData.get("policyType");
        
        if (question.toLowerCase().contains("checkup")) {
            if ("ELEMENTARY_HEALTH".equals(policyType)) {
                return "Evet, yıllık 1 check-up hakkınız var.";
            } else {
                return "Poliçe tipine göre check-up teminatı değişir.";
            }
        }
        
        if (question.toLowerCase().contains("acil")) {
            return "Acil durumlar poliçe kapsamındadır.";
        }
        
        return "Sorunuza yanıt vermek için daha fazla bilgi gerekli.";
    }
    
    // --- Policy Comparison Methods ---
    
    private Map<String, Object> comparePolicyFeatures(Map<String, Object> policy1, Map<String, Object> policy2) {
        return Map.of(
                "coverageLimit_diff", 
                        getBigDecimal(policy1, "coverageLimit").subtract(getBigDecimal(policy2, "coverageLimit")),
                "deductible_diff",
                        getBigDecimal(policy1, "deductible").subtract(getBigDecimal(policy2, "deductible")),
                "coPayment_diff",
                        getBigDecimal(policy1, "coPaymentPercentage").subtract(getBigDecimal(policy2, "coPaymentPercentage"))
        );
    }
    
    private String determineBetterForEmergency(Map<String, Object> policy1, Map<String, Object> policy2) {
        BigDecimal deductible1 = getBigDecimal(policy1, "deductible");
        BigDecimal deductible2 = getBigDecimal(policy2, "deductible");
        
        return deductible1.compareTo(deductible2) < 0 ? "Policy 1" : "Policy 2";
    }
    
    private String determineBetterForChronic(Map<String, Object> policy1, Map<String, Object> policy2) {
        BigDecimal coPayment1 = getBigDecimal(policy1, "coPaymentPercentage");
        BigDecimal coPayment2 = getBigDecimal(policy2, "coPaymentPercentage");
        
        return coPayment1.compareTo(coPayment2) < 0 ? "Policy 1" : "Policy 2";
    }
    
    private Map<String, Object> compareCosts(Map<String, Object> policy1, Map<String, Object> policy2) {
        return Map.of(
                "annualPremium_diff",
                        getBigDecimal(policy1, "annualPremium").subtract(getBigDecimal(policy2, "annualPremium")),
                "cost_per_coverage_1",
                        getBigDecimal(policy1, "annualPremium").divide(getBigDecimal(policy1, "coverageLimit"), 2, RoundingMode.HALF_UP),
                "cost_per_coverage_2",
                        getBigDecimal(policy2, "annualPremium").divide(getBigDecimal(policy2, "coverageLimit"), 2, RoundingMode.HALF_UP)
        );
    }
    
    /**
     * Dashboard Insight Event'i Kafka'ya gönderir
     * 
     * Analytics için Kafka event'i yayınlar:
     * - dashboard.insight topic'ine gönderir
     * - Analytics service bunu dinler
     * - Kullanıcı davranışlarını takip eder
     * 
     * @param actionType Action tipi (VALIDATION, RECOMMENDATION, QUICK_CHECK)
     * @param request Policy validation request
     * @param result Sonuç (COVERED, NOT_COVERED, PARTIALLY_COVERED)
     * @param analysisSource Analiz kaynağı (AI, RULE_BASED)
     */
    private void publishDashboardInsightEvent(String actionType, PolicyValidationRequest request, String result, String analysisSource) {
        try {
            DashboardInsightEvent event = DashboardInsightEvent.builder()
                    .eventId(UUID.randomUUID())
                    .userId("employee-" + UUID.randomUUID()) // JWT'den alınmalı
                    .actionType(actionType)
                    .policyId(request.getPolicyId())
                    .policyNumber(request.getPolicyNumber())
                    .result(result)
                    .analysisSource(analysisSource)
                    .timestamp(LocalDateTime.now())
                    .correlationId(UUID.randomUUID().toString())
                    .build();
            
            kafkaTemplate.send("dashboard.insight", event.getEventId().toString(), event);
            log.info("Dashboard insight event published to Kafka for action: {}", actionType);
        } catch (Exception e) {
            log.error("Error publishing dashboard insight event", e);
        }
    }
}