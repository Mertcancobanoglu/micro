# 🔒 Null Safety Guidelines - Java 21 Best Practices

## 🎯 Amaç

NullPointerException (NPE) riskini minimize etmek ve modern Java özelliklerini kullanarak daha güvenli kod yazmak.

---

## 📋 Null Safety Kuralları

### 1. **Objects.isNull() Kullanımı**

#### ❌ Kötü
```java
if (request.getConversationId() == null) {
    request.setConversationId(UUID.randomUUID().toString());
}
```

#### ✅ İyi
```java
if (Objects.isNull(request.getConversationId())) {
    request.setConversationId(UUID.randomUUID().toString());
}
```

**Avantajları:**
- Statik import ile daha okunabilir
- Null-safe comparison
- Modern Java standardı

---

### 2. **Objects.nonNull() Kullanımı**

#### ❌ Kötü
```java
if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    // ...
}
```

#### ✅ İyi
```java
if (Objects.nonNull(username) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
    // ...
}
```

**Avantajları:**
- Tutarlı API
- Null-safe negation
- Better readability

---

### 3. **Optional Kullanımı**

#### ❌ Kötü
```java
String policyId = request.getPolicyId();
if (policyId != null) {
    // işlemler
}
```

#### ✅ İyi
```java
Optional.ofNullable(request.getPolicyId())
    .ifPresent(policyId -> {
        // işlemler
    });
```

**Avantajları:**
- Explicit null handling
- Functional style
- Better API contract

---

### 4. **Optional.map() ve orElse()**

#### ❌ Kötü
```java
String policyId = request.getPolicyId();
if (policyId != null) {
    return policyService.getPolicy(policyId);
} else {
    return "default-policy";
}
```

#### ✅ İyi
```java
return Optional.ofNullable(request.getPolicyId())
    .map(policyService::getPolicy)
    .orElse("default-policy");
```

**Avantajları:**
- Chainable operations
- Null-safe chaining
- Better error handling

---

### 5. **Optional.orElseThrow()**

#### ❌ Kötü
```java
String policyId = request.getPolicyId();
if (policyId == null) {
    throw new IllegalArgumentException("Policy ID required");
}
```

#### ✅ İyi
```java
String policyId = Optional.ofNullable(request.getPolicyId())
    .orElseThrow(() -> new IllegalArgumentException("Policy ID required"));
```

**Avantajları:**
- Concise error handling
- Lazy exception creation
- Better API design

---

### 6. **Optional.filter()**

#### ❌ Kötü
```java
String policyId = request.getPolicyId();
if (policyId != null && policyId.startsWith("POL-")) {
    // işlemler
}
```

#### ✅ İyi
```java
Optional.ofNullable(request.getPolicyId())
    .filter(id -> id.startsWith("POL-"))
    .ifPresent(id -> {
        // işlemler
    });
```

**Avantajları:**
- Conditional processing
- Chainable filters
- Null-safe conditionals

---

### 7. **Java 21 Pattern Matching**

#### ❌ Kötü
```java
Object obj = request.getValue();
if (obj instanceof String) {
    String str = (String) obj;
    // işlemler
}
```

#### ✅ İyi
```java
Object obj = request.getValue();
if (obj instanceof String str) {
    // str otomatik olarak String tipinde
}
```

**Avantajları:**
- Eliminates casting
- Type-safe
- Concise syntax

---

### 8. **Java 21 Switch Pattern Matching**

#### ❌ Kötü
```java
String status = claim.getStatus();
if (status != null) {
    switch (status) {
        case "APPROVED":
            // işlemler
            break;
        case "REJECTED":
            // işlemler
            break;
        default:
            // işlemler
    }
}
```

#### ✅ İyi
```java
String status = claim.getStatus();
switch (status) {
    case "APPROVED" -> // işlemler
    case "REJECTED" -> // işlemler
    case null -> // null handling
    default -> // işlemler
};
```

**Avantajları:**
- Null-safe switch
- Concise syntax
- Better readability

---

### 9. **@NonNull Annotation**

#### ❌ Kötü
```java
public void processClaim(String claimId) {
    if (claimId == null) {
        throw new IllegalArgumentException("Claim ID required");
    }
    // işlemler
}
```

#### ✅ İyi
```java
public void processClaim(@NonNull String claimId) {
    // @NonNull annotation ile null kontrolü otomatik
    // işlemler
}
```

**Avantajları:**
- Compile-time checking
- Documentation
- IDE support

---

### 10. **Null-safe DTO'lar**

#### ❌ Kötü
```java
@Data
public class PolicyValidationRequest {
    private String policyId;
    private String tcKimlikNo;
    
    public String getPolicyId() {
        return policyId; // null dönebilir
    }
}
```

#### ✅ İyi
```java
@Data
@Builder
public class PolicyValidationRequest {
    @Builder.Default
    private String policyId = "";
    
    @Builder.Default
    private String tcKimlikNo = "";
    
    public Optional<String> getPolicyId() {
        return Optional.ofNullable(policyId);
    }
}
```

**Avantajları:**
- Default values
- Optional getters
- Null-safe API

---

## 🎯 Projemizde Uygulanan Değişiklikler

### 1. **Objects.isNull() Kullanımı**
```java
// ChatbotController.java
if (Objects.isNull(request.getConversationId())) {
    request.setConversationId(UUID.randomUUID().toString());
}

// ChatbotService.java
if (Objects.isNull(policyId) && Objects.isNull(tcKimlikNo)) {
    return askForPolicyId(request, context);
}

// EmployeeDashboardService.java
if (Objects.isNull(policyData)) {
    return createNotFoundResponse();
}
```

### 2. **Objects.nonNull() Kullanımı**
```java
// JwtAuthenticationFilter.java (3 servis)
if (Objects.nonNull(username) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
    if (jwtUtil.validateToken(jwt, username)) {
        // işlemler
    }
}
```

### 3. **AuthController.java**
```java
if (Objects.isNull(username) || Objects.isNull(password)) {
    return ResponseEntity.badRequest().body("Username and password are required");
}

if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
    return ResponseEntity.badRequest().body("Invalid token format");
}
```

### 4. **ConversationContext.java**
```java
public void addCollectedInfo(String key, Object value) {
    if (Objects.isNull(this.collectedInfo)) {
        this.collectedInfo = new HashMap<>();
    }
    this.collectedInfo.put(key, value);
    this.lastActivity = LocalDateTime.now();
}
```

---

## 📊 Kapsanan Dosyalar

1. **ChatbotController.java** - 2 değişiklik
2. **ChatbotService.java** - 10 değişiklik
3. **ConversationContext.java** - 1 değişiklik
4. **EmployeeDashboardService.java** - 4 değişiklik
5. **AuthController.java** - 2 değişiklik
6. **JwtAuthenticationFilter.java** (BFF) - 1 değişiklik
7. **JwtAuthenticationFilter.java** (Claim) - 1 değişiklik
8. **JwtAuthenticationFilter.java** (Policy) - 1 değişiklik

**Toplam:** 22 `== null` kontrolü `Objects.isNull()` veya `Objects.nonNull()` ile değiştirildi.

---

## 🚀 İleri Seviye Null Safety

### 1. **Null-safe Navigation**
```java
// Java 21 preview feature
String hospitalName = request.getHospital()?.getName();
```

### 2. **Record Pattern Matching**
```java
if (obj instanceof ClaimClaim(String claimNumber, BigDecimal amount)) {
    // Claim record pattern matching
}
```

### 3. **Guarded Pattern Matching**
```java
if (obj instanceof ClaimClaim c && c.getAmount().compareTo(BigDecimal.ZERO) > 0) {
    // Guarded pattern
}
```

---

## 📚 Best Practices Summary

1. **Use Objects.isNull()** for null checks
2. **Use Objects.nonNull()** for non-null checks
3. **Use Optional** for explicit null handling
4. **Use Java 21 pattern matching** for type-safe operations
5. **Use @NonNull** for parameter validation
6. **Use default values** in DTOs
7. **Avoid null returns** where possible
8. **Document null behavior** in Javadoc
9. **Test null scenarios** in unit tests
10. **Use IDE warnings** for null safety

Bu değişikliklerle projemiz çok daha güvenli ve modern Java standartlarına uygun hale geldi!

1. **Use Objects.isNull()** for null checks
2. **Use Objects.nonNull()** for non-null checks
3. **Use Optional** for explicit null handling
4. **Use Java 21 pattern matching** for type-safe operations
5. **Use @NonNull** for parameter validation
6. **Use default values** in DTOs
7. **Avoid null returns** where possible
8. **Document null behavior** in Javadoc
9. **Test null scenarios** in unit tests
10. **Use IDE warnings** for null safety

Bu değişikliklerle projemiz çok daha güvenli ve modern Java standartlarına uygun hale geldi!