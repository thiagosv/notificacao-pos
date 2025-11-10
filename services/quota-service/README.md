# Quota Service

Gerenciamento e controle de cotas de notificações por cliente e canal.

## 🚀 Quick Start
```bash
# Local
mvn spring-boot:run

# Docker
docker-compose up -d quota-service
```

Acesse: `http://localhost:8083`

## 📊 Funcionalidades

- ✅ Gerenciamento de cotas por cliente e canal
- ✅ Validação e consumo em tempo real
- ✅ Cache Redis para alta performance
- ✅ Auditoria completa de uso
- ✅ Lock pessimista (previne race conditions)

## 🔌 API Endpoints

### Consulta de Cotas
```bash
# Listar todas as cotas de um cliente
GET /quotas/{clientId}

# Obter quota específica
GET /quotas/{clientId}/{channel}
```

### Validação e Consumo
```bash
# Validar e consumir quota
POST /quotas/validate
{
  "clientId": "demo-client",
  "channel": "EMAIL",
  "amount": 5,
  "notificationId": "notif-001"
}

# Resposta (sucesso)
{
  "allowed": true,
  "availableQuota": 995,
  "requestedAmount": 5
}

# Resposta (quota excedida - HTTP 429)
{
  "allowed": false,
  "availableQuota": 10,
  "requestedAmount": 100,
  "reason": "Insufficient quota"
}
```

### Verificação (sem consumir)
```bash
POST /quotas/check
{
  "clientId": "demo-client",
  "channel": "EMAIL",
  "amount": 5
}
```

### Liberação de Quota
```bash
POST /quotas/release?clientId=demo-client&channel=EMAIL&amount=5
```

### Administração
```bash
# Criar quota
POST /admin/quotas
{
  "clientId": "new-client",
  "channel": "EMAIL",
  "totalQuota": 1000
}

# Atualizar limite
PUT /admin/quotas/{clientId}/{channel}?newTotalQuota=2000

# Resetar uso
POST /admin/quotas/{clientId}/{channel}/reset

# Deletar quota
DELETE /admin/quotas/{clientId}/{channel}

# Quotas próximas do limite
GET /admin/quotas/near-limit?threshold=100
```

## 🔄 Fluxo de Validação
```
1. Request chega → Verifica cache Redis
2. Cache HIT → Decrementa Redis + PostgreSQL
3. Cache MISS → Busca PostgreSQL com lock
4. Valida disponibilidade
5. Consome quota (usado += amount)
6. Atualiza cache
7. Registra auditoria
8. Retorna resposta
```

## 💾 Cache Redis

**Estratégia:** Cache-Aside com TTL de 1 hora
```bash
# Chaves Redis
quota:demo-client:EMAIL → 995
quota:demo-client:SMS → 500

# Verificar cache
docker exec -it notification-redis redis-cli
> GET quota:demo-client:EMAIL
"995"
```

## 📡 Monitoramento
```bash
# Health check
GET /actuator/health

# Métricas
GET /actuator/prometheus
```

## ⚙️ Configuração

### Variáveis de Ambiente
```bash
SERVER_PORT: 8080
REDIS_HOST: redis
REDIS_PORT: 6379
POSTGRESQL_HOST: postgres
POSTGRESQL_USER: notification
POSTGRESQL_PASS: notification123
KAFKA_BOOTSTRAP: kafka:9092
```

### Quotas Padrão (inseridas via Flyway)

| Cliente | Canal | Total | Disponível |
|---------|-------|-------|------------|
| demo-client | EMAIL | 1000 | 1000 |
| demo-client | SMS | 500 | 500 |
| demo-client | PUSH | 2000 | 2000 |
| demo-client | WHATSAPP | 300 | 300 |

## 🧪 Testes
```bash
# Via API Gateway (com auth)
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"clientId":"demo-client","password":"demo123"}' | jq -r '.token')

# Consultar quotas
curl http://localhost:8081/api/quotas/demo-client \
  -H "Authorization: Bearer $TOKEN" | jq .

# Validar e consumir
curl -X POST http://localhost:8081/api/quotas/validate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "demo-client",
    "channel": "EMAIL",
    "amount": 5
  }' | jq .
```

## 🛠️ Stack

- Spring Boot 3.5.7
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- Kafka (preparação)
- Java 21

## 📦 Build
```bash
mvn clean package
docker build -t quota-service .
```