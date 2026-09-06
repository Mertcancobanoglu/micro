# Sağlık Sigortası Sistemi - Mimari Dokümantasyonu

## 🏗️ Genel Mimari

Bu sistem, sağlık sigortası hasar yönetimi için microservices mimarisi kullanır. 3 ana microservice, bir BFF katmanı ve çeşitli altyapı servislerinden oluşur.

### 📊 Servis Mimarisi

```
┌─────────────────┐
│   Frontend      │
│   (Web/Mobile)  │
└────────┬────────┘
         │
         │ HTTP/REST
         │
┌────────▼────────┐
│  BFF Service    │ ← Port 8080
│  (API Gateway)  │
└────────┬────────┘
         │
         │ HTTP/REST
         │
    ┌────┴────┬────────────┐
    │         │            │
┌───▼────┐ ┌─▼────────┐ ┌─▼──────────┐
│ Claim  │ │ Policy   │ │ External  │
│Service │ │ Service  │ │ Services  │
│Port 8081│ │Port 8082 │ │           │
└───┬────┘ └─┬────────┘ └───────────┘
    │         │
    │         │
    │    ┌────┴────┐
    │    │         │
┌───▼─▼──┐ ┌─▼────────┐
│ Kafka  │ │  Redis   │
│Events  │ │  Cache   │
└───┬────┘ └─┬────────┘
    │         │
┌───▼─────────▼─────┐
│   PostgreSQL      │
│   Database        │
└───────────────────┘
```

## 🎯 Servis Detayları

### 1. BFF Service (Backend for Frontend)
**Port: 8080**

**Sınıflar ve Görevleri:**

#### `BffServiceApplication.java`
- Ana uygulama giriş noktası
- Spring Boot application başlatıcı
- BFF servisini konfigüre eder

#### `SecurityConfig.java`
- Spring Security konfigürasyonu
- JWT tabanlı stateless authentication
- Role-based access control (RBAC)
- Endpoint bazlı yetkilendirme kuralları

#### `AuthController.java`
- Authentication endpoint'leri
- Login işlemi ve JWT token oluşturma
- Token validasyonu
- Rol belirleme (ADMIN, USER, POLICY_MANAGER, CLAIMS_PROCESSOR)

#### `BffClaimController.java`
- Claim service proxy'i
- İstekleri claim-service'e yönlendirir
- Hata yönetimi ve graceful degradation
- API gateway işlevselliği

#### `BffPolicyController.java`
- Policy service proxy'i
- İstekleri policy-service'e yönlendirir
- Response aggregation
- Service soyutlama

#### `JwtUtil.java`
- JWT token oluşturma ve doğrulama
- Token'dan bilgi çıkarma
- Token süresi kontrolü
- HMAC SHA256 imzalama

#### `JwtAuthenticationFilter.java`
- JWT token filtresi
- Her request'te token doğrulama
- Security context'e kullanıcı bilgisi ekleme
- Spring Security entegrasyonu

#### `OpenApiConfig.java`
- Swagger/OpenAPI dokümantasyonu
- API endpoint'lerini dokümante eder
- JWT authentication konfigürasyonu
- Interactive API testing arayüzü

#### `RedisConfig.java`
- Redis caching konfigürasyonu
- Cache TTL ayarları
- JSON serializer konfigürasyonu
- Cache null değerleri kontrolü

#### `RestTemplateConfig.java`
- RestTemplate bean konfigürasyonu
- HTTP client ayarları
- Service-to-service communication

---

### 2. Claim Service (Hasar Yönetim Servisi)
**Port: 8081**

**Sınıflar ve Görevleri:**

#### `ClaimServiceApplication.java`
- Ana uygulama giriş noktası
- Hasar yönetim servisini başlatır
- Saga pattern konfigürasyonu

#### `Claim.java` (Entity)
- Hasar veri modeli
- Veritabanı tablo yapısı
- Hasar tipleri enum'ı (INPATIENT, OUTPATIENT, EMERGENCY, vb.)
- Hasar durumları enum'ı (DRAFT, SUBMITTED, APPROVED, REJECTED, vb.)
- Audit trail (createdAt, updatedAt)
- Optimistic locking (version)

#### `ClaimRepository.java`
- Veritabanı erişim katmanı
- Custom query metodları
- JPQL sorguları
- Index'li alanlar için optimizasyon

#### `ClaimService.java`
- İş mantığı katmanı
- CRUD operasyonları
- Redis caching entegrasyonu
- Saga orchestration başlatma
- Transaction yönetimi

#### `ClaimController.java`
- REST API endpoint'leri
- Swagger dokümantasyonu
- HTTP metodları (GET, POST, PUT, PATCH)
- İstatistik endpoint'i

#### `ClaimSagaOrchestrator.java`
- Saga pattern orchestrator
- Hasar işleme sürecini koordine eder
- Kafka event'leri gönderir
- Policy verification sonuçlarını işler
- Ödeme hesaplama (muafiyet - katılım payı)

#### `ClaimSubmittedEvent.java`
- Saga event'i
- Hasar gönderildi bilgisini taşır
- Policy service'e gönderilir

#### `PolicyVerifiedEvent.java`
- Saga event'i
- Policy doğrulama sonucunu taşır
- Policy service'den gelir

#### `ClaimEvent.java`
- Saga event'i
- Hasar durumu değişikliklerini taşır
- Diğer servislerle paylaşılır

#### `KafkaConfig.java`
- Kafka producer/consumer konfigürasyonu
- Event-driven communication
- Saga pattern için mesajlaşma

#### `RedisConfig.java`
- Redis caching konfigürasyonu
- Performans optimizasyonu
- TTL ayarları

#### `SecurityConfig.java`
- Spring Security konfigürasyonu
- JWT authentication
- Role-based yetkilendirme

#### `JwtUtil.java`
- JWT token işlemleri
- Token oluşturma ve doğrulama

#### `JwtAuthenticationFilter.java`
- JWT token filtresi
- Request authentication

#### `OpenApiConfig.java`
- Swagger/OpenAPI dokümantasyonu

#### `JpaConfig.java`
- JPA auditing konfigürasyonu
- Audit trail otomasyonu

---

### 3. Policy Service (Poliçe Yönetim Servisi)
**Port: 8082**

**Sınıflar ve Görevleri:**

#### `PolicyServiceApplication.java`
- Ana uygulama giriş noktası
- Poliçe yönetim servisini başlatır
- Saga participant konfigürasyonu

#### `Policy.java` (Entity)
- Poliçe veri modeli
- Elementer sağlık sigortası tipleri
- Poliçe durumları (ACTIVE, SUSPENDED, CANCELLED, vb.)
- Finansal bilgiler (prim, limit, muafiyat, katılım payı)
- Audit trail ve optimistic locking

#### `PolicyRepository.java`
- Veritabanı erişim katmanı
- TC no bazlı sorgular
- Aktif poliçe sorguları
- Süresi dolacak poliçe sorguları

#### `PolicyService.java`
- İş mantığı katmanı
- CRUD operasyonları
- Redis caching entegrasyonu
- Policy yaşam döngüsü yönetimi

#### `PolicyController.java`
- REST API endpoint'leri
- Swagger dokümantasyonu
- İstatistik endpoint'i

#### `PolicySagaHandler.java`
- Saga pattern participant
- Claim service'den gelen event'leri işler
- Policy validasyonu yapar
- Doğrulama sonucunu gönderir

#### `ClaimSubmittedEvent.java`
- Saga event'i
- Claim service'den gelen hasar bilgisi

#### `PolicyVerifiedEvent.java`
- Saga event'i
- Policy doğrulama sonucu
- Claim service'e gönderilir

#### Diğer Konfigürasyon Sınıfları
- `KafkaConfig.java`, `RedisConfig.java`, `SecurityConfig.java`, `JwtUtil.java`, `JwtAuthenticationFilter.java`, `OpenApiConfig.java`, `JpaConfig.java` (Claim service ile aynı)

---

## 🔄 Saga Pattern Akışı

### Hasar İşleme Süreci

1. **Hasar Oluşturma**
   ```
   Frontend → BFF → Claim Service
   Claim created with status: SUBMITTED
   ```

2. **Saga Başlatma**
   ```
   Claim Saga Orchestrator
   ↓
   ClaimSubmittedEvent → Kafka (claim.submitted topic)
   ```

3. **Policy Doğrulama**
   ```
   Policy Saga Handler (Kafka listener)
   ↓
   Policy validation:
   - Status check (ACTIVE)
   - Date validation
   - TC number match
   - Coverage limit check
   ↓
   PolicyVerifiedEvent → Kafka (policy.verified topic)
   ```

4. **Hasar Kararı**
   ```
   Claim Saga Orchestrator (Kafka listener)
   ↓
   IF policy.valid:
     Calculate payment amount
     Update claim status: APPROVED
     Send ClaimApprovedEvent
   ELSE:
     Update claim status: REJECTED
     Send ClaimRejectedEvent
   ```

5. **Sonuç**
   ```
   Claim status updated in database
   Cache invalidated
   Notification sent
   ```

### Ödeme Hesaplama Formülü

```
Onaylanan Tutar = Talep Edilen - Muafiyet - Katılım Payı
Katılım Payı = Talep Edilen × (Katılım Yüzdesi / 100)
```

## 🚀 Data Flow Diagram

### Hasar Oluşturma Akışı

```
1. POST /api/bff/claims
   ↓
2. BFF → Claim Service (POST /api/claims)
   ↓
3. Claim Service:
   - Generate claim number
   - Set status: SUBMITTED
   - Save to database
   - Invalidate cache
   ↓
4. Start Saga:
   - Send ClaimSubmittedEvent to Kafka
   ↓
5. Policy Service receives event
   - Validate policy
   - Send PolicyVerifiedEvent to Kafka
   ↓
6. Claim Service receives response
   - Calculate payment amount
   - Update claim status
   - Send result event
   ↓
7. Response to BFF → Frontend
```

## 🗄️ Veritabanı Şeması

### Claims Tablosu
```sql
- id (UUID, PK)
- claim_number (String, UNIQUE)
- policy_id (UUID, FK)
- insured_person_name (String)
- insured_person_tc_no (String)
- claim_type (Enum)
- status (Enum)
- incident_date (DateTime)
- report_date (DateTime)
- hospital_name (String)
- diagnosis (String)
- claimed_amount (Decimal)
- approved_amount (Decimal)
- rejected_amount (Decimal)
- description (Text)
- reported_by (String)
- assigned_to (String)
- created_at (DateTime)
- updated_at (DateTime)
- version (Long)
```

### Policies Tablosu
```sql
- id (UUID, PK)
- policy_number (String, UNIQUE)
- insured_person_name (String)
- insured_person_tc_no (String)
- birth_date (Date)
- gender (String)
- address (String)
- phone (String)
- email (String)
- policy_type (Enum)
- status (Enum)
- start_date (Date)
- end_date (Date)
- annual_premium (Decimal)
- coverage_limit (Decimal)
- deductible (Decimal)
- co_payment_percentage (Integer)
- coverage_details (Text)
- exclusions (Text)
- beneficiary_name (String)
- beneficiary_tc_no (String)
- agent_id (String)
- created_at (DateTime)
- updated_at (DateTime)
- version (Long)
```

## 🔐 Güvenlik Mimarisi

### JWT Authentication Flow

```
1. Login Request
   POST /api/auth/login
   {username, password}
   ↓
2. BFF generates JWT token
   - Claims: {username, role, exp, iat}
   - Signature: HMAC SHA256
   ↓
3. Token returned to client
   ↓
4. Client includes token in headers
   Authorization: Bearer <token>
   ↓
5. JwtAuthenticationFilter validates token
   - Extract username and role
   - Check expiration
   - Set security context
   ↓
6. Request proceeds to controller
   - Role-based authorization check
   - Business logic execution
```

### Roller ve Yetkiler

| Rol | Yetkiler |
|-----|----------|
| ADMIN | Tüm endpoint'lere erişim, sistem yönetimi |
| POLICY_MANAGER | Poliçe işlemleri, poliçe oluşturma/güncelleme |
| CLAIMS_PROCESSOR | Hasar işlemleri, hasar inceleme/onaylama |
| USER | Kendi hasarlarını görüntüleme, hasar oluşturma |

## 📊 Caching Stratejisi

### Redis Cache Yapısı

```
Claim Service Cache:
- claims:{id} → 1 saat TTL
- claims:{claimNumber} → 1 saat TTL
- policyClaims:{policyId} → 1 saat TTL
- userClaims:{username} → 1 saat TTL
- claimStatistics:statusCount → 1 saat TTL

Policy Service Cache:
- policies:{id} → 2 saat TTL
- policies:{policyNumber} → 2 saat TTL
- personPolicies:{tcNo} → 2 saat TTL
- activePolicies:{date} → 2 saat TTL
- expiringPolicies:{dateRange} → 2 saat TTL
- policyStatistics:statusCount → 2 saat TTL

BFF Service Cache:
- API responses → 30 dakika TTL
- Authentication tokens → 24 saat TTL
```

### Cache Invalidation

- **Write Operations**: Tüm cache'ler temizlenir (@CacheEvict)
- **Read Operations**: Cache'den okuma (@Cacheable)
- **Update Operations**: İlgili cache'ler temizlenir

## 🧪 Test Stratejisi

### Unit Tests
- Service layer metodları
- Repository metodları
- Utility metodları
- Mock kullanımı (Mockito)

### Integration Tests
- Controller endpoint'leri
- Database operasyonları
- Kafka event'leri
- Redis caching
- Testcontainers kullanımı

### Test Coverage
- Service layer: >80%
- Controller layer: >70%
- Repository layer: >90%

## 🚀 Deployment Mimarisi

### Docker Containerization

Her service için ayrı Docker container:
- Multi-stage build (build + runtime)
- Minimal image size (Alpine)
- Environment variable konfigürasyonu
- Health check endpoint'leri

### Docker Compose Orchestration

```
Infrastructure:
- PostgreSQL (Database)
- Redis (Cache)
- Kafka + Zookeeper (Messaging)

Services:
- claim-service (Port 8081)
- policy-service (Port 8082)
- bff-service (Port 8080)
```

### CI/CD Pipeline

GitHub Actions workflow:
1. Build & Test
2. Security Scan (Trivy)
3. Docker Build & Push
4. Integration Tests
5. Deployment (production only)

## 📈 Monitoring & Logging

### Actuator Endpoints
- `/actuator/health` - Servis sağlık durumu
- `/actuator/metrics` - Prometheus metrics
- `/actuator/info` - Servis bilgileri

### Logging Strategy
- Console output (development)
- File logging (production)
- Structured logging (JSON format)
- Log levels: DEBUG, INFO, WARN, ERROR

### Metrics
- Request counts
- Response times
- Error rates
- Cache hit/miss ratios
- Kafka message throughput

---

Bu mimari, modern microservices prensiplerine uygun olarak tasarlanmıştır ve üretim ortamında ölçeklenebilir, güvenli ve bakımı kolay bir sistem sağlar.