# Diagramas de Sequência - Quota Service

## 1. Validar e Consumir Quota (Sucesso)
```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant QS as Quota Service
    participant Redis
    participant DB as PostgreSQL

    Client->>Gateway: POST /api/quotas/validate
    Note over Client,Gateway: {clientId, channel, amount}
    
    Gateway->>Gateway: Valida JWT
    Gateway->>QS: Forward request
    
    QS->>Redis: GET quota:clientId:channel
    
    alt Cache HIT
        Redis-->>QS: availableQuota = 995
        QS->>QS: Valida: 995 >= 5 ✓
        QS->>Redis: DECR quota:clientId:channel 5
        Redis-->>QS: 990
        
        QS->>DB: UPDATE quotas (com lock)
        Note over QS,DB: used += 5, available -= 5
        DB-->>QS: OK
        
        QS->>DB: INSERT quota_usage
        Note over QS,DB: Auditoria
        DB-->>QS: OK
        
    else Cache MISS
        Redis-->>QS: null
        QS->>DB: SELECT ... FOR UPDATE
        Note over QS,DB: Lock pessimista
        DB-->>QS: Quota {available: 1000}
        
        QS->>QS: Valida: 1000 >= 5 ✓
        QS->>DB: UPDATE quotas
        DB-->>QS: OK
        
        QS->>DB: INSERT quota_usage
        DB-->>QS: OK
        
        QS->>Redis: SET quota:clientId:channel 995
        Redis-->>QS: OK
    end
    
    QS-->>Gateway: 200 OK {allowed: true, available: 990}
    Gateway-->>Client: 200 OK
```

---

## 2. Validar e Consumir Quota (Quota Excedida)
```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant QS as Quota Service
    participant Redis
    participant DB as PostgreSQL

    Client->>Gateway: POST /api/quotas/validate
    Note over Client,Gateway: {amount: 10000}
    
    Gateway->>Gateway: Valida JWT
    Gateway->>QS: Forward request
    
    QS->>Redis: GET quota:clientId:channel
    Redis-->>QS: availableQuota = 50
    
    QS->>QS: Valida: 50 >= 10000 ✗
    Note over QS: Quota insuficiente!
    
    QS-->>Gateway: 429 Too Many Requests
    Note over QS,Gateway: {allowed: false, reason: "Insufficient quota"}
    
    Gateway-->>Client: 429 Too Many Requests
```

---

## 3. Consultar Quotas
```mermaid
sequenceDiagram
    participant Client
    participant Gateway as API Gateway
    participant QS as Quota Service
    participant DB as PostgreSQL

    Client->>Gateway: GET /api/quotas/demo-client
    Note over Client,Gateway: Header: Authorization Bearer {token}
    
    Gateway->>Gateway: Valida JWT
    Gateway->>QS: Forward request
    
    QS->>DB: SELECT * FROM quotas WHERE client_id = ?
    DB-->>QS: List<Quota>
    
    QS->>QS: Converte para DTO
    Note over QS: Calcula usagePercentage
    
    QS-->>Gateway: 200 OK [QuotaResponse]
    Gateway-->>Client: 200 OK
```

---

## 4. Liberar Quota (Rollback)
```mermaid
sequenceDiagram
    participant NS as Notification Service
    participant Kafka
    participant QS as Quota Service
    participant Redis
    participant DB as PostgreSQL

    NS->>Kafka: Publica evento
    Note over NS,Kafka: NotificationFailed
    
    Kafka->>QS: Consume evento
    Note over Kafka,QS: {clientId, channel, amount}
    
    QS->>DB: SELECT ... FOR UPDATE
    DB-->>QS: Quota
    
    QS->>QS: Libera quota
    Note over QS: used -= amount<br/>available += amount
    
    QS->>DB: UPDATE quotas
    DB-->>QS: OK
    
    QS->>DB: INSERT quota_usage
    Note over QS,DB: operation: RELEASE
    DB-->>QS: OK
    
    QS->>Redis: INCR quota:clientId:channel amount
    Redis-->>QS: OK
    
    QS->>Kafka: ACK
```

---

## 5. Criar Nova Quota (Admin)
```mermaid
sequenceDiagram
    participant Admin
    participant Gateway as API Gateway
    participant QS as Quota Service
    participant DB as PostgreSQL
    participant Redis

    Admin->>Gateway: POST /admin/quotas
    Note over Admin,Gateway: {clientId, channel, totalQuota}
    
    Gateway->>Gateway: Valida JWT + Role Admin
    Gateway->>QS: Forward request
    
    QS->>DB: SELECT COUNT(*) WHERE client_id AND channel
    DB-->>QS: 0 (não existe)
    
    QS->>QS: Cria entidade Quota
    Note over QS: used=0, available=total
    
    QS->>DB: INSERT INTO quotas
    DB-->>QS: Quota (id: 5)
    
    QS->>Redis: SET quota:clientId:channel totalQuota
    Redis-->>QS: OK
    
    QS-->>Gateway: 201 Created
    Gateway-->>Admin: 201 Created
```

---

## 6. Reset de Quota
```mermaid
sequenceDiagram
    participant Admin
    participant Gateway as API Gateway
    participant QS as Quota Service
    participant DB as PostgreSQL
    participant Redis

    Admin->>Gateway: POST /admin/quotas/clientId/EMAIL/reset
    
    Gateway->>Gateway: Valida JWT
    Gateway->>QS: Forward request
    
    QS->>DB: SELECT ... FOR UPDATE
    DB-->>QS: Quota {total: 1000, used: 750}
    
    QS->>QS: Reset
    Note over QS: used = 0<br/>available = total
    
    QS->>DB: UPDATE quotas
    DB-->>QS: OK
    
    QS->>Redis: SET quota:clientId:EMAIL 1000
    Redis-->>QS: OK
    
    QS-->>Gateway: 200 OK
    Gateway-->>Admin: 200 OK
```

---

## Legenda

- **Cache HIT**: Quota encontrada no Redis (rápido)
- **Cache MISS**: Busca no PostgreSQL (mais lento)
- **Lock Pessimista**: `SELECT ... FOR UPDATE` previne race conditions
- **Auditoria**: Registro em `quota_usage` para histórico