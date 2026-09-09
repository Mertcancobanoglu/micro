# 🤖 Chatbot Entegrasyonu - Sigorta Çalışanları için AI Asistan

## 🎯 Amaç

Sigorta çalışanlarının doğal dil ile poliçe validasyon yapması, doküman önerisi alması ve hızlı sorular sorması için AI-powered chatbot.

---

## 🎯 Temel Senaryo

### Çalışan:
> "Müşteri kalp krizi geçirdi, poliçe kapsar mı?"

### Chatbot:
> "✅ POLİÇE KAPSAR
> 
> 📊 Ödeme Detayları:
> - Tahmini Ödeme: 12,000 TL
> - Müşteri Ödemesi: 1,800 TL
> - Teminat Kategorisi: ACIL_DURUM
> 
> ⚠️ Uyarılar:
> - Önceki hasarlar yoğun
> 
> 💡 Öneriler:
> - Hasta raporu alınmalı
> - Fatura detayları kontrol edilmeli
> 
> 📄 Gerekli Dokümanlar görmek ister misiniz?"

---

## 📊 Chatbot Endpoint'leri

### 1. **REST API - Mesaj İşleme** 💬
```http
POST /api/bff/chatbot/message
```

#### İstek Örneği
```json
{
  "message": "Müşteri kalp krizi geçirdi, poliçe kapsar mı?",
  "conversationId": "conv-123",
  "userId": "user-456",
  "context": {
    "policyId": "uuid-123"
  }
}
```

#### Cevap Örneği
```json
{
  "message": "✅ **POLİÇE KAPSAR**\n\n📊 **Ödeme Detayları:**\n- Tahmini Ödeme: 12,000 TL\n- Müşteri Ödemesi: 1,800 TL\n- Teminat Kategorisi: ACIL_DURUM",
  "conversationId": "conv-123",
  "messageId": "msg-789",
  "responseType": "CARD",
  "intent": "POLICY_VALIDATION",
  "confidence": 0.9,
  "suggestedActions": [
    {
      "type": "BUTTON",
      "label": "Doküman Önerisi",
      "action": "DOCUMENT_RECOMMENDATION",
      "icon": "📄"
    },
    {
      "type": "BUTTON",
      "label": "Detaylı Bilgi",
      "action": "SHOW_DETAILS",
      "icon": "ℹ️"
    }
  ],
  "followUpQuestions": [
    "Gerekli dokümanlar neler?",
    "Ödeme detayları",
    "Başka bir işlem kontrol et"
  ],
  "status": "SUCCESS",
  "timestamp": 1757390400000,
  "processingTime": 450
}
```

### 2. **REST API - Konuşma Başlat** 🆕
```http
POST /api/bff/chatbot/conversation/start?userId=user-456
```

#### Cevap Örneği
```json
{
  "conversationId": "conv-123",
  "userId": "user-456",
  "message": "Yeni konuşma başlatıldı. Size nasıl yardımcı olabilirim?"
}
```

### 3. **REST API - Konuşma Sonlandır** 🏁
```http
POST /api/bff/chatbot/conversation/end?conversationId=conv-123
```

### 4. **WebSocket - Real-time Chat** ⚡
```javascript
// WebSocket connection
const socket = new WebSocket('ws://localhost:8080/ws/chat');

// Send message
socket.send(JSON.stringify({
  message: "Müşteri kalp krizi geçirdi, poliçe kapsar mı?",
  conversationId: "conv-123",
  userId: "user-456"
}));

// Receive message
socket.onmessage = (event) => {
  const response = JSON.parse(event.data);
  console.log(response.message);
};
```

---

## 🎨 Chatbot UI Design

### 💻 Desktop Chat Interface
```
┌─────────────────────────────────────────────────────────────┐
│ 🤖 Sigorta Chatbot - AI Asistan                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  💬 Konuşma Geçmişi                                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 👤 Çalışan: Merhaba                                   │  │
│  │ 🤖 Chatbot: Merhaba! Size nasıl yardımcı olabilirim?  │  │
│  │                                                        │  │
│  │ 👤 Çalışan: Müşteri kalp krizi geçirdi, poliçe kapsar│  │
│  │    mı?                                                  │  │
│  │                                                        │  │
│  │ 🤖 Chatbot: ✅ POLİÇE KAPSAR                         │  │
│  │                                                        │  │
│  │    📊 Ödeme Detayları:                                │  │
│  │    - Tahmini Ödeme: 12,000 TL                         │  │
│  │    - Müşteri Ödemesi: 1,800 TL                       │  │
│  │    - Teminat Kategorisi: ACIL_DURUM                   │  │
│  │                                                        │  │
│  │    [📄 Doküman Önerisi] [ℹ️ Detaylı Bilgi]            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  📝 Mesaj Yaz...                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Mesajınızı buraya yazın...                            │  │
│  └──────────────────────────────────────────────────────┘  │
│  [Gönder] [🎤 Sesli Giriş] [📎 Dosya Ekle]                   │
│                                                              │
│  🎯 Hızlı Sorular                                           │
│  [📋 Poliçe Validasyon] [📄 Doküman Önerisi] [⚡ Hızlı Soru]│
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 📱 Mobile Chat Interface
```
┌─────────────────────────┐
│ 🤖 Sigorta Chatbot     │
├─────────────────────────┤
│                         │
│ 👤: Merhaba            │
│ 🤖: Merhaba! Size      │
│     nasıl yardımcı     │
│     olabilirim?         │
│                         │
│ 👤: Müşteri kalp       │
│     krizi geçirdi,      │
│     poliçe kapsar mı?   │
│                         │
│ 🤖: ✅ POLİÇE KAPSAR  │
│                         │
│     Ödeme: 12,000 TL   │
│     Müşteri: 1,800 TL  │
│                         │
│     [📄 Dokümanlar]    │
│     [ℹ️ Detaylar]      │
│                         │
│ [📝 Mesaj Yaz...]      │
│                         │
│ [🎯 Hızlı Sorular]      │
└─────────────────────────┘
```

---

## 🎯 Intent Classification

### 🤖 AI-Based Intent Classification
```java
// OpenAI ile intent classification
String prompt = """
Aşağıdaki mesajın intent'ini belirle.
Mesaj: "Müşteri kalp krizi geçirdi, poliçe kapsar mı?"

Olabilir intent'ler:
- POLICY_VALIDATION: Poliçe kapsamı sorgulama
- DOCUMENT_RECOMMENDATION: Doküman önerisi
- QUICK_CHECK: Hızlı soru
- POLICY_COMPARISON: Poliçe karşılaştırma
- GREETING: Selamlaşma
- HELP: Yardım talebi

JSON formatında:
{
    "intent": "POLICY_VALIDATION",
    "confidence": 0.9
}
""";
```

### 📋 Rule-Based Intent Classification
```java
// Pattern matching ile intent classification
INTENT_PATTERNS.put("POLICY_VALIDATION", Arrays.asList(
    Pattern.compile("(?i).*poliçe.*kapsar.*"),
    Pattern.compile("(?i).*teminat.*dahil.*"),
    Pattern.compile("(?i).*işlem.*ödeme.*")
));

INTENT_PATTERNS.put("DOCUMENT_RECOMMENDATION", Arrays.asList(
    Pattern.compile("(?i).*doküman.*kontrol.*"),
    Pattern.compile("(?i).*dosya.*gerekli.*"),
    Pattern.compile("(?i).*belge.*istedi.*")
));
```

---

## 🔄 Conversation Flow Examples

### Senaryo 1: Poliçe Validasyon
```
👤 Çalışan: Merhaba
🤖 Chatbot: Merhaba! Size nasıl yardımcı olabilirim?

👤 Çalışan: Müşteri kalp krizi geçirdi, poliçe kapsar mı?
🤖 Chatbot: Poliçe ID verir misiniz?

👤 Çalışan: POL-001
🤖 Chatbot: Yapılan tıbbi işlem nedir?

👤 Çalışan: Kalp krizi tedavisi, stent takıldı
🤖 Chatbot: ✅ POLİÇE KAPSAR
   📊 Ödeme Detayları:
   - Tahmini Ödeme: 12,000 TL
   - Müşteri Ödemesi: 1,800 TL
   
   [📄 Doküman Önerisi] [ℹ️ Detaylı Bilgi]

👤 Çalışan: Doküman önerisi
🤖 Chatbot: 📄 Gerekli Dokümanlar:
   1. Hasta raporu
   2. Fatura
   3. Kimlik fotokopisi
   4. Acil durum belgesi
   
   ⏰ Deadline: 48 saat
```

### Senaryo 2: Doküman Önerisi
```
👤 Çalışan: Hangi dokümanları kontrol etmeliyim?
🤖 Chatbot: Poliçe ID ve işlem bilgisi verir misiniz?

👤 Çalışan: POL-001, check-up
🤖 Chatbot: 📄 Gerekli Dokümanlar:
   1. Randevu formu
   2. Kimlik fotokopisi
   3. Poliçe fotokopisi
   
   ⏰ Deadline: 5 iş günü
```

### Senaryo 3: Hızlı Soru
```
👤 Çalışan: Polisemde check-up var mı?
🤖 Chatbot: 🤖 Evet, bu poliçe check-up teminatını kapsar. Yıllık 1 check-up hakkınız var.
   
   Takip edilebilir sorular:
   - Diğer teminatlar
   - Policy detayları
   - Başka bir soru
```

### Senaryo 4: Policy Comparison
```
👤 Çalışan: İki poliçe karşılaştır
🤖 Chatbot: İlk poliçe ID'si nedir?

👤 Çalışan: POL-001
🤖 Chatbot: İkinci poliçe ID'si nedir?

👤 Çalışan: POL-002
🤖 Chatbot: 🔄 Poliçe Karşılaştırma Sonucu:
   
   Farklar:
   - coverageLimit_diff: -50000.00
   - deductible_diff: 500.00
   - coPayment_diff: 5.0
   
   🚨 Acil Durum İçin Daha İyi: Policy 2
```

---

## 🎨 UI Components

### 1. **Chat Bubble Component**
```javascript
// User message
<div class="chat-bubble user">
  <div class="avatar">👤</div>
  <div class="message">Müşteri kalp krizi geçirdi, poliçe kapsar mı?</div>
</div>

// Bot message
<div class="chat-bubble bot">
  <div class="avatar">🤖</div>
  <div class="message card">
    <div class="card-header">✅ POLİÇE KAPSAR</div>
    <div class="card-body">
      <div class="payment-details">
        <div>Tahmini Ödeme: 12,000 TL</div>
        <div>Müşteri Ödemesi: 1,800 TL</div>
      </div>
    </div>
    <div class="card-actions">
      <button>📄 Doküman Önerisi</button>
      <button>ℹ️ Detaylı Bilgi</button>
    </div>
  </div>
</div>
```

### 2. **Suggested Actions Component**
```javascript
<div class="suggested-actions">
  <button class="action-button" data-action="DOCUMENT_RECOMMENDATION">
    📄 Doküman Önerisi
  </button>
  <button class="action-button" data-action="SHOW_DETAILS">
    ℹ️ Detaylı Bilgi
  </button>
</div>
```

### 3. **Follow-up Questions Component**
```javascript
<div class="follow-up-questions">
  <div class="question-chip" onclick="askQuestion('Gerekli dokümanlar neler?')">
    Gerekli dokümanlar neler?
  </div>
  <div class="question-chip" onclick="askQuestion('Ödeme detayları')">
    Ödeme detayları
  </div>
  <div class="question-chip" onclick="askQuestion('Başka bir işlem kontrol et')">
    Başka bir işlem kontrol et
  </div>
</div>
```

### 4. **Input Component**
```javascript
<div class="chat-input-container">
  <input 
    type="text" 
    placeholder="Mesajınızı buraya yazın..."
    id="messageInput"
  />
  <button id="sendButton">Gönder</button>
  <button id="voiceButton">🎤</button>
  <button id="fileButton">📎</button>
</div>
```

---

## 🔧 Frontend Implementation

### React Component Example
```javascript
import React, { useState, useEffect } from 'react';

const Chatbot = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [conversationId, setConversationId] = useState(null);
  const [isTyping, setIsTyping] = useState(false);

  const startConversation = async () => {
    const response = await fetch('/api/bff/chatbot/conversation/start?userId=user-123');
    const data = await response.json();
    setConversationId(data.conversationId);
    addMessage('bot', data.message);
  };

  const sendMessage = async () => {
    if (!input.trim()) return;

    addMessage('user', input);
    setIsTyping(true);

    const response = await fetch('/api/bff/chatbot/message', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        message: input,
        conversationId: conversationId,
        userId: 'user-123'
      })
    });

    const data = await response.json();
    addMessage('bot', data.message);
    setIsTyping(false);
    setInput('');
  };

  const addMessage = (sender, message) => {
    setMessages([...messages, { sender, message }]);
  };

  const handleAction = (action) => {
    // Handle suggested action clicks
    setInput(`Lütfen ${action} yap`);
    sendMessage();
  };

  useEffect(() => {
    startConversation();
  }, []);

  return (
    <div className="chatbot-container">
      <div className="chat-header">
        <h2>🤖 Sigorta Chatbot</h2>
      </div>
      
      <div className="chat-messages">
        {messages.map((msg, index) => (
          <div key={index} className={`chat-bubble ${msg.sender}`}>
            <div className="avatar">{msg.sender === 'user' ? '👤' : '🤖'}</div>
            <div className="message">{msg.message}</div>
          </div>
        ))}
        {isTyping && <div className="typing-indicator">🤖 Yazıyor...</div>}
      </div>
      
      <div className="chat-input">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Mesajınızı buraya yazın..."
        />
        <button onClick={sendMessage}>Gönder</button>
      </div>
    </div>
  );
};

export default Chatbot;
```

---

## 🎯 Voice Integration

### Speech-to-Text
```javascript
const startVoiceInput = () => {
  const recognition = new webkitSpeechRecognition();
  recognition.lang = 'tr-TR';
  
  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    setInput(transcript);
  };
  
  recognition.start();
};
```

### Text-to-Speech
```javascript
const speakMessage = (message) => {
  const utterance = new SpeechSynthesisUtterance(message);
  utterance.lang = 'tr-TR';
  speechSynthesis.speak(utterance);
};
```

---

## 📊 Analytics ve Monitoring

### Chatbot Metrics
```javascript
// Track conversation metrics
const metrics = {
  totalConversations: 150,
  averageResponseTime: 450, // ms
  intentDistribution: {
    POLICY_VALIDATION: 45,
    DOCUMENT_RECOMMENDATION: 30,
    QUICK_CHECK: 20,
    POLICY_COMPARISON: 5
  },
  userSatisfaction: 4.5, // out of 5
  resolutionRate: 0.85
};
```

---

## 🔒 Security Considerations

### 1. **Authentication**
```javascript
// JWT token validation
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};
```

### 2. **Rate Limiting**
```javascript
// Limit requests per user
const rateLimiter = {
  maxRequests: 100,
  windowMs: 60000, // 1 minute
  currentRequests: 0
};
```

### 3. **Data Privacy**
```javascript
// Mask sensitive data
const maskSensitiveData = (data) => {
  return data.replace(/\d{11}/g, '***********'); // TC kimlik no
};
```

---

## 🚀 Deployment

### Docker Compose
```yaml
services:
  bff-service:
    environment:
      - AI_ENABLED=true
      - CHATBOT_ENABLED=true
      - WEBSOCKET_ENABLED=true
```

### Environment Variables
```bash
AI_ENABLED=true
CHATBOT_ENABLED=true
WEBSOCKET_ENABLED=true
CHATBOT_RATE_LIMIT=100
```

---

## 📚 Mülakat Soruları

### **Q: Chatbot neden AI kullanıyor?**
**A:** 
- Natural language understanding
- Intent classification accuracy
- Better user experience
- Reduced manual work
- Scalable support

### **Q: REST API mi WebSocket mi?**
**A:**
- **REST API:** Tekil mesajlar, simple integration
- **WebSocket:** Real-time chat, better UX
- **Hybrid:** Her ikisi de desteklenir

### **Q: Intent classification nasıl çalışır?**
**A:**
- AI-based: OpenAI ile intent classification
- Rule-based: Pattern matching
- Fallback: Rule-based if AI fails

### **Q: Conversation state nasıl yönetiliyor?**
**A:**
- ConversationContext class
- In-memory storage (Redis in production)
- State tracking: ACTIVE, COMPLETED, EXPIRED
- Message history preservation

---

## 🎓 Sonuç

Chatbot entegrasyonu ile sigorta çalışanları:
- **Doğal Dil:** Türkçe konuşarak iletişim
- **Hızlı:** Saniyeler içinde yanıt
- **Etkili:** AI-powered intent classification
- **User-Friendly:** Intuitive chat interface
- **Multi-Channel:** REST API ve WebSocket

Bu chatbot, çalışanların günlük işlerini kolaylaştırır ve müşteri deneyimini iyileştirir.