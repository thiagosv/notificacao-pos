# Diagrama de Componentes - Sistema de Notificações

## Visão Geral do Sistema
```mermaid
C4Component
    title Diagrama de Componentes - Sistema de Notificações

    Container_Boundary(gateway, "API Gateway") {
        Component(auth, "Authentication", "Spring Security", "Autenticação JWT")
        Component(routing, "Router", "Spring Cloud Gateway", "Roteamento dinâmico")
        Component(circuitbreaker, "Circuit Breaker", "Resilience4j", "Resiliência")
        Component(ratelimit, "Rate Limiter", "Resilience4j", "Controle de taxa")
    }

    Container_Boundary(quota, "Quota Service") {
        Component(quotaapi, "Quota API", "Spring MVC", "REST endpoints")
        Component(quotaservice, "Quota Service", "Spring Service", "Lógica de negócio")
        Component(quotavalidation, "Validation Service", "Spring Service", "Validação e consumo")
        Component(quotacache, "Cache Repository", "Spring Data Redis", "Cache de cotas")
        Component(quotarepo, "Quota Repository", "Spring Data JPA", "Persistência")
    }

    Container_Boundary(notification, "Notification Core") {
        Component(notifapi, "Notification API", "Spring MVC", "REST endpoints")
        Component(orchestrator, "Orchestrator", "Spring Service", "Orquestração")
        Component(idempotency, "Idempotency", "Spring Service", "Controle de duplicatas")
        Component(eventpublisher, "Event Publisher", "Spring Kafka", "Publicação de eventos")
    }

    ContainerDb(postgres, "PostgreSQL", "Database", "Dados transacionais")
    ContainerDb(redis, "Redis", "Cache", "Cache e rate limiting")
    ContainerQueue(kafka, "Kafka", "Message Broker", "Eventos assíncronos")

    Rel(auth, routing, "Valida token")
    Rel(routing, circuitbreaker, "Protege chamadas")
    Rel(routing, quotaapi, "Roteia", "HTTP")
    Rel(routing, notifapi, "Roteia", "HTTP")
    
    Rel(quotaapi, quotaservice, "Usa")
    Rel(quotaapi, quotavalidation, "Usa")
    Rel(quotaservice, quotarepo, "Persiste")
    Rel(quotavalidation, quotacache, "Consulta cache")
    Rel(quotavalidation, quotarepo, "Persiste")
    Rel(quotacache, redis, "Lê/Escreve")
    Rel(quotarepo, postgres, "Lê/Escreve")
    
    Rel(notifapi, orchestrator, "Usa")
    Rel(orchestrator, idempotency, "Verifica duplicata")
    Rel(orchestrator, quotaapi, "Valida quota", "HTTP")
    Rel(orchestrator, eventpublisher, "Publica evento")
    Rel(eventpublisher, kafka, "Produz mensagem")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="2")
```

---

## API Gateway - Componentes Detalhados
```mermaid
graph TB
    subgraph "API Gateway - Port 8081"
        direction TB
        
        subgraph "Security Layer"
            JwtFilter[JWT Authentication Filter]
            JwtProvider[JWT Token Provider]
            SecurityConfig[Security Configuration]
        end
        
        subgraph "Routing Layer"
            GatewayRouter[Gateway Router]
            RouteLocator[Route Locator]
            Predicates[Route Predicates]
        end
        
        subgraph "Resilience Layer"
            CB[Circuit Breaker]
            Retry[Retry Handler]
            RateLimiter[Rate Limiter]
            Fallback[Fallback Controller]
        end
        
        subgraph "Observability"
            Metrics[Metrics Collector]
            Tracing[Tracing Filter]
            Logging[Request Logger]
        end
        
        Request[HTTP Request] --> JwtFilter
        JwtFilter --> JwtProvider
        JwtProvider --> GatewayRouter
        GatewayRouter --> RouteLocator
        RouteLocator --> Predicates
        Predicates --> CB
        CB --> Retry
        Retry --> RateLimiter
        RateLimiter --> TargetService[Target Service]
        CB -.Fallback.-> Fallback
        
        GatewayRouter --> Metrics
        GatewayRouter --> Tracing
        GatewayRouter --> Logging
    end
    
    TargetService --> QuotaService[Quota Service]
    TargetService --> NotificationService[Notification Service]
    
    JwtProvider -.uses.-> RedisCache[(Redis Cache)]
    RateLimiter -.uses.-> RedisCache
    
    style JwtFilter fill:#e1f5ff
    style CB fill:#ffe1e1
    style Fallback fill:#fff4e1
    style Metrics fill:#e1ffe1
```

---

## Quota Service - Componentes Detalhados
```mermaid
graph TB
    subgraph "Quota Service - Port 8083"
        direction TB
        
        subgraph "API Layer"
            QuotaController[Quota Controller]
            AdminController[Admin Controller]
            ExceptionHandler[Exception Handler]
        end
        
        subgraph "Service Layer"
            QuotaService[Quota Service]
            ValidationService[Validation Service]
            EventHandler[Event Handler]
        end
        
        subgraph "Repository Layer"
            QuotaRepo[Quota Repository<br/>JPA]
            UsageRepo[Usage Repository<br/>JPA]
            CacheRepo[Cache Repository<br/>Redis]
        end
        
        subgraph "Domain Model"
            Quota[Quota Entity]
            QuotaUsage[Quota Usage Entity]
            Channel[Channel Enum]
        end
        
        Request[HTTP Request] --> QuotaController
        Request --> AdminController
        
        QuotaController --> QuotaService
        QuotaController --> ValidationService
        AdminController --> QuotaService
        
        ValidationService --> CacheRepo
        ValidationService --> QuotaRepo
        ValidationService --> UsageRepo
        
        QuotaService --> QuotaRepo
        QuotaService --> CacheRepo
        
        QuotaRepo --> Quota
        UsageRepo --> QuotaUsage
        Quota --> Channel
        
        QuotaRepo -.persists.-> PostgreSQL[(PostgreSQL)]
        UsageRepo -.persists.-> PostgreSQL
        CacheRepo -.caches.-> RedisDB[(Redis)]
        
        KafkaConsumer[Kafka Consumer] --> EventHandler
        EventHandler --> ValidationService
    end
    
    style QuotaController fill:#e1f5ff
    style ValidationService fill:#ffe1e1
    style CacheRepo fill:#fff4e1
    style PostgreSQL fill:#e1ffe1
    style RedisDB fill:#ffe1f5
```

---

## Fluxo de Validação de Quota (Componentes)
```mermaid
sequenceDiagram
    autonumber
    
    box API Gateway
        participant Router as Gateway Router
        participant CB as Circuit Breaker
        participant RL as Rate Limiter
    end
    
    box Quota Service
        participant API as Quota Controller
        participant Validation as Validation Service
        participant Cache as Cache Repository
        participant Repo as Quota Repository
    end
    
    box Infrastructure
        participant Redis
        participant Postgres
    end
    
    Router->>CB: Route request
    CB->>RL: Check limits
    RL->>API: POST /quotas/validate
    
    API->>Validation: validateAndConsume(request)
    
    Validation->>Cache: getAvailableQuota(clientId, channel)
    Cache->>Redis: GET quota:clientId:channel
    
    alt Cache HIT
        Redis-->>Cache: 995
        Cache-->>Validation: Optional(995)
        Validation->>Validation: Validate: 995 >= 5 ✓
        Validation->>Cache: decrementQuota(5)
        Cache->>Redis: DECR 5
        Redis-->>Cache: 990
        Validation->>Repo: consumeQuotaFromDatabase(lock=true)
        Repo->>Postgres: UPDATE quotas SET used += 5
        Postgres-->>Repo: OK
    else Cache MISS
        Redis-->>Cache: null
        Cache-->>Validation: Optional.empty()
        Validation->>Repo: findByClientIdAndChannelWithLock()
        Repo->>Postgres: SELECT ... FOR UPDATE
        Postgres-->>Repo: Quota{available: 1000}
        Validation->>Validation: Validate: 1000 >= 5 ✓
        Validation->>Repo: save(quota)
        Repo->>Postgres: UPDATE quotas
        Postgres-->>Repo: OK
        Validation->>Cache: saveAvailableQuota(995)
        Cache->>Redis: SET quota:clientId:channel 995
        Redis-->>Cache: OK
    end
    
    Validation-->>API: QuotaValidationResponse{allowed: true}
    API-->>RL: 200 OK
    RL-->>CB: Response
    CB-->>Router: Response
```

---

## Componentes por Camada

### 1. Presentation Layer (API)
```mermaid
graph LR
    subgraph "Controllers"
        AC[Auth Controller]
        QC[Quota Controller]
        ADC[Admin Controller]
        NC[Notification Controller]
        FC[Fallback Controller]
    end
    
    subgraph "DTOs"
        AuthReq[Auth Request/Response]
        QuotaReq[Quota Request/Response]
        NotifReq[Notification Request/Response]
        ErrorResp[Error Response]
    end
    
    AC --> AuthReq
    QC --> QuotaReq
    ADC --> QuotaReq
    NC --> NotifReq
    AC --> ErrorResp
    QC --> ErrorResp
    
    style AC fill:#e1f5ff
    style QC fill:#e1f5ff
    style ADC fill:#ffe1e1
```

---

### 2. Service Layer (Business Logic)
```mermaid
graph TB
    subgraph "Services"
        AS[Authentication Service]
        QS[Quota Service]
        VS[Validation Service]
        OS[Orchestrator Service]
        IS[Idempotency Service]
        EH[Event Handler]
    end
    
    subgraph "Domain Logic"
        QV[Quota Validator]
        QC[Quota Calculator]
        NR[Notification Router]
        IR[Idempotency Registry]
    end
    
    AS --> JwtProvider[JWT Provider]
    QS --> QV
    VS --> QV
    VS --> QC
    OS --> NR
    IS --> IR
    
    style AS fill:#e1f5ff
    style QS fill:#ffe1e1
    style VS fill:#ffe1e1
    style OS fill:#fff4e1
```

---

### 3. Data Layer (Persistence)
```mermaid
graph TB
    subgraph "Repositories"
        QR[Quota Repository]
        UR[Usage Repository]
        CR[Cache Repository]
        NR[Notification Repository]
        ER[Event Repository]
    end
    
    subgraph "Entities"
        QE[Quota Entity]
        UE[Usage Entity]
        NE[Notification Entity]
        EE[Event Entity]
    end
    
    subgraph "Databases"
        PG[(PostgreSQL)]
        RD[(Redis)]
        MG[(MongoDB)]
        KF[(Kafka)]
    end
    
    QR --> QE
    UR --> UE
    NR --> NE
    ER --> EE
    
    QR --> PG
    UR --> PG
    CR --> RD
    NR --> PG
    ER --> MG
    
    EventPublisher[Event Publisher] --> KF
    
    style QR fill:#e1ffe1
    style CR fill:#ffe1f5
    style PG fill:#e1ffe1
    style RD fill:#ffe1f5
```

---

## Infraestrutura de Observabilidade
```mermaid
graph TB
    subgraph "Services"
        GW[API Gateway]
        QS[Quota Service]
        NS[Notification Service]
    end
    
    subgraph "Observability Stack"
        Metrics[Metrics Collector<br/>Micrometer]
        Tracing[Tracing<br/>OpenTelemetry]
        Logging[Logging<br/>SLF4J + Logback]
    end
    
    subgraph "Monitoring Tools"
        Prom[Prometheus]
        Graf[Grafana]
        Jaeger[Jaeger/Tempo]
        ELK[ELK Stack]
    end
    
    GW --> Metrics
    GW --> Tracing
    GW --> Logging
    
    QS --> Metrics
    QS --> Tracing
    QS --> Logging
    
    NS --> Metrics
    NS --> Tracing
    NS --> Logging
    
    Metrics --> Prom
    Prom --> Graf
    
    Tracing --> Jaeger
    
    Logging --> ELK
    
    style Metrics fill:#e1f5ff
    style Tracing fill:#ffe1e1
    style Logging fill:#fff4e1
    style Prom fill:#e1ffe1
```

---

## Componentes de Resiliência
```mermaid
graph TB
    subgraph "Resilience Components"
        direction LR
        
        CB[Circuit Breaker<br/>Resilience4j]
        RT[Retry<br/>Resilience4j]
        RL[Rate Limiter<br/>Resilience4j]
        BH[Bulkhead<br/>Resilience4j]
        TL[Time Limiter<br/>Resilience4j]
        FB[Fallback<br/>Handler]
    end
    
    subgraph "States & Metrics"
        CBS[CB States:<br/>CLOSED, OPEN, HALF_OPEN]
        Metrics[Metrics:<br/>Success Rate<br/>Failure Rate<br/>Slow Call Rate]
    end
    
    Request[Request] --> CB
    CB --> RT
    RT --> RL
    RL --> BH
    BH --> TL
    TL --> Service[Downstream Service]
    
    CB -.monitors.-> CBS
    CB -.emits.-> Metrics
    
    CB -.failure.-> FB
    FB --> FallbackResp[Fallback Response]
    
    style CB fill:#ffe1e1
    style FB fill:#fff4e1
    style Metrics fill:#e1ffe1
```

---

## Tecnologias por Componente

| Componente | Tecnologia | Versão | Propósito |
|------------|------------|--------|-----------|
| **API Gateway** | Spring Cloud Gateway | 4.1.6 | Roteamento reativo |
| **Autenticação** | JJWT | 0.12.6 | JWT tokens |
| **Circuit Breaker** | Resilience4j | 2.2.0 | Resiliência |
| **Quota Service** | Spring Boot | 3.5.7 | Microsserviço |
| **ORM** | Hibernate | 6.6.4 | Persistência |
| **Cache** | Redis | 7.4.5 | Cache distribuído |
| **Database** | PostgreSQL | 15 | Banco transacional |
| **Migrations** | Flyway | 10.23.2 | Versionamento schema |
| **Mensageria** | Kafka | 3.x | Eventos assíncronos |
| **Métricas** | Micrometer | 1.15.x | Observabilidade |
| **Tracing** | OpenTelemetry | 1.x | Rastreamento distribuído |

---

## Padrões de Design Aplicados
```mermaid
mindmap
  root((Padrões))
    Arquiteturais
      API Gateway
      Circuit Breaker
      Service Mesh Ready
      Event Sourcing Prep
      CQRS Prep
    
    Design
      Repository
      Factory
      Strategy
      Observer
      Singleton
    
    Integração
      Cache Aside
      Retry Pattern
      Bulkhead
      Rate Limiting
      Idempotency
    
    Dados
      Unit of Work
      Lazy Loading
      Pessimistic Lock
      Optimistic Lock Prep
```

---

## Dependências entre Componentes
```mermaid
graph LR
    subgraph "External"
        Client[Cliente]
    end
    
    subgraph "Gateway Layer"
        GW[API Gateway]
    end
    
    subgraph "Service Layer"
        QS[Quota Service]
        NS[Notification Service]
        PS[Provider Services]
    end
    
    subgraph "Data Layer"
        PG[(PostgreSQL)]
        RD[(Redis)]
        KF[(Kafka)]
    end
    
    Client -->|HTTPS + JWT| GW
    GW -->|HTTP| QS
    GW -->|HTTP| NS
    NS -->|HTTP| QS
    NS -->|HTTP| PS
    
    QS -->|JDBC| PG
    QS -->|TCP| RD
    NS -->|JDBC| PG
    NS -->|TCP| KF
    PS -->|TCP| KF
    
    style GW fill:#e1f5ff
    style QS fill:#ffe1e1
    style NS fill:#fff4e1
    style PG fill:#e1ffe1
    style RD fill:#ffe1f5
```