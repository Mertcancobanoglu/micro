package com.sigorta.bff_service.service;

import com.sigorta.bff_service.dto.*;
import com.sigorta.bff_service.events.ChatbotMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.Objects;

/**
 * Chatbot Service
 * 
 * Sigorta çalışanları için AI-powered chatbot.
 * 
 * Özellikler:
 * - Intent classification (kullanıcı niyetini anlama)
 * - Conversation state management (konuşma durum takibi)
 * - Multi-step conversations (adım adım bilgi toplama)
 * - Integration with EmployeeDashboardService
 * - Natural language understanding
 * 
 * @author Insurance Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {
    
    private final EmployeeDashboardService employeeDashboardService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${ai.enabled:false}")
    private boolean aiEnabled;
    
    // Conversation storage (production'da Redis kullanılmalı)
    private final Map<String, ConversationContext> activeConversations = new ConcurrentHashMap<>();
    
    // Intent patterns
    private static final Map<String, List<Pattern>> INTENT_PATTERNS = new HashMap<>();
    
    static {
        // Policy validation patterns
        INTENT_PATTERNS.put("POLICY_VALIDATION", Arrays.asList(
                Pattern.compile("(?i).*poliçe.*kapsar.*"),
                Pattern.compile("(?i).*teminat.*dahil.*"),
                Pattern.compile("(?i).*işlem.*ödeme.*"),
                Pattern.compile("(?i).*müşteri.*hastane.*")
        ));
        
        // Document recommendation patterns
        INTENT_PATTERNS.put("DOCUMENT_RECOMMENDATION", Arrays.asList(
                Pattern.compile("(?i).*doküman.*kontrol.*"),
                Pattern.compile("(?i).*dosya.*gerekli.*"),
                Pattern.compile("(?i).*belge.*istedi.*"),
                Pattern.compile("(?i).*evrak.*ne.*")
        ));
        
        // Quick check patterns
        INTENT_PATTERNS.put("QUICK_CHECK", Arrays.asList(
                Pattern.compile("(?i).*check-up.*var.*"),
                Pattern.compile("(?i).*diş.*teminat.*"),
                Pattern.compile("(?i).*gözlük.*teminat.*"),
                Pattern.compile("(?i).*emzirme.*teminat.*")
        ));
        
        // Policy comparison patterns
        INTENT_PATTERNS.put("POLICY_COMPARISON", Arrays.asList(
                Pattern.compile("(?i).*poliçe.*karşılaştır.*"),
                Pattern.compile("(?i).*hangisi.*daha.*iyi.*"),
                Pattern.compile("(?i).*fark.*nedir.*")
        ));
        
        // Greeting patterns
        INTENT_PATTERNS.put("GREETING", Arrays.asList(
                Pattern.compile("(?i).*merhaba.*"),
                Pattern.compile("(?i).*selam.*"),
                Pattern.compile("(?i).*günaydın.*"),
                Pattern.compile("(?i).*iyi günler.*")
        ));
        
        // Help patterns
        INTENT_PATTERNS.put("HELP", Arrays.asList(
                Pattern.compile("(?i).*yardım.*"),
                Pattern.compile("(?i).*nasıl.*kullanılır.*"),
                Pattern.compile("(?i).*ne.*yapabilirim.*")
        ));
    }
    
    /**
     * Chatbot mesajını işle
     */
    public ChatResponse processMessage(ChatRequest request) {
        log.info("Processing chat message: {}", request.getMessage());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Konuşma bağlamını al veya oluştur
            ConversationContext context = getOrCreateContext(request);
            
            // 2. Intent classification
            IntentResult intentResult = classifyIntent(request.getMessage());
            
            // 3. Context güncelle
            context.updateIntent(intentResult.getIntent(), intentResult.getConfidence());
            
            // 4. Intent'e göre routing
            ChatResponse response = routeIntent(request, context, intentResult);
            
            // 5. Response build
            response.setConversationId(request.getConversationId());
            response.setMessageId(UUID.randomUUID().toString());
            response.setIntent(intentResult.getIntent());
            response.setConfidence(intentResult.getConfidence());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setTimestamp(System.currentTimeMillis());
            
            // 6. Konuşma geçmişine ekle
            addToConversationHistory(context, request, response);
            
            // 7. Kafka event gönder - analytics için
            publishChatbotMessageEvent(request, response, intentResult.getIntent());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return createErrorResponse(request, e.getMessage());
        }
    }
    
    /**
     * Intent classification
     */
    private IntentResult classifyIntent(String message) {
        if (aiEnabled) {
            return aiBasedIntentClassification(message);
        } else {
            return ruleBasedIntentClassification(message);
        }
    }
    
    /**
     * AI-based intent classification
     */
    private IntentResult aiBasedIntentClassification(String message) {
        // OpenAI ile intent classification
        // Gerçek implementasyonda: OpenAI API çağrısı
        
        String prompt = String.format("""
            Aşağıdaki mesajın intent'ini belirle.
            Mesaj: %s
            
            Olası intent'ler:
            - POLICY_VALIDATION: Poliçe kapsamı sorgulama
            - DOCUMENT_RECOMMENDATION: Doküman önerisi
            - QUICK_CHECK: Hızlı soru
            - POLICY_COMPARISON: Poliçe karşılaştırma
            - GREETING: Selamlaşma
            - HELP: Yardım talebi
            
            JSON formatında:
            {
                "intent": "INTENT_NAME",
                "confidence": 0.0-1.0
            }
            """, message);
        
        // AI çağrısı (mock)
        IntentResult result = new IntentResult();
        result.setIntent("POLICY_VALIDATION");
        result.setConfidence(0.9);
        return result;
    }
    
    /**
     * Rule-based intent classification
     */
    private IntentResult ruleBasedIntentClassification(String message) {
        String normalizedMessage = message.toLowerCase().trim();
        
        // Her intent için pattern kontrolü
        for (Map.Entry<String, List<Pattern>> entry : INTENT_PATTERNS.entrySet()) {
            String intent = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                if (pattern.matcher(normalizedMessage).find()) {
                    IntentResult result = new IntentResult();
                    result.setIntent(intent);
                    result.setConfidence(0.8); // Rule-based daha düşük confidence
                    return result;
                }
            }
        }
        
        // Default intent
        IntentResult result = new IntentResult();
        result.setIntent("UNKNOWN");
        result.setConfidence(0.5);
        return result;
    }
    
    /**
     * Intent routing
     */
    private ChatResponse routeIntent(ChatRequest request, ConversationContext context, IntentResult intentResult) {
        switch (intentResult.getIntent()) {
            case "POLICY_VALIDATION":
                return handlePolicyValidation(request, context);
            case "DOCUMENT_RECOMMENDATION":
                return handleDocumentRecommendation(request, context);
            case "QUICK_CHECK":
                return handleQuickCheck(request, context);
            case "POLICY_COMPARISON":
                return handlePolicyComparison(request, context);
            case "GREETING":
                return handleGreeting(request, context);
            case "HELP":
                return handleHelp(request, context);
            default:
                return handleUnknown(request, context);
        }
    }
    
    /**
     * Policy validation handler
     */
    private ChatResponse handlePolicyValidation(ChatRequest request, ConversationContext context) {
        log.info("Handling policy validation intent");
        
        // Context'ten bilgileri al
        String policyId = (String) context.getCollectedInfo().get("policyId");
        String tcKimlikNo = (String) context.getCollectedInfo().get("tcKimlikNo");
        String medicalProcedure = extractMedicalProcedure(request.getMessage());
        
        // Eksik bilgileri kontrol et
        if (Objects.isNull(policyId) && Objects.isNull(tcKimlikNo)) {
            return askForPolicyId(request, context);
        }
        
        if (Objects.isNull(medicalProcedure)) {
            return askForMedicalProcedure(request, context);
        }
        
        // Validasyon isteği oluştur
        PolicyValidationRequest validationRequest = PolicyValidationRequest.builder()
                .policyId(policyId)
                .tcKimlikNo(tcKimlikNo)
                .medicalProcedure(medicalProcedure)
                .employeeQuestion(request.getMessage())
                .build();
        
        // Validasyon yap
        PolicyValidationResponse validationResponse = 
                employeeDashboardService.validatePolicy(validationRequest);
        
        // Chatbot response oluştur
        String botMessage = buildPolicyValidationMessage(validationResponse);
        
        return ChatResponse.builder()
                .message(botMessage)
                .responseType("CARD")
                .status("SUCCESS")
                .data(Map.of(
                        "validationResult", validationResponse,
                        "context", context.getCollectedInfo()
                ))
                .suggestedActions(buildPolicyValidationActions(validationResponse))
                .followUpQuestions(Arrays.asList(
                        "Gerekli dokümanlar neler?",
                        "Ödeme detayları",
                        "Başka bir işlem kontrol et"
                ))
                .build();
    }
    
    /**
     * Document recommendation handler
     */
    private ChatResponse handleDocumentRecommendation(ChatRequest request, ConversationContext context) {
        log.info("Handling document recommendation intent");
        
        String policyId = (String) context.getCollectedInfo().get("policyId");
        String medicalProcedure = extractMedicalProcedure(request.getMessage());
        
        if (Objects.isNull(policyId)) {
            return askForPolicyId(request, context);
        }
        
        if (Objects.isNull(medicalProcedure)) {
            return askForMedicalProcedure(request, context);
        }
        
        PolicyValidationRequest docRequest = PolicyValidationRequest.builder()
                .policyId(policyId)
                .medicalProcedure(medicalProcedure)
                .build();
        
        Map<String, Object> docResponse = 
                employeeDashboardService.recommendDocuments(docRequest);
        
        String botMessage = buildDocumentRecommendationMessage(docResponse);
        
        return ChatResponse.builder()
                .message(botMessage)
                .responseType("CARD")
                .status("SUCCESS")
                .data(docResponse)
                .suggestedActions(buildDocumentRecommendationActions(docResponse))
                .followUpQuestions(Arrays.asList(
                        "Doküman nerede?",
                        "Acil durum belgesi nasıl alınır?",
                        "Diğer bilgiler"
                ))
                .build();
    }
    
    /**
     * Quick check handler
     */
    private ChatResponse handleQuickCheck(ChatRequest request, ConversationContext context) {
        log.info("Handling quick check intent");

        String policyId = (String) context.getCollectedInfo().get("policyId");
        String question = request.getMessage();

        if (Objects.isNull(policyId)) {
            return askForPolicyId(request, context);
        }

        // ✅ Değişken tipi Map<String, Object> olarak güncellendi:
        Map<String, String> quickCheckResponse =
                employeeDashboardService.quickCheck(question, policyId);

        String botMessage = String.format("🤖 %s", quickCheckResponse.get("answer"));

// 2. Map<String, Object> tipine cast/dönüştürme yap
        Map<String, Object> dataMap = new HashMap<>(quickCheckResponse);

        return ChatResponse.builder()
                .message(botMessage)
                .responseType("TEXT")
                .status("SUCCESS")
                .data(dataMap) // dataMap (Map<String, Object>) geçiliyor
                .followUpQuestions(Arrays.asList(
                        "Diğer teminatlar",
                        "Policy detayları",
                        "Başka bir soru"
                ))
                .build();
    }
    
    /**
     * Policy comparison handler
     */
    private ChatResponse handlePolicyComparison(ChatRequest request, ConversationContext context) {
        log.info("Handling policy comparison intent");
        
        String policyId1 = (String) context.getCollectedInfo().get("policyId1");
        String policyId2 = (String) context.getCollectedInfo().get("policyId2");
        
        if (Objects.isNull(policyId1)) {
            return askForPolicyId1(request, context);
        }
        
        if (Objects.isNull(policyId2)) {
            return askForPolicyId2(request, context);
        }
        
        Map<String, Object> comparisonResponse = 
                employeeDashboardService.comparePolicies(policyId1, policyId2);
        
        String botMessage = buildPolicyComparisonMessage(comparisonResponse);
        
        return ChatResponse.builder()
                .message(botMessage)
                .responseType("CARD")
                .status("SUCCESS")
                .data(comparisonResponse)
                .followUpQuestions(Arrays.asList(
                        "Detaylı karşılaştırma",
                        "Cost analysis",
                        "Recommendation"
                ))
                .build();
    }
    
    /**
     * Greeting handler
     */
    private ChatResponse handleGreeting(ChatRequest request, ConversationContext context) {
        log.info("Handling greeting intent");
        
        String[] greetings = {
                "Merhaba! Size nasıl yardımcı olabilirim?",
                "Selam! Poliçe validasyonu, doküman önerisi veya hızlı sorular için buradayım.",
                "Günaydın! Bugün size nasıl yardımcı olabilirim?"
        };
        
        Random random = new Random();
        String botMessage = greetings[random.nextInt(greetings.length)];
        
        return ChatResponse.builder()
                .message(botMessage)
                .responseType("TEXT")
                .status("SUCCESS")
                .suggestedActions(Arrays.asList(
                        ChatResponse.SuggestedAction.builder()
                                .type("BUTTON")
                                .label("Poliçe Validasyon")
                                .action("POLICY_VALIDATION")
                                .build(),
                        ChatResponse.SuggestedAction.builder()
                                .type("BUTTON")
                                .label("Doküman Önerisi")
                                .action("DOCUMENT_RECOMMENDATION")
                                .build(),
                        ChatResponse.SuggestedAction.builder()
                                .type("BUTTON")
                                .label("Hızlı Soru")
                                .action("QUICK_CHECK")
                                .build()
                ))
                .followUpQuestions(Arrays.asList(
                        "Müşteri poliçe kapsar mı?",
                        "Hangi dokümanlar gerekli?",
                        "Check-up teminatı var mı?"
                ))
                .build();
    }
    
    /**
     * Help handler
     */
    private ChatResponse handleHelp(ChatRequest request, ConversationContext context) {
        log.info("Handling help intent");
        
        String helpMessage = """
            🤖 **Sigorta Chatbot Yardım**
            
            Size şu konularda yardımcı olabilirim:
            
            **📋 Poliçe Validasyon**
            - "Müşteri kalp krizi geçirdi, poliçe kapsar mı?"
            - "Bu işlem teminat dahilinde mi?"
            
            **📄 Doküman Önerisi**
            - "Hangi dokümanları kontrol etmeliyim?"
            - "Bu durum için gerekli evraklar neler?"
            
            **⚡ Hızlı Sorular**
            - "Polisemde check-up var mı?"
            - "Diş teminatı kapsar mı?"
            
            **🔄 Policy Comparison**
            - "İki poliçe arasındaki farklar neler?"
            - "Hangi poliçe daha iyi?"
            
            Başlamak için bir soru sorun veya yukarıdaki butonları kullanın!
            """;
        
        return ChatResponse.builder()
                .message(helpMessage)
                .responseType("TEXT")
                .status("SUCCESS")
                .suggestedActions(Arrays.asList(
                        ChatResponse.SuggestedAction.builder()
                                .type("BUTTON")
                                .label("Poliçe Validasyon")
                                .action("POLICY_VALIDATION")
                                .build(),
                        ChatResponse.SuggestedAction.builder()
                                .type("BUTTON")
                                .label("Doküman Önerisi")
                                .action("DOCUMENT_RECOMMENDATION")
                                .build()
                ))
                .build();
    }
    
    /**
     * Unknown intent handler
     */
    private ChatResponse handleUnknown(ChatRequest request, ConversationContext context) {
        log.info("Handling unknown intent");
        
        String botMessage = """
            👋 Anlayamadım, lütfen tekrar açıklayın.
            
            Size şu konularda yardımcı olabilirim:
            - Poliçe validasyon
            - Doküman önerisi
            - Hızlı sorular
            - Policy comparison
            
            Yardım için "yardım" yazabilirsiniz.
            """;
        
        return ChatResponse.builder()
                .message(botMessage)
                .responseType("TEXT")
                .status("REQUIRE_MORE_INFO")
                .intent("UNKNOWN")
                .confidence(0.5)
                .followUpQuestions(Arrays.asList(
                        "Yardım",
                        "Poliçe validasyon",
                        "Doküman önerisi"
                ))
                .build();
    }
    
    // --- Helper Methods ---
    
    /**
     * Konuşma bağlamını al veya oluştur
     */
    private ConversationContext getOrCreateContext(ChatRequest request) {
        // 1. conversationId yoksa yeni bir String UUID üret
        String conversationId = Optional.ofNullable(request.getConversationId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        // 2. Map'te varsa getir, yoksa atomically oluştur ve Map'e koy
        ConversationContext context = activeConversations.computeIfAbsent(conversationId, id ->
                ConversationContext.startConversation(id, request.getUserId())
        );

        // 3. Son aktivite zamanını güncelle
        context.setLastActivity(LocalDateTime.now());
        return context;
    }
    
    /**
     * Medical procedure extraction
     */
    private String extractMedicalProcedure(String message) {
        // Basit extraction - production'da AI kullanılmalı
        if (message.toLowerCase().contains("kalp krizi")) {
            return "Kalp krizi tedavisi";
        } else if (message.toLowerCase().contains("check-up")) {
            return "Check-up";
        } else if (message.toLowerCase().contains("diş")) {
            return "Diş tedavisi";
        } else if (message.toLowerCase().contains("kan tahlili")) {
            return "Kan tahlili";
        }
        
        // Full message kullan
        return message;
    }
    
    /**
     * Policy ID sor
     */
    private ChatResponse askForPolicyId(ChatRequest request, ConversationContext context) {
        context.updateStep("AWAITING_POLICY_ID");
        
        return ChatResponse.builder()
                .message("Poliçe ID veya poliçe numarası verir misiniz?")
                .responseType("INPUT")
                .status("REQUIRE_MORE_INFO")
                .suggestedActions(Arrays.asList(
                        ChatResponse.SuggestedAction.builder()
                                .type("INPUT")
                                .label("Poliçe ID")
                                .action("SET_POLICY_ID")
                                .parameters(Map.of("placeholder", "POL-001 veya UUID"))
                                .build()
                ))
                .build();
    }
    
    /**
     * Medical procedure sor
     */
    private ChatResponse askForMedicalProcedure(ChatRequest request, ConversationContext context) {
        context.updateStep("AWAITING_MEDICAL_PROCEDURE");
        
        return ChatResponse.builder()
                .message("Yapılan tıbbi işlem nedir?")
                .responseType("INPUT")
                .status("REQUIRE_MORE_INFO")
                .suggestedActions(Arrays.asList(
                        ChatResponse.SuggestedAction.builder()
                                .type("INPUT")
                                .label("Tıbbi İşlem")
                                .action("SET_MEDICAL_PROCEDURE")
                                .parameters(Map.of("placeholder", "Örn: Kalp krizi tedavisi"))
                                .build()
                ))
                .build();
    }
    
    /**
     * Policy ID 1 sor
     */
    private ChatResponse askForPolicyId1(ChatRequest request, ConversationContext context) {
        context.updateStep("AWAITING_POLICY_ID_1");
        
        return ChatResponse.builder()
                .message("İlk poliçe ID'si nedir?")
                .responseType("INPUT")
                .status("REQUIRE_MORE_INFO")
                .suggestedActions(Arrays.asList(
                        ChatResponse.SuggestedAction.builder()
                                .type("INPUT")
                                .label("Poliçe ID 1")
                                .action("SET_POLICY_ID_1")
                                .parameters(Map.of("placeholder", "POL-001"))
                                .build()
                ))
                .build();
    }
    
    /**
     * Policy ID 2 sor
     */
    private ChatResponse askForPolicyId2(ChatRequest request, ConversationContext context) {
        context.updateStep("AWAITING_POLICY_ID_2");
        
        return ChatResponse.builder()
                .message("İkinci poliçe ID'si nedir?")
                .responseType("INPUT")
                .status("REQUIRE_MORE_INFO")
                .suggestedActions(Arrays.asList(
                        ChatResponse.SuggestedAction.builder()
                                .type("INPUT")
                                .label("Poliçe ID 2")
                                .action("SET_POLICY_ID_2")
                                .parameters(Map.of("placeholder", "POL-002"))
                                .build()
                ))
                .build();
    }
    
    /**
     * Policy validation message build
     */
    private String buildPolicyValidationMessage(PolicyValidationResponse response) {
        StringBuilder message = new StringBuilder();
        
        if (Boolean.TRUE.equals(response.getIsCovered())) {
            message.append("✅ **POLİÇE KAPSAR**\n\n");
            message.append(String.format("📊 **Ödeme Detayları:**\n"));
            message.append(String.format("- Tahmini Ödeme: %s TL\n", response.getEstimatedPayment()));
            message.append(String.format("- Müşteri Ödemesi: %s TL\n", response.getCustomerPayment()));
            message.append(String.format("- Teminat Kategorisi: %s\n", response.getCoverageCategory()));
            
            if (!response.getWarnings().isEmpty()) {
                message.append("\n⚠️ **Uyarılar:**\n");
                response.getWarnings().forEach(w -> message.append(String.format("- %s\n", w)));
            }
            
            if (!response.getRecommendations().isEmpty()) {
                message.append("\n💡 **Öneriler:**\n");
                response.getRecommendations().forEach(r -> message.append(String.format("- %s\n", r)));
            }
        } else {
            message.append("❌ **POLİÇE KAPSAMAZ**\n\n");
            message.append(response.getExplanation());
        }
        
        return message.toString();
    }
    
    /**
     * Document recommendation message build
     */
    private String buildDocumentRecommendationMessage(Map<String, Object> docResponse) {
        StringBuilder message = new StringBuilder();
        
        message.append("📄 **Gerekli Dokümanlar:**\n\n");
        
        @SuppressWarnings("unchecked")
        List<String> documents = (List<String>) docResponse.get("requiredDocuments");
        
        if (documents != null) {
            for (int i = 0; i < documents.size(); i++) {
                message.append(String.format("%d. %s\n", i + 1, documents.get(i)));
            }
        }
        
        String deadline = (String) docResponse.get("deadline");
        if (deadline != null) {
            message.append(String.format("\n⏰ **Deadline:** %s\n", deadline));
        }
        
        return message.toString();
    }
    
    /**
     * Policy comparison message build
     */
    private String buildPolicyComparisonMessage(Map<String, Object> comparisonResponse) {
        StringBuilder message = new StringBuilder();
        
        message.append("🔄 **Poliçe Karşılaştırma Sonucu:**\n\n");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> differences = (Map<String, Object>) comparisonResponse.get("differences");
        
        if (differences != null) {
            message.append("**Farklar:**\n");
            differences.forEach((key, value) -> 
                    message.append(String.format("- %s: %s\n", key, value)));
        }
        
        String betterForEmergency = (String) comparisonResponse.get("betterForEmergency");
        if (betterForEmergency != null) {
            message.append(String.format("\n🚨 **Acil Durum İçin Daha İyi:** %s\n", betterForEmergency));
        }
        
        return message.toString();
    }
    
    /**
     * Policy validation actions build
     */
    private List<ChatResponse.SuggestedAction> buildPolicyValidationActions(PolicyValidationResponse response) {
        List<ChatResponse.SuggestedAction> actions = new ArrayList<>();
        
        actions.add(ChatResponse.SuggestedAction.builder()
                .type("BUTTON")
                .label("Doküman Önerisi")
                .action("DOCUMENT_RECOMMENDATION")
                .icon("📄")
                .build());
        
        actions.add(ChatResponse.SuggestedAction.builder()
                .type("BUTTON")
                .label("Detaylı Bilgi")
                .action("SHOW_DETAILS")
                .icon("ℹ️")
                .build());
        
        return actions;
    }
    
    /**
     * Document recommendation actions build
     */
    private List<ChatResponse.SuggestedAction> buildDocumentRecommendationActions(Map<String, Object> docResponse) {
        List<ChatResponse.SuggestedAction> actions = new ArrayList<>();
        
        actions.add(ChatResponse.SuggestedAction.builder()
                .type("BUTTON")
                .label("Poliçe Validasyon")
                .action("POLICY_VALIDATION")
                .icon("📋")
                .build());
        
        actions.add(ChatResponse.SuggestedAction.builder()
                .type("BUTTON")
                .label("Doküman Yükle")
                .action("UPLOAD_DOCUMENT")
                .icon("📤")
                .build());
        
        return actions;
    }
    
    /**
     * Konuşma geçmişine ekle
     */
    private void addToConversationHistory(ConversationContext context, 
                                         ChatRequest request, ChatResponse response) {
        ConversationContext.ChatMessage userMessage = ConversationContext.ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .sender("USER")
                .message(request.getMessage())
                .intent(response.getIntent())
                .confidence(response.getConfidence())
                .timestamp(LocalDateTime.now())
                .context(request.getContext())
                .build();
        
        ConversationContext.ChatMessage botMessage = ConversationContext.ChatMessage.builder()
                .messageId(response.getMessageId())
                .sender("BOT")
                .message(response.getMessage())
                .intent(response.getIntent())
                .confidence(response.getConfidence())
                .timestamp(LocalDateTime.now())
                .context(response.getContext())
                .build();
        
        if (Objects.isNull(context.getMessageHistory())) {
            context.setMessageHistory(new ArrayList<>());
        }
        
        context.getMessageHistory().add(userMessage);
        context.getMessageHistory().add(botMessage);
    }
    
    /**
     * Error response oluştur
     */
    private ChatResponse createErrorResponse(ChatRequest request, String errorMessage) {
        return ChatResponse.builder()
                .message("❌ Bir hata oluştu: " + errorMessage)
                .responseType("ERROR")
                .status("ERROR")
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Chatbot Message Event'i Kafka'ya gönderir
     * 
     * Analytics için Kafka event'i yayınlar:
     * - chatbot.message topic'ine gönderir
     * - Analytics service bunu dinler
     * - Kullanıcı davranışlarını takip eder
     * 
     * @param request Chat request
     * @param response Chat response
     * @param intent Classified intent
     */
    private void publishChatbotMessageEvent(ChatRequest request, ChatResponse response, String intent) {
        try {
            ChatbotMessageEvent event = ChatbotMessageEvent.builder()
                    .conversationId(UUID.fromString(request.getConversationId()))
                    .userId(request.getUserId())
                    .message(request.getMessage())
                    .intent(intent)
                    .response(response.getMessage())
                    .timestamp(LocalDateTime.now())
                    .correlationId(request.getConversationId())
                    .metadata(Map.of(
                            "responseType", response.getResponseType(),
                            "status", response.getStatus()
                    ))
                    .build();

            kafkaTemplate.send("chatbot.message", request.getConversationId(), event);
            log.info("Chatbot message event published to Kafka for conversation: {}", request.getConversationId());
        } catch (Exception e) {
            log.error("Error publishing chatbot message event", e);
        }
    }
    
    /**
     * Intent Result Inner Class
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class IntentResult {
        private String intent;
        private Double confidence;
    }
}