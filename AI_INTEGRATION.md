# 🤖 AI/LLM Entegrasyonu - Sağlık Sigortası Sistemi

## 🎯 AI Kullanım Alanları

### 1. **Hasar Açıklama Analizi**
AI ile hasar açıklamasını analiz etme:
- Severity detection (LOW, MEDIUM, HIGH)
- Priority scoring (1-10)
- Category classification (HASTANE_DOKTOR, LABORATUVAR, ILAÇ, DIGER)
- Keyword extraction
- Urgency classification (NORMAL, ACIL, ACIL_YARDIM)

### 2. **Otomatik Tıbbi Kodlama**
ICD-10 kodu tahmini:
- Tanı açıklamasından ICD-10 kodu tahmin etme
- Standart tıbbi kodlama
- Otomatik faturalandırma desteği

### 3. **Fraud Detection (Dolandırıcılık Tespiti)**
Şüpheli hasar tespiti:
- Risk level analysis (LOW, MEDIUM, HIGH, CRITICAL)
- Risk scoring (0-100)
- Risk factor identification
- Recommendation generation

### 4. **Otomatik Ödeme Hesaplama**
AI ile ödeme tahmini:
- Teminat limit kontrolü
- Muafiyet ve katılım payı hesaplama
- Confidence scoring
- Reason explanation

### 5. **Customer Support Chatbot**
Müşteri sorularını yanıtla:
- Poliçe sorguları
- Teminat bilgileri
- İşlem adımları
- Acil durum yönlendirmesi

---

## 🤖 LLM Seçenekleri

### 1. **OpenAI GPT-4/GPT-3.5**
- **Avantajları:** En iyi performans, geniş knowledge base
- **Dezavantajları:** Cost yüksek, rate limits
- **Kullanım:** Karmaşık tıbbi analiz, fraud detection
- **Cost:** $0.03/1K tokens (GPT-4), $0.002/1K tokens (GPT-3.5)

### 2. **Anthropic Claude**
- **Avantajları:** Güvenlik odaklı, long context (200K tokens)
- **Dezavantajları:** Yeni platform, integration karmaşık
- **Kullanım:** Sensitive data analysis, compliance
- **Cost:** $0.015/1K tokens (Claude 3)

### 3. **Google Gemini**
- **Avantajları:** Google ecosystem, multimodal (image analysis)
- **Dezavantajları:** Rate limits, API complexity
- **Kullanım:** Image analysis (medikal görüntüler)
- **Cost:** $0.001/1K tokens (Gemini Pro)

### 4. **Local LLM (Llama, Mistral)**
- **Avantajları:** Free, private, no external calls
- **Dezavantajları:** GPU gerekir, lower performance
- **Kullanım:** Development, testing, privacy-critical
- **Hardware:** RTX 3090 veya A100 GPU gerekir

### 5. **Azure OpenAI**
- **Avantajları:** Enterprise security, compliance, region-specific
- **Dezavantajları:** Azure bağımlılığı, cost
- **Kullanım:** Enterprise deployment, GDPR compliance
- **Cost:** Similar to OpenAI + Azure fees

---

## 🔧 Spring Boot Entegrasyonu

### 1. **Dependency Ekleme**

#### **OpenAI SDK**
```xml
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>retrofit</artifactId>
    <version>2.9.0</version>
</dependency>
<dependency>
    <groupId>com.squareup.retrofit2</groupId>
    <artifactId>converter-gson</artifactId>
    <version>2.9.0</version>
</dependency>
```

#### **Spring AI (Beta)**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>0.8.0</version>
</dependency>
```

#### **LangChain4j**
```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.29.1</version>
</dependency>
```

### 2. **Configuration**

#### **application.yaml**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
          max-tokens: 1000
          
openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4
  timeout: 30000
  
ai:
  enabled: true
  rate-limit: 10  # requests per minute
  cache-enabled: true
```

#### **AI Config Class**
```java
@Configuration
public class AIConfig {
    
    @Value("${openai.api-key}")
    private String apiKey;
    
    @Value("${openai.model}")
    private String model;
    
    @Value("${ai.enabled}")
    private boolean aiEnabled;
    
    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey);
    }
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .temperature(0.7)
                .build();
    }
    
    @Bean
    @ConditionalOnProperty(name = "ai.enabled", havingValue = "true")
    public AIService aiService(ChatLanguageModel chatModel) {
        return new OpenAIService(chatModel);
    }
    
    @Bean
    @ConditionalOnProperty(name = "ai.enabled", havingValue = "false")
    public AIService mockAIService() {
        return new MockAIService();
    }
}
```

### 3. **AI Service Implementation**

#### **AIService Interface**
```java
public interface AIService {
    
    // Hasar açıklaması analizi
    ClaimAnalysis analyzeClaimDescription(String description);
    
    // ICD-10 kod tahmini
    String predictICDCode(String diagnosis);
    
    // Fraud detection
    FraudRisk detectFraud(Claim claim);
    
    // Ödeme tahmini
    PaymentEstimation estimatePayment(Claim claim);
    
    // Customer support
    String answerCustomerQuestion(String question);
}
```

#### **OpenAI Implementation**
```java
@Service
@Slf4j
public class OpenAIService implements AIService {
    
    private final ChatLanguageModel chatModel;
    
    public OpenAIService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    @Override
    public ClaimAnalysis analyzeClaimDescription(String description) {
        String prompt = String.format("""
            Bir sağlık sigortası hasar açıklamasını analiz et.
            Açıklama: %s
            
            Lütfen şu bilgileri JSON formatında ver:
            - severity: LOW, MEDIUM, HIGH
            - priority: 1-10 arası puan
            - category: HASTANE_DOKTOR, LABORATUVAR, ILAÇ, DIGER
            - keywords: İlgili anahtar kelimeler (array)
            - urgency: NORMAL, ACIL, ACIL_YARDIM
            """, description);
        
        String response = chatModel.generate(prompt);
        return parseClaimAnalysis(response);
    }
    
    @Override
    public String predictICDCode(String diagnosis) {
        String prompt = String.format("""
            Aşağıdaki tanı için ICD-10 kodunu tahmin et.
            Tanı: %s
            
            Sadece ICD-10 kodunu (örn: I21, J45, E11) ver.
            """, diagnosis);
        
        return chatModel.generate(prompt).trim();
    }
    
    @Override
    public FraudRisk detectFraud(Claim claim) {
        String prompt = String.format("""
            Aşağıdaki hasar bilgilerini fraud risk analizine tabi tut.
            
            Hasar Bilgileri:
            - Poliçe ID: %s
            - İhbar Tarihi: %s
            - Olay Tarihi: %s
            - Talep Tutarı: %s
            - Hastane: %s
            - Tanı: %s
            - Raporlayan: %s
            
            Lütfen şu bilgileri JSON formatında ver:
            - risk_level: LOW, MEDIUM, HIGH, CRITICAL
            - risk_score: 0-100 arası puan
            - risk_factors: Risk faktörleri (array)
            - recommendation: Öneri
            """, 
            claim.getPolicyId(),
            claim.getReportDate(),
            claim.getIncidentDate(),
            claim.getClaimedAmount(),
            claim.getHospitalName(),
            claim.getDiagnosis(),
            claim.getReporter()
        );
        
        String response = chatModel.generate(prompt);
        return parseFraudRisk(response);
    }
    
    @Override
    public PaymentEstimation estimatePayment(Claim claim) {
        String prompt = String.format("""
            Aşağıdaki hasar için ödeme tahmini yap.
            
            Hasar Bilgileri:
            - Talep Tutarı: %s
            - Muafiyet: %s
            - Katılım Payı: %s
            - Teminat Limiti: %s
            - Tanı: %s
            - Hasar Tipi: %s
            
            Hesaplama kuralı:
            Tahmini Ödeme = (Talep - Muafiyet) * (1 - KatılımPayı)
            Teminat limitini aşmamalı.
            
            JSON formatında ver:
            - estimated_amount: Tahmini ödeme
            - reason: Hesaplama nedeni
            - confidence: Güven oranı (0-1)
            """,
            claim.getClaimedAmount(),
            claim.getDeductible(),
            claim.getCoPaymentPercentage(),
            claim.getCoverageLimit(),
            claim.getDiagnosis(),
            claim.getClaimType()
        );
        
        String response = chatModel.generate(prompt);
        return parsePaymentEstimation(response);
    }
    
    @Override
    public String answerCustomerQuestion(String question) {
        String prompt = String.format("""
            Bir sağlık sigortası müşterisinin sorusunu Türkçe olarak yanıtla.
            Soru: %s
            
            Yanıt:
            - Kısa ve net ol
            - Pratik bilgiler ver
            - Gerekirse işlem adımları belirt
            """, question);
        
        return chatModel.generate(prompt);
    }
    
    private ClaimAnalysis parseClaimAnalysis(String response) {
        // JSON parsing logic
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response, ClaimAnalysis.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            return ClaimAnalysis.builder()
                    .severity("MEDIUM")
                    .priority(5)
                    .category("DIGER")
                    .keywords(Collections.emptyList())
                    .urgency("NORMAL")
                    .build();
        }
    }
    
    private FraudRisk parseFraudRisk(String response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response, FraudRisk.class);
        } catch (Exception e) {
            log.error("Failed to parse fraud risk", e);
            return FraudRisk.builder()
                    .riskLevel("LOW")
                    .riskScore(0)
                    .riskFactors(Collections.emptyList())
                    .recommendation("AI analysis failed")
                    .build();
        }
    }
    
    private PaymentEstimation parsePaymentEstimation(String response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response, PaymentEstimation.class);
        } catch (Exception e) {
            log.error("Failed to parse payment estimation", e);
            return PaymentEstimation.builder()
                    .estimatedAmount(BigDecimal.ZERO)
                    .reason("AI analysis failed")
                    .confidence(0.0)
                    .build();
        }
    }
}
```

#### **Mock Implementation (Testing)**
```java
@Service
@Profile("test")
public class MockAIService implements AIService {
    
    @Override
    public ClaimAnalysis analyzeClaimDescription(String description) {
        return ClaimAnalysis.builder()
                .severity("MEDIUM")
                .priority(5)
                .category("DIGER")
                .keywords(Arrays.asList("test"))
                .urgency("NORMAL")
                .build();
    }
    
    @Override
    public String predictICDCode(String diagnosis) {
        return "Z00"; // Genel check-up kodu
    }
    
    @Override
    public FraudRisk detectFraud(Claim claim) {
        return FraudRisk.builder()
                .riskLevel("LOW")
                .riskScore(10)
                .riskFactors(Collections.emptyList())
                .recommendation("Mock result")
                .build();
    }
    
    @Override
    public PaymentEstimation estimatePayment(Claim claim) {
        return PaymentEstimation.builder()
                .estimatedAmount(claim.getClaimedAmount().multiply(BigDecimal.valueOf(0.8)))
                .reason("Mock calculation")
                .confidence(0.7)
                .build();
    }
    
    @Override
    public String answerCustomerQuestion(String question) {
        return "Mock answer for: " + question;
    }
}
```

### 4. **Controller Entegrasyonu**

#### **ClaimController Enhancement**
```java
@RestController
@RequestMapping("/api/claims")
@Tag(name = "Claim Management")
public class ClaimController {
    
    private final ClaimService claimService;
    private final AIService aiService;
    
    @PostMapping("/analyze")
    @Operation(summary = "Analyze claim description with AI")
    public ResponseEntity<ClaimAnalysis> analyzeClaim(
            @RequestBody ClaimAnalysisRequest request) {
        
        ClaimAnalysis analysis = aiService.analyzeClaimDescription(
            request.getDescription()
        );
        
        return ResponseEntity.ok(analysis);
    }
    
    @PostMapping("/{id}/fraud-check")
    @Operation(summary = "Check claim for fraud with AI")
    public ResponseEntity<FraudRisk> checkFraud(@PathVariable UUID id) {
        
        Claim claim = claimService.getClaimById(id);
        FraudRisk risk = aiService.detectFraud(claim);
        
        // High risk ise otomatik flag at
        if (risk.getRiskLevel().equals("HIGH") || 
            risk.getRiskLevel().equals("CRITICAL")) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
            claim.setFraudFlag(true);
            claimService.updateClaim(id, claim);
        }
        
        return ResponseEntity.ok(risk);
    }
    
    @PostMapping("/{id}/estimate-payment")
    @Operation(summary = "Estimate payment with AI")
    public ResponseEntity<PaymentEstimation> estimatePayment(@PathVariable UUID id) {
        
        Claim claim = claimService.getClaimById(id);
        PaymentEstimation estimation = aiService.estimatePayment(claim);
        
        return ResponseEntity.ok(estimation);
    }
    
    @PostMapping("/icd-code")
    @Operation(summary = "Predict ICD-10 code from diagnosis")
    public ResponseEntity<String> predictICDCode(
            @RequestBody ICDCodeRequest request) {
        
        String icdCode = aiService.predictICDCode(request.getDiagnosis());
        
        return ResponseEntity.ok(icdCode);
    }
}
```

### 5. **BFF Entegrasyonu**

#### **BffClaimController Enhancement**
```java
@RestController
@RequestMapping("/api/bff/claims")
public class BffClaimController {
    
    private final RestTemplate restTemplate;
    private final AIService aiService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @PostMapping("/{id}/ai-analysis")
    public ResponseEntity<ClaimAnalysis> getAIAnalysis(@PathVariable UUID id) {
        
        // 1. Cache'den kontrol et
        String cacheKey = "claim-analysis:" + id;
        ClaimAnalysis cached = (ClaimAnalysis) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        
        // 2. Claim'i al
        Claim claim = restTemplate.getForObject(
            "http://localhost:8081/api/claims/" + id, 
            Claim.class
        );
        
        // 3. AI analizi yap
        ClaimAnalysis analysis = aiService.analyzeClaimDescription(
            claim.getDescription()
        );
        
        // 4. Sonucu cache'le
        redisTemplate.opsForValue().set(
            cacheKey, 
            analysis, 
            Duration.ofHours(1)
        );
        
        return ResponseEntity.ok(analysis);
    }
    
    @PostMapping("/customer-support")
    public ResponseEntity<String> customerSupport(
            @RequestBody CustomerSupportRequest request) {
        
        String answer = aiService.answerCustomerQuestion(request.getQuestion());
        
        return ResponseEntity.ok(answer);
    }
}
```

---

## 🔒 Security Considerations

### 1. **API Key Management**
```yaml
# Environment variables kullan
openai:
  api-key: ${OPENAI_API_KEY}  # NEVER commit to git
```

### 2. **PII Protection**
```java
// Sensitive data maskeleme
public String maskSensitiveData(String data) {
    return data.replaceAll("\\d{11}", "***********"); // TC kimlik no
}
```

### 3. **Rate Limiting**
```java
@RateLimiter(value = "ai-api", fallbackMethod = "fallbackMethod")
public ClaimAnalysis analyzeClaimDescription(String description) {
    // Max 10 request/minute
}
```

### 4. **Content Filtering**
```java
// Prompt injection koruması
public String sanitizePrompt(String prompt) {
    // Malicious content'i temizle
    return prompt.replaceAll(".*ignore.*", "");
}
```

---

## 💰 Cost Management

### 1. **Caching Strategy**
```java
@Cacheable(value = "ai-analysis", key = "#description.hashCode()")
public ClaimAnalysis analyzeClaimDescription(String description) {
    // Aynı açıklama tekrar işlenmez
}
```

### 2. **Token Optimization**
```java
// Prompt uzunluğunu minimize et
String shortPrompt = "Analyze: " + description.substring(0, 500);
```

### 3. **Fallback Logic**
```java
// AI başarısız olursa rule-based fallback
public ClaimAnalysis analyzeClaimDescription(String description) {
    try {
        return aiService.analyzeClaimDescription(description);
    } catch (Exception e) {
        return ruleBasedAnalysis(description);
    }
}
```

### 4. **Cost Tracking**
```java
@Component
public class AICostTracker {
    
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final AtomicLong totalCost = new AtomicLong(0);
    
    public void trackUsage(int tokens) {
        totalTokens.addAndGet(tokens);
        // $0.03/1K tokens (GPT-4)
        long cost = (tokens * 3) / 1000;  // cents
        totalCost.addAndGet(cost);
    }
    
    public long getTotalCost() {
        return totalCost.get();
    }
}
```

---

## 🚀 Deployment

### 1. **Docker Compose**
```yaml
services:
  claim-service:
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - AI_ENABLED=true
      - AI_RATE_LIMIT=10
```

### 2. **Kubernetes**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: openai-secret
data:
  api-key: BASE64_ENCODED_KEY
---
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: claim-service
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: openai-secret
              key: api-key
        - name: AI_ENABLED
          value: "true"
```

### 3. **GitHub Actions Secret**
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Production
        env:
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
        run: |
          # Deployment script
```

---

## 📊 Performance Optimization

### 1. **Async Processing**
```java
@Async
public CompletableFuture<ClaimAnalysis> analyzeClaimDescriptionAsync(String description) {
    return CompletableFuture.completedFuture(
        aiService.analyzeClaimDescription(description)
    );
}
```

### 2. **Batch Processing**
```java
public List<ClaimAnalysis> analyzeClaimsInBatch(List<String> descriptions) {
    // Multiple requests parallel
    return descriptions.parallelStream()
        .map(aiService::analyzeClaimDescription)
        .collect(Collectors.toList());
}
```

### 3. **Circuit Breaker**
```java
@CircuitBreaker(fallbackMethod = "fallbackAnalysis")
public ClaimAnalysis analyzeClaimDescription(String description) {
    return aiService.analyzeClaimDescription(description);
}

private ClaimAnalysis fallbackAnalysis(String description) {
    return ruleBasedAnalysis(description);
}
```

### 4. **Connection Pooling**
```java
@Configuration
public class OkHttpClientConfig {
    
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
```

---

## 🎯 Örnek Kullanım Senaryosu

### **Hasar Oluşturma ve AI Analizi**
```java
// 1. Hasar oluştur
Claim claim = new Claim();
claim.setDescription("Hasta acil servise girdi, kalp krizi teşhisi konuldu, stent takıldı");
claim.setClaimedAmount(new BigDecimal("15000.00"));
claim.setDeductible(new BigDecimal("1000.00"));
claim.setCoPaymentPercentage(15.0);
claim.setCoverageLimit(new BigDecimal("50000.00"));

// 2. AI analizi
ClaimAnalysis analysis = aiService.analyzeClaimDescription(claim.getDescription());
// Sonuç: severity=HIGH, priority=9, urgency=ACIL

// 3. Fraud check
FraudRisk fraud = aiService.detectFraud(claim);
// Sonuç: risk_level=MEDIUM, risk_score=35

// 4. Ödeme tahmini
PaymentEstimation payment = aiService.estimatePayment(claim);
// Sonuç: estimated_amount=11850.00

// 5. Sonuçları claim'e ekle
claim.setSeverity(analysis.getSeverity());
claim.setFraudRisk(fraud.getRiskScore());
claim.setEstimatedPayment(payment.getEstimatedAmount());
```

---

## 🎓 Sonuç

AI entegrasyonu sağlık sigortası hasar yönetiminde büyük fayda sağlar:
- **Otomasyon:** Manuel işleri azaltır
- **Accuracy:** Hata oranını düşürür
- **Speed:** İşlem süresini kısaltır
- **Cost:** Uzun vadede maliyet tasarrufu
- **Experience:** Müşteri deneyimini iyileştirir

Ancak dikkatli implementasyon gerekir:
- Security (API key management)
- Privacy (PII protection)
- Cost (token optimization)
- Reliability (fallback logic)
- Monitoring (cost tracking)

Spring Boot ile entegrasyon kolay ve production-ready.