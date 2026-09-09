# 👨‍💼 Sigorta Çalışanları için AI-Powered Dashboard

## 🎯 Amaç

Sigorta çalışanlarının müşterilerin poliçe kapsamını hızlıca sorgulaması ve doküman önerileri alması için AI-powered dashboard.

---

## 🎯 Temel Senaryo

### Çalışan Sorusu:
> "Müşteri hastanede x işlemini gerçekleştiriyor, bu kullanıcının poliçesi buna uygun mu?"

### AI Yanıtı:
> "Evet, bu poliçe bu işlemi kapsar. Kalp krizi tedavisi teminat dahilindedir. Tahmini ödeme: 12,000 TL, Müşterinin ödemesi gereken: 1,800 TL"

---

## 📊 Dashboard Endpoint'leri

### 1. **Poliçe Validasyon** 🚀
```http
POST /api/bff/employee/validate-policy
```

#### Senaryo
Çalışan müşteriden bilgi alır ve AI ile poliçe kapsamını kontrol eder.

#### İstek Örneği
```json
{
  "policyId": "uuid-123",
  "tcKimlikNo": "12345678901",
  "medicalProcedure": "Hasta acil servise girdi, kalp krizi teşhisi konuldu, stent takıldı",
  "hospitalName": "Acıbadem Hastanesi",
  "procedureDate": "2026-09-09",
  "estimatedCost": 15000.00,
  "procedureCategory": "HASTANE_DOKTOR",
  "isEmergency": true,
  "employeeQuestion": "Bu kullanıcının poliçesi buna uygun mu?"
}
```

#### Cevap Örneği
```json
{
  "isCovered": true,
  "decision": "POLİÇE KAPSAR",
  "explanation": "Kalp krizi tedavisi teminat dahilindedir. Acil durum olarak değerlendirilmiştir.",
  "coverageLimit": 50000.00,
  "usedCoverage": 20000.00,
  "remainingCoverage": 30000.00,
  "estimatedPayment": 12000.00,
  "customerPayment": 1800.00,
  "coverageCategory": "ACIL_DURUM",
  "deductible": 1000.00,
  "coPaymentPercentage": 15.0,
  "applicableRules": [
    "Acil durum kuralı: Öncelikli işlem",
    "Muafiyet uygulanır",
    "Katılım payı uygulanır"
  ],
  "warnings": [
    "Önceki hasarlar yoğun"
  ],
  "recommendations": [
    "Hasta raporu alınmalı",
    "Fatura detayları kontrol edilmeli"
  ],
  "requiredDocuments": [
    "Hasta raporu",
    "Fatura",
    "Kimlik fotokopisi",
    "Acil durum belgesi"
  ],
  "confidence": 0.9,
  "detectedKeywords": ["hasta", "acil", "kalp", "kriz", "stent"],
  "procedureCategory": "HASTANE_DOKTOR",
  "isEmergency": true,
  "preApprovalRequired": false,
  "specialConditions": ["Acil durum önceliği"],
  "analysisSource": "AI"
}
```

#### Swagger UI Test
1. Swagger UI'ye git: `http://localhost:8080/swagger-ui.html`
2. "Employee Dashboard" section'ı bul
3. `/api/bff/employee/validate-policy` endpoint'ini seç
4. "Try it out" butonuna tıkla
5. Yukarıdaki JSON request'i yapıştır
6. "Execute" butonuna tıkla
7. AI response'u gör

---

### 2. **Doküman Önerisi** 📄
```http
POST /api/bff/employee/recommend-documents
```

#### Senaryo
Çalışan sorar: "Bu durum için hangi dokümanları kontrol etmeliyim?"

#### İstek Örneği
```json
{
  "policyId": "uuid-123",
  "medicalProcedure": "Hasta acil servise girdi, kalp krizi teşhisi konuldu",
  "procedureCategory": "HASTANE_DOKTOR",
  "isEmergency": true
}
```

#### Cevap Örneği
```json
{
  "requiredDocuments": [
    "Kimlik fotokopisi",
    "Poliçe fotokopisi",
    "Hasta raporu",
    "Fatura",
    "Acil durum belgesi"
  ],
  "priority": {
    "Kimlik fotokopisi": 1,
    "Poliçe fotokopisi": 1,
    "Acil durum belgesi": 1,
    "Hasta raporu": 2,
    "Fatura": 2
  },
  "reason": "HASTANE_DOKTOR kategorisindeki Hasta acil servise girdi, kalp krizi teşhisi konuldu işlemi için standart dokümanlar gerekli.",
  "deadline": "48 saat",
  "specialNotes": [
    "Acil durum belgesi en geç 48 saat içinde sunulmalı",
    "Fatura resmi kaşe ve imzalı olmalı",
    "Hasta raporu doktor imzalı olmalı"
  ]
}
```

#### Öncelik Sistemi
- **Priority 1:** En yüksek öncelik (acil)
- **Priority 2:** Orta öncelik
- **Priority 3:** Normal öncelik

---

### 3. **Hızlı Policy Check** ⚡
```http
GET /api/bff/employee/quick-check?question=...&policyId=...
```

#### Senaryo
Hızlı soru-yanıt formatında kontrol.

#### İstek Örneği
```
GET /api/bff/employee/quick-check?question=Polisemde check-up var mı?&policyId=uuid-123
```

#### Cevap Örneği
```json
{
  "answer": "Evet, bu poliçe check-up teminatını kapsar. Yıllık 1 check-up hakkınız var.",
  "source": "AI"
}
```

#### Kullanım Alanları
- Hızlı sorgular
- Müşteri cevapları
- Temel teminat kontrolü

---

### 4. **Toplu Policy Validasyon** 📋
```http
POST /api/bff/employee/validate-policies-batch
```

#### Senaryo
Birden fazla işlem için aynı anda validasyon.

#### İstek Örneği
```json
[
  {
    "policyId": "uuid-123",
    "medicalProcedure": "Kalp krizi tedavisi",
    "estimatedCost": 15000.00,
    "isEmergency": true
  },
  {
    "policyId": "uuid-123",
    "medicalProcedure": "Kan tahlili",
    "estimatedCost": 500.00,
    "isEmergency": false
  }
]
```

#### Cevap Örneği
```json
[
  {
    "isCovered": true,
    "decision": "POLİÇE KAPSAR",
    "estimatedPayment": 12000.00,
    "customerPayment": 1800.00
  },
  {
    "isCovered": true,
    "decision": "POLİÇE KAPSAR",
    "estimatedPayment": 425.00,
    "customerPayment": 75.00
  }
]
```

#### Kullanım Alanları
- Birden fazla işlem karşılaştırma
- Cost estimation toplu işlem
- Customer dashboard hazırlama

---

### 5. **Policy Comparison** 🔄
```http
GET /api/bff/employee/compare-policies?policyId1=...&policyId2=...
```

#### Senaryo
İki poliçe arasındaki farkları karşılaştır.

#### İstek Örneği
```
GET /api/bff/employee/compare-policies?policyId1=uuid-123&policyId2=uuid-456
```

#### Cevap Örneği
```json
{
  "policy1": {
    "policyNumber": "POL-001",
    "policyType": "ELEMENTARY_HEALTH",
    "coverageLimit": 50000.00,
    "deductible": 1000.00,
    "coPaymentPercentage": 15.0
  },
  "policy2": {
    "policyNumber": "POL-002",
    "policyType": "COMPLEMENTARY_HEALTH",
    "coverageLimit": 100000.00,
    "deductible": 500.00,
    "coPaymentPercentage": 10.0
  },
  "differences": {
    "coverageLimit_diff": -50000.00,
    "deductible_diff": 500.00,
    "coPayment_diff": 5.0
  },
  "betterForEmergency": "Policy 2",
  "betterForChronic": "Policy 2",
  "costComparison": {
    "annualPremium_diff": -2000.00,
    "cost_per_coverage_1": 0.04,
    "cost_per_coverage_2": 0.03
  }
}
```

#### Kullanım Alanları
- Upselling
- Policy comparison
- Customer education

---

## 🎨 Frontend Dashboard Design

### 📱 Mobile View
```
┌─────────────────────────┐
│ Sigorta Dashboard       │
├─────────────────────────┤
│ Müşteri Ara              │
│ ┌─────────────────────┐ │
│ │ TC Kimlik: [____]   │ │
│ │ [Ara]               │ │
│ └─────────────────────┘ │
│                         │
│ Müşteri: Ahmet Yılmaz   │
│ Poliçe: POL-001         │
│                         │
│ ┌─────────────────────┐ │
│ │ İşlem Detayları     │ │
│ │ ┌─────────────────┐ │ │
│ │ │Hastane: [____]  │ │ │
│ │ │İşlem: [____]    │ │ │
│ │ │Maliyet: [____]  │ │ │
│ │ │[Acil]            │ │ │
│ │ └─────────────────┘ │ │
│ │ [Validasyon Yap]    │ │
│ └─────────────────────┘ │
│                         │
│ AI Sonucu:              │
│ ✅ POLİÇE KAPSAR        │
│ Ödeme: 12,000 TL       │
│                         │
│ Gerekli Dokümanlar:     │
│ 1. Hasta raporu         │
│ 2. Fatura               │
│ 3. Kimlik               │
└─────────────────────────┘
```

### 💻 Desktop View
```
┌─────────────────────────────────────────────────────────────┐
│ Sigorta Çalışan Dashboard - AI-Powered                     │
├─────────────────────────────────────────────────────────────┤
│ 🔍 Müşteri Ara    📋 Hasar Listesi    📊 İstatistikler      │
├─────────────────────────────────────────────────────────────┤
│ Müşteri Bilgileri          │ AI Validasyon Paneli            │
│ ┌──────────────────────┐ │ ┌────────────────────────────┐ │
│ │ Ad: Ahmet Yılmaz     │ │ │ 🤖 AI Policy Check         │ │
│ │ TC: 12345678901      │ │ │                            │ │
│ │ Poliçe: POL-001      │ │ │ İşlem: Kalp krizi tedavisi │ │
│ │ Tip: Elementer Sağlık │ │ │ Hastane: Acıbadem          │ │
│ │ Durum: AKTİF         │ │ │ Maliyet: 15,000 TL         │ │
│ │ Teminat: 50,000 TL   │ │ │ [Acil durum]               │ │
│ └──────────────────────┘ │ │                            │ │
│                          │ │ [AI ile Analiz Et]          │ │
│ ┌──────────────────────┐ │ └────────────────────────────┘ │
│ │ 🎯 Hızlı Sorular     │ │                                │
│ │ • Check-up var mı?   │ │ AI Sonucu:                     │
│ │ • Diş teminatı?      │ │ ✅ POLİÇE KAPSAR             │
│ │ • Gözlük teminatı?   │ │ Tahmini Ödeme: 12,000 TL      │
│ │ • Emzirme teminatı?  │ │ Müşteri Ödemesi: 1,800 TL    │
│ └──────────────────────┘ │                                │
│                          │ 📄 Gerekli Dokümanlar           │
│ ┌──────────────────────┐ │ 1. Hasta raporu ⚡             │
│ │ 📊 Son İşlemler      │ │ 2. Fatura ⚡                   │
│ │ • 15.08.2026 - Check │ │ 3. Kimlik fotokopisi ⚡        │
│ │   up (750 TL)        │ │ 4. Acil durum belgesi ⚡       │
│ │ • 10.07.2026 - Diş   │ │                                │
│ │   tedavisi (2,500 TL)│ │ ⚠️ Uyarılar:                   │
│ │ • 05.06.2026 - Laboratuvar │ • Önceki hasarlar yoğun   │
│ │   tahlili (500 TL)   │ │                                │
│ └──────────────────────┘ │ 💡 Öneriler:                   │
│                          │ • Hasta raporu alınmalı         │
│ ┌──────────────────────┐ │ • Fatura detayları kontrol edilmeli│
│ │ 🔄 Policy Comparison │ │                                │
│ │ [İki Poliçe Karşılaştır]│                                │
│ └──────────────────────┘ │                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Teknik Detaylar

### 🏗️ Mimari
```
Frontend Dashboard
        ↓
BFF Service (EmployeeDashboardController)
        ↓
EmployeeDashboardService
        ↓
┌─────────────────┬─────────────────┐
│ Policy Service   │ AI Service      │
│ (Policy data)    │ (OpenAI API)    │
└─────────────────┴─────────────────┘
```

### 🤖 AI Entegrasyonu

#### **AI-Based Validation**
```java
// AI kullanırsa
if (aiEnabled) {
    String prompt = buildAIPrompt(request, policyData);
    String aiResponse = callOpenAI(prompt);
    return parseAIResponse(aiResponse, policyData);
}
```

#### **Rule-Based Validation**
```java
// AI yoksa veya hata olursa
else {
    return ruleBasedValidation(request, policyData);
}
```

#### **Fallback Logic**
```java
try {
    return aiBasedValidation(request, policyData);
} catch (Exception e) {
    log.error("AI validation failed, falling back to rule-based", e);
    return ruleBasedValidation(request, policyData);
}
```

### 📊 Validasyon Kuralları

#### **Basic Rules**
```java
boolean isCovered = isCoveredByRules(request, policyType, policyStatus);

// 1. Policy status kontrolü
if (!"ACTIVE".equals(policyStatus)) {
    return false;
}

// 2. Acil durum kontrolü
if (request.getIsEmergency()) {
    return true; // Acil durumlar genelde kapsar
}

// 3. Policy type kontrolü
if ("ELEMENTARY_HEALTH".equals(policyType)) {
    return Arrays.asList("HASTANE_DOKTOR", "LABORATUVAR", "ILAÇ", "CERRAHI")
            .contains(request.getProcedureCategory());
}
```

#### **Payment Calculation**
```java
// Ödeme hesaplama
BigDecimal customerPayment = calculateCustomerPayment(
    estimatedCost, deductible, coPaymentPercentage, isEmergency
);

// Acil durumda muafiyet %50 düşer
if (isEmergency) {
    deductible = deductible.multiply(BigDecimal.valueOf(0.5));
}

// Katılım payı hesapla
BigDecimal coPayment = afterDeductible.multiply(coPaymentPercentage)
    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

// Müşteri ödemesi = muafiyet + katılım payı
return deductible.add(coPayment);
```

### 📄 Doküman Önerisi

#### **Rule-Based**
```java
List<String> docs = new ArrayList<>();
docs.add("Kimlik fotokopisi");
docs.add("Poliçe fotokopisi");
docs.add("Hasta raporu");
docs.add("Fatura");

if (request.getIsEmergency()) {
    docs.add("Acil durum belgesi");
}
```

#### **Priority Assignment**
```java
Map<String, Integer> priority = new HashMap<>();

// Acil durumda en yüksek öncelik
int basePriority = isEmergency ? 1 : 3;

// Kimlik ve poliçe en yüksek öncelik
if (doc.contains("Kimlik") || doc.contains("Poliçe")) {
    priority.put(doc, basePriority);
}
```

---

## 🎯 Kullanım Senaryoları

### Senaryo 1: Acil Durum Validasyonu
```
Çalışan: "Müşteri şu an acil serviste, kalp krizi geçirdi. 
         Poliçesi bunu kapsar mı?"

AI: "Evet, poliçe kapsar. Acil durum önceliği var. 
     Tahmini ödeme: 12,000 TL, Müşteri ödemesi: 1,800 TL"

Çalışan: "Hangi dokümanları kontrol etmeliyim?"

AI: "Acil durum belgesi (48 saat), hasta raporu, fatura, kimlik"
```

### Senaryo 2: Planlı Tedavi
```
Çalışan: "Müşteri check-up yapacak, teminat dahilinde mi?"

AI: "Evet, yıllık 1 check-up hakkınız var. 
     Teminat dahilinde, maliyet: 750 TL"

Çalışan: "Gerekli dokümanlar?"

AI: "Randevu formu, kimlik, poliçe fotokopisi"
```

### Senaryo 3: Laboratuvar Tahlili
```
Çalışan: "Kan tahlili yapılacak, poliçe kapsar mı?"

AI: "Evet, laboratuvar teminatı dahilinde. 
     Tahmini ödeme: 425 TL, Müşteri ödemesi: 75 TL"

Çalışan: "Dokümanlar?"

AI: "Laboratuvar sonuçları, fatura, kimlik"
```

### Senaryo 4: Policy Comparison
```
Çalışan: "Müşteri daha iyi bir poliçe istiyor, 
         mevcut poliçe ile yeni poliçe arasındaki farklar?"

AI: "Yeni poliçe (COMPLEMENTARY_HEALTH) daha avantajlı:
     - Teminat limiti: 100,000 TL (mevcut: 50,000 TL)
     - Muafiyet: 500 TL (mevcut: 1,000 TL)
     - Katılım payı: %10 (mevcut: %15)
     - Yıllık prim farkı: +2,000 TL"
```

---

## 🔒 Security Considerations

### 1. **Authentication**
```java
// JWT token doğrulama
// Role-based access control
// Çalışan rolü gerekli
```

### 2. **Data Privacy**
```java
// TC kimlik no maskeleme
// Hastane bilgisi gizliliği
// AI prompt'ta sensitive data filtering
```

### 3. **Rate Limiting**
```java
// Çalışan başına request limit
// DDoS koruması
// API throttling
```

---

## 💰 Cost Management

### 1. **AI Cost Tracking**
```java
@Component
public class AICostTracker {
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final AtomicLong totalCost = new AtomicLong(0);
    
    public void trackUsage(int tokens) {
        totalTokens.addAndGet(tokens);
        long cost = (tokens * 3) / 1000; // cents
        totalCost.addAndGet(cost);
    }
}
```

### 2. **Caching Strategy**
```java
@Cacheable(value = "policy-validation", key = "#request.hashCode()")
public PolicyValidationResponse validatePolicy(PolicyValidationRequest request) {
    // Aynı request tekrar işlenmez
}
```

### 3. **Fallback to Rule-Based**
```java
// AI başarısız olursa rule-based fallback
// Cost control
// Reliability assurance
```

---

## 🚀 Deployment

### Docker Compose
```yaml
services:
  bff-service:
    environment:
      - AI_ENABLED=true
      - AI_MODEL=gpt-4
      - OPENAI_API_KEY=${OPENAI_API_KEY}
```

### Environment Variables
```bash
AI_ENABLED=true
AI_MODEL=gpt-4
OPENAI_API_KEY=sk-...
AI_RATE_LIMIT=10
```

---

## 🎓 Sonuç

Employee dashboard ile sigorta çalışanları:
- **Hızlı:** Saniyeler içinde policy validasyon
- **Doğru:** AI-powered accurate analysis
- **Etkili:** Otomatik doküman önerisi
- **Müşteri Odaklı:** Better customer service
- **Verimli:** Reduced manual work

Bu dashboard, sigorta çalışanlarının günlük işlerini kolaylaştırır ve müşteri deneyimini iyileştirir.