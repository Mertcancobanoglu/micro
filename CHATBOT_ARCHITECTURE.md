# 🎨 Chatbot Architecture Diagrams

## 🎯 Overall Chatbot Architecture

```mermaid
graph TB
    A[Employee User] --> B[Chatbot UI]
    B --> C[REST API / WebSocket]
    C --> D[ChatbotController]
    D --> E[ChatbotService]
    E --> F[Intent Classification]
    E --> G[Conversation Management]
    E --> H[EmployeeDashboardService]
    
    F --> I{AI Available?}
    I -->|Yes| J[OpenAI API]
    I -->|No| K[Rule-Based]
    
    H --> L[Policy Service]
    H --> M[AI Service]
    
    G --> N[Conversation Storage]
    N --> O[Redis / Memory]
    
    E --> P[Response Builder]
    P --> Q[ChatResponse]
    Q --> C
    C --> B
    B --> A
```

## 🔄 Chatbot Message Flow

```mermaid
sequenceDiagram
    participant E as Employee
    participant UI as Chatbot UI
    participant C as ChatbotController
    participant S as ChatbotService
    participant I as Intent Classifier
    participant ED as EmployeeDashboardService
    participant AI as AI Service
    
    E->>UI: "Müşteri kalp krizi geçirdi, poliçe kapsar mı?"
    UI->>C: POST /api/bff/chatbot/message
    C->>S: processMessage(request)
    S->>S: Get or create conversation context
    S->>I: classifyIntent(message)
    I->>AI: OpenAI API call
    AI-->>I: Intent: POLICY_VALIDATION
    I-->>S: Intent result
    S->>S: routeIntent()
    S->>ED: validatePolicy(request)
    ED-->>S: Validation result
    S->>S: buildResponse()
    S-->>C: ChatResponse
    C-->>UI: JSON response
    UI-->>E: Display AI response
```

## 🎯 Intent Classification Flow

```mermaid
graph TB
    A[User Message] --> B{AI Enabled?}
    B -->|Yes| C[AI-Based Classification]
    B -->|No| D[Rule-Based Classification]
    
    C --> E[OpenAI API]
    E --> F[Intent + Confidence]
    
    D --> G[Pattern Matching]
    G --> H[Intent + Confidence]
    
    F --> I[Intent Router]
    H --> I
    
    I --> J{Intent Type}
    J -->|POLICY_VALIDATION| K[Policy Validation Handler]
    J -->|DOCUMENT_RECOMMENDATION| L[Document Recommendation Handler]
    J -->|QUICK_CHECK| M[Quick Check Handler]
    J -->|POLICY_COMPARISON| N[Policy Comparison Handler]
    J -->|GREETING| O[Greeting Handler]
    J -->|HELP| P[Help Handler]
    J -->|UNKNOWN| Q[Unknown Handler]
```

## 📊 Conversation State Management

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: Start Conversation
    ACTIVE --> AWAITING_INFO: Missing Info
    AWAITING_INFO --> ACTIVE: Info Provided
    ACTIVE --> COMPLETED: Task Complete
    ACTIVE --> EXPIRED: Timeout
    COMPLETED --> [*]
    EXPIRED --> [*]
    
    note right of ACTIVE
        Current Intent
        Collected Info
        Message History
    end note
    
    note right of AWAITING_INFO
        Policy ID Missing
        Medical Procedure Missing
        Other Info Missing
    end note
```

## 🎨 Chatbot UI Components

```mermaid
graph TB
    subgraph "Chatbot UI"
        A[Chat Header]
        B[Message History]
        C[Input Area]
        D[Suggested Actions]
        E[Follow-up Questions]
        F[Voice Input]
        G[File Upload]
    end
    
    B --> H[User Messages]
    B --> I[Bot Messages]
    I --> J[Text Response]
    I --> K[Card Response]
    I --> L[Action Buttons]
    
    D --> M[Policy Validation]
    D --> N[Document Recommendation]
    D --> O[Quick Check]
    
    E --> P[Related Questions]
    E --> Q[Next Steps]
```

## 🔄 Multi-Step Conversation Flow

```mermaid
sequenceDiagram
    participant E as Employee
    participant C as Chatbot
    participant S as ChatbotService
    participant CT as ConversationContext
    
    E->>C: "Müşteri kalp krizi geçirdi"
    C->>S: processMessage()
    S->>CT: Get context
    CT-->>S: Empty context
    S->>S: Intent: POLICY_VALIDATION
    S->>CT: Check collected info
    CT-->>S: Policy ID missing
    S-->>C: "Poliçe ID verir misiniz?"
    C-->>E: "Poliçe ID verir misiniz?"
    
    E->>C: "POL-001"
    C->>S: processMessage()
    S->>CT: Add policy ID
    S->>CT: Check collected info
    CT-->>S: Medical procedure missing
    S-->>C: "Yapılan tıbbi işlem nedir?"
    C-->>E: "Yapılan tıbbi işlem nedir?"
    
    E->>C: "Kalp krizi tedavisi"
    C->>S: processMessage()
    S->>CT: Add medical procedure
    S->>CT: All info collected
    S->>S: Execute validation
    S-->>C: Validation result
    C-->>E: "✅ POLİÇE KAPSAR"
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
    J -->|Yes| L[PII Masking]
    L --> M[Request Processing]
```

## 📊 Response Types

```mermaid
graph TB
    A[Chatbot Service] --> B{Response Type}
    B -->|TEXT| C[Simple Text Response]
    B -->|CARD| D[Card Response with Actions]
    B -->|INPUT| E[Input Request]
    B -->|ERROR| F[Error Response]
    
    C --> G[Display as Text]
    D --> H[Display as Card]
    E --> I[Display Input Field]
    F --> J[Display Error Message]
    
    D --> K[Suggested Actions]
    K --> L[Buttons]
    K --> M[Links]
    K --> N[Custom Actions]
```

## 🎯 WebSocket vs REST API

```mermaid
graph LR
    subgraph "REST API"
        A1[HTTP POST]
        A2[Request-Response]
        A3[Stateless]
        A4[Simple Integration]
    end
    
    subgraph "WebSocket"
        B1[WS Connection]
        B2[Real-time]
        B3[Stateful]
        B4[Better UX]
    end
    
    A1 --> C[Chatbot Service]
    B1 --> C
    
    A2 --> D[Single Response]
    B2 --> E[Streaming Responses]
    
    A3 --> F[No Connection State]
    B3 --> G[Persistent Connection]
    
    A4 --> H[Easy Implementation]
    B4 --> I[Complex Implementation]
```

## 🎨 Frontend Architecture

```mermaid
graph TB
    subgraph "Frontend Application"
        A[React/Vue Component]
        B[Chat UI Component]
        C[Message List Component]
        D[Input Component]
        E[Action Button Component]
    end
    
    A --> B
    B --> C
    B --> D
    B --> E
    
    A --> F[HTTP Client]
    A --> G[WebSocket Client]
    
    F --> H[REST API]
    G --> I[WebSocket API]
    
    H --> J[Chatbot Service]
    I --> J
```

## 📊 Analytics Flow

```mermaid
graph LR
    A[Chatbot Service] --> B[Metrics Collector]
    B --> C[Conversation Metrics]
    B --> D[Intent Distribution]
    B --> E[Response Time]
    B --> F[User Satisfaction]
    
    C --> G[Dashboard]
    D --> G
    E --> G
    F --> G
    
    G --> H[Admin Interface]
    G --> I[Reports]
```

## 🔄 Error Handling Flow

```mermaid
graph TB
    A[Request] --> B{Success?}
    B -->|Yes| C[Return Success]
    B -->|No| D{Error Type?}
    D -->|AI Error| E[Fallback to Rule-Based]
    D -->|Service Error| F[Return Service Unavailable]
    D -->|Validation Error| G[Return Validation Error]
    D -->|Network Error| H[Retry Logic]
    
    E --> I{Fallback Success?}
    I -->|Yes| C
    I -->|No| F
    
    H --> J{Retry Count?}
    J -->|< 3| K[Retry Request]
    J -->|>= 3| F
    K --> A
```

## 🎯 Multi-Channel Architecture

```mermaid
graph TB
    subgraph "Channels"
        A[Web Chat]
        B[Mobile App]
        C[Desktop App]
        D[WhatsApp Integration]
    end
    
    A --> E[Chatbot Service]
    B --> E
    C --> E
    D --> E
    
    E --> F[REST API]
    E --> G[WebSocket]
    
    F --> H[Chatbot Controller]
    G --> H
    
    H --> I[Chatbot Service]
    I --> J[Employee Dashboard Service]
```