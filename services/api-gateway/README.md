# API Gateway

Gateway centralizado com autenticação JWT, roteamento e resiliência.

## 🚀 Quick Start
```bash
# Local
mvn spring-boot:run

# Docker
docker-compose up -d api-gateway
```

Acesse: `http://localhost:8081`

## 🔐 Autenticação
```bash
# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"clientId":"demo-client","password":"demo123"}'

# Resposta
{
  "success": true,
  "token": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}

# Usar token
curl http://localhost:8081/api/quotas/demo-client \
  -H "Authorization: Bearer {token}"
```

## 📡 Endpoints

### Autenticação
- `POST /api/auth/login` - Obter token JWT
- `POST /api/auth/validate` - Validar token

### Monitoramento
- `GET /actuator/health` - Status do serviço
- `GET /actuator/circuitbreakers` - Estado dos circuit breakers
- `GET /actuator/prometheus` - Métricas

## 🛣️ Rotas

| Path | Destino |
|------|---------|
| `/api/quotas/**` | Quota Service |
| `/api/notifications/**` | Notification Core |

## ⚙️ Configuração

### Variáveis de Ambiente
```bash
SERVER_PORT=8081
JWT_SECRET=your-secret-key
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

### Circuit Breaker

- Threshold: 50% de falhas
- Window: 10 requisições
- Wait (Open): 30 segundos

## 🛠️ Stack

- Spring Cloud Gateway
- Spring Security + JWT
- Resilience4j
- Redis
- Java 21

## 📦 Build
```bash
mvn clean package
docker build -t api-gateway .
```