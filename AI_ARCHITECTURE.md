# 🤖 AI Integration Architecture Diagrams

## 🎯 AI Integration Flow

```mermaid
graph TB
    A[Frontend Application] --> B[BFF Service]
    B --> C[Claim Service]
    C --> D[AI Service]
    D --> E[OpenAI API]
    D --> F[Claude API]
    D --> G[Local LLM]
    D --> H[Azure OpenAI]
    
    D --> I[Redis Cache]
    C --> I
    B --> I
    
    D --> J[Metrics & Cost Tracking]
    J --> K[Monitoring Dashboard]
```

## 🔄 AI Service Components

```mermaid
graph LR
    A[AIService Interface] --> B[OpenAI Implementation]
    A --> C[Claude Implementation]
    A --> D[Local LLM Implementation]
    A --> E[Mock Implementation]
    
    B --> F[Claim Analysis]
    B --> G[Fraud Detection]
    B --> H[Payment Estimation]
    B --> I[ICD-10 Coding]
    B --> J[Customer Support]
    
    F --> K[ClaimController]
    G --> K
    H --> K
    I --> K
    J --> L[BFF Controller]
```

## 📊 AI Request Flow

```mermaid
sequenceDiagram
    participant F as Frontend
    participant B as BFF Service
    participant C as Claim Service
    participant A as AI Service
    participant R as Redis Cache
    participant O as OpenAI API
    
    F->>B: POST /api/bff/claims/{id}/ai-analysis
    B->>R: Check cache
    alt Cache Hit
        R-->>B: Return cached result
        B-->>F: Analysis result
    else Cache Miss
        B->>C: GET /api/claims/{id}
        C-->>B: Claim data
        B->>A: analyzeClaimDescription()
        A->>O: Generate analysis
        O-->>A: AI response
        A-->>B: Analysis result
        B->>R: Cache result
        B-->>F: Analysis result
    end
```

## 🔒 Security Layers

```mermaid
graph TB
    A[Request] --> B[API Gateway]
    B --> C[JWT Validation]
    C --> D[Rate Limiting]
    D --> E[Content Filtering]
    E --> F[PII Masking]
    F --> G[AI Service]
    G --> H[External API]
    
    H --> I[Response]
    I --> J[Cost Tracking]
    J --> K[Logging]
    K --> L[Cache Storage]
```

## 💰 Cost Management Flow

```mermaid
graph LR
    A[AI Request] --> B[Token Counter]
    B --> C[Cost Calculator]
    C --> D[Budget Check]
    D --> E{Within Budget?}
    E -->|Yes| F[Process Request]
    E -->|No| G[Fallback / Reject]
    F --> H[Update Costs]
    H --> I[Store Metrics]
    I --> J[Monitoring Alert]
```

## 🚀 Deployment Architecture

```mermaid
graph TB
    subgraph "Development"
        A[Local LLM<br/>Llama/Mistral]
        B[Mock AI Service]
    end
    
    subgraph "Staging"
        C[OpenAI API<br/>GPT-3.5]
        D[Redis Cache]
    end
    
    subgraph "Production"
        E[Azure OpenAI<br/>GPT-4]
        F[Dedicated GPU Cluster<br/>for sensitive data]
        G[Cost Management]
        H[Monitoring]
    end
    
    A --> I[Claim Service]
    B --> I
    C --> I
    E --> I
    F --> I
    
    I --> D
    I --> G
    I --> H
```

## 🎯 AI Use Case Mapping

```mermaid
graph TB
    A[Claim Management] --> B[Description Analysis]
    A --> C[Fraud Detection]
    A --> D[Payment Estimation]
    
    E[Policy Management] --> F[ICD-10 Coding]
    E --> G[Risk Assessment]
    
    H[Customer Support] --> I[Chatbot]
    H --> J[Q&A System]
    
    B --> K[OpenAI GPT-4]
    C --> K
    D --> K
    
    F --> L[Claude 3]
    G --> L
    
    I --> M[Local LLM]
    J --> M
```

## 📊 Performance Optimization

```mermaid
graph LR
    A[AI Request] --> B{Cache Hit?}
    B -->|Yes| C[Return Cached]
    B -->|No| D{AI Available?}
    D -->|Yes| E[AI Processing]
    D -->|No| F[Fallback Logic]
    E --> G[Cache Result]
    F --> G
    G --> H[Return Result]
    C --> H
```

## 🔧 Service Dependencies

```mermaid
graph TB
    A[Claim Service] --> B[AIService]
    A --> C[Redis]
    A --> D[PostgreSQL]
    
    B --> E[OpenAI SDK]
    B --> F[LangChain4j]
    B --> G[Spring AI]
    
    E --> H[OpenAI API]
    F --> H
    G --> H
    
    B --> I[Cost Tracker]
    B --> J[Rate Limiter]
    B --> K[Circuit Breaker]
    
    C --> L[Cache Manager]
    K --> L
```

## 🎯 Data Flow: Claim with AI

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as BFF
    participant C as Claim Service
    participant A as AI Service
    participant R as Redis
    participant O as OpenAI
    
    U->>F: Create claim with description
    F->>B: POST /api/bff/claims
    B->>C: POST /api/claims
    C->>C: Save claim to DB
    C-->>B: Claim created
    B-->>F: Claim ID
    
    F->>B: POST /api/bff/claims/{id}/ai-analysis
    B->>R: Check cache
    R-->>B: Cache miss
    B->>C: GET /api/claims/{id}
    C-->>B: Claim data
    B->>A: analyzeClaimDescription()
    A->>O: Generate analysis
    O-->>A: Analysis result
    A-->>B: Analysis
    B->>R: Cache result
    B-->>F: Analysis result
    F-->>U: Display AI insights
    
    F->>B: POST /api/bff/claims/{id}/fraud-check
    B->>A: detectFraud()
    A->>O: Fraud analysis
    O-->>A: Risk assessment
    A-->>B: Fraud risk
    B-->>F: Risk level
    F-->>U: Display risk alert
    
    F->>B: POST /api/bff/claims/{id}/estimate-payment
    B->>A: estimatePayment()
    A->>O: Payment calculation
    O-->>A: Payment estimate
    A-->>B: Payment estimate
    B-->>F: Estimated amount
    F-->>U: Display payment prediction
```