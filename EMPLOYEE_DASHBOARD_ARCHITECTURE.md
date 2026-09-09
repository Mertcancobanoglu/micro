# 🎨 Employee Dashboard Architecture Diagrams

## 🎯 Overall System Architecture

```mermaid
graph TB
    A[Employee Dashboard UI] --> B[BFF Service]
    B --> C[EmployeeDashboardController]
    C --> D[EmployeeDashboardService]
    D --> E[Policy Service]
    D --> F[AI Service]
    F --> G[OpenAI API]
    F --> H[Local LLM]
    D --> I[Redis Cache]
    D --> J[Metrics Tracker]
    
    E --> K[PostgreSQL]
    K --> L[Policy Data]
    
    C --> M[Security Layer]
    M --> N[JWT Validation]
    M --> O[Role Check]
    
    D --> P[Cost Management]
    P --> Q[AI Cost Tracker]
```

## 🔄 Policy Validation Flow

```mermaid
sequenceDiagram
    participant E as Employee
    participant D as Dashboard UI
    participant B as BFF Service
    participant S as EmployeeDashboardService
    participant P as Policy Service
    participant A as AI Service
    participant R as Redis Cache
    
    E->>D: Enter customer info & procedure
    D->>B: POST /api/bff/employee/validate-policy
    B->>S: validatePolicy(request)
    S->>R: Check cache
    alt Cache Hit
        R-->>S: Return cached result
        S-->>B: Validation result
        B-->>D: AI analysis
        D-->>E: Display result
    else Cache Miss
        S->>P: Get policy data
        P-->>S: Policy details
        S->>A: Build AI prompt
        A->>G: Call OpenAI API
        G-->>A: AI response
        A-->>S: Analysis result
        S->>R: Cache result
        S-->>B: Validation result
        B-->>D: AI analysis
        D-->>E: Display result
    end
```

## 📄 Document Recommendation Flow

```mermaid
graph LR
    A[Employee Question] --> B[Document Recommendation]
    B --> C{AI Available?}
    C -->|Yes| D[AI Analysis]
    C -->|No| E[Rule-Based]
    D --> F[Document List]
    E --> F
    F --> G[Priority Assignment]
    G --> H[Deadline Calculation]
    H --> I[Document Recommendations]
    I --> J[Display to Employee]
```

## 🎯 Dashboard UI Components

```mermaid
graph TB
    subgraph "Employee Dashboard"
        A[Customer Search]
        B[Policy Validation Panel]
        C[Document Recommendations]
        D[Quick Questions]
        E[Recent Claims]
        F[Policy Comparison]
        G[AI Chat Interface]
        H[Statistics Dashboard]
    end
    
    A --> B
    B --> C
    B --> G
    D --> B
    E --> B
    F --> B
    H --> B
```

## 🔒 Security Layer

```mermaid
graph TB
    A[Employee Request] --> B[JWT Token]
    B --> C[Token Validation]
    C --> D{Valid Token?}
    D -->|No| E[401 Unauthorized]
    D -->|Yes| F[Role Check]
    F --> G{Employee Role?}
    G -->|No| H[403 Forbidden]
    G -->|Yes| I[Rate Limiting]
    I --> J{Within Limit?}
    J -->|No| K[429 Too Many Requests]
    J -->|Yes| L[Request Processing]
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
    J --> K[Cost Report]
```

## 📊 AI vs Rule-Based Decision

```mermaid
graph TB
    A[Validation Request] --> B{AI Enabled?}
    B -->|No| C[Rule-Based Validation]
    B -->|Yes| D{AI Available?}
    D -->|No| C
    D -->|Yes| E[AI-Based Validation]
    E --> F{AI Success?}
    F -->|Yes| G[Return AI Result]
    F -->|No| C
    C --> H[Return Rule-Based Result]
    G --> I[Cache Result]
    H --> I
```

## 🎯 Use Case: Emergency Scenario

```mermaid
sequenceDiagram
    participant E as Employee
    participant D as Dashboard
    participant AI as AI Service
    participant P as Policy Service
    
    E->>D: Customer: "Emergency heart attack"
    D->>AI: Analyze emergency procedure
    AI->>P: Check policy emergency coverage
    P-->>AI: Policy covers emergency
    AI-->>D: "Policy covers, priority processing"
    D->>E: Display: "Covered, 12,000 TL payment"
    E->>D: "What documents needed?"
    D->>AI: Recommend emergency documents
    AI-->>D: "Emergency report, patient report, bill"
    D-->>E: Display document list with priorities
```

## 📱 Mobile vs Desktop UI

```mermaid
graph TB
    subgraph "Mobile Dashboard"
        A1[Customer Search]
        A2[Simple Validation Form]
        A3[Quick AI Result]
        A4[Document List]
    end
    
    subgraph "Desktop Dashboard"
        B1[Advanced Search]
        B2[Comprehensive Validation]
        B3[Detailed AI Analysis]
        B4[Document Management]
        B5[Policy Comparison]
        B6[Statistics]
        B7[Batch Processing]
    end
    
    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4
```

## 🔄 Data Flow: Complete Validasyon

```mermaid
graph TB
    A[Employee Input] --> B[Request DTO]
    B --> C[Controller]
    C --> D[Service Layer]
    D --> E[Policy Data Fetch]
    E --> F[Cache Check]
    F --> G{Cache Hit?}
    G -->|Yes| H[Return Cached]
    G -->|No| I[AI Analysis]
    I --> J{AI Success?}
    J -->|Yes| K[Parse AI Response]
    J -->|No| L[Rule-Based Fallback]
    K --> M[Response Builder]
    L --> M
    M --> N[Cache Result]
    N --> O[Return Response]
    H --> O
    O --> P[UI Display]
```

## 🎨 Component Hierarchy

```mermaid
graph TB
    A[EmployeeDashboardController] --> B[validatePolicy]
    A --> C[recommendDocuments]
    A --> D[quickCheck]
    A --> E[validatePoliciesBatch]
    A --> F[comparePolicies]
    
    B --> G[EmployeeDashboardService]
    C --> G
    D --> G
    E --> G
    F --> G
    
    G --> H[AI-Based Validation]
    G --> I[Rule-Based Validation]
    G --> J[Document Recommendation]
    G --> K[Policy Comparison]
    
    H --> L[OpenAI API]
    I --> M[Validation Rules]
    J --> N[Document Rules]
    K --> O[Comparison Logic]
```

## 📊 Performance Optimization

```mermaid
graph LR
    A[Request] --> B{Cache?}
    B -->|Hit| C[Return Cached]
    B -->|Miss| D{AI Available?}
    D -->|Yes| E[AI Processing]
    D -->|No| F[Rule-Based]
    E --> G[Cache Result]
    F --> G
    G --> H[Return Result]
    C --> I[Response]
    H --> I
```

## 🎯 Error Handling Flow

```mermaid
graph TB
    A[Request] --> B{Success?}
    B -->|Yes| C[Return Success]
    B -->|No| D{Error Type?}
    D -->|Policy Not Found| E[Return Not Found]
    D -->|AI Error| F[Fallback to Rule-Based]
    D -->|Service Error| G[Return Service Unavailable]
    D -->|Validation Error| H[Return Validation Error]
    F --> I{Fallback Success?}
    I -->|Yes| C
    I -->|No| G
```