# Sistema de Notificações Multi-Canal

Sistema distribuído de notificações baseado em arquitetura de microsserviços, implementando controle de cotas, resiliência, event sourcing e observabilidade completa.

## 🎯 Visão Geral

Plataforma de notificações multi-canal projetada para suportar alta carga com controle granular de cotas, auditoria completa e resiliência avançada. O sistema foi desenvolvido aplicando padrões modernos de arquitetura distribuída.

### Características Principais

- **Notificações Multi-Canal**: Suporte para Email, SMS e Push Notifications
- **Controle de Cotas**: Gerenciamento de limites de consumo por cliente e canal
- **Autenticação & Autorização**: API Gateway com JWT e rate limiting
- **Resiliência**: Circuit Breaker, Retry e Fallback configuráveis
- **Event Sourcing**: Auditoria completa de eventos via Kafka
- **Observabilidade**: Tracing distribuído, métricas e dashboards
- **Alta Performance**: Cache Redis e otimizações para ~2000 TPS

## 🏗️ Arquitetura

![](docs\arquitetura\arch.png)

### Fluxo de Comunicação

1. **Cliente** → Autentica via API Gateway (JWT)
2. **API Gateway** → Roteia para Notification Core
3. **Notification Core** → Valida quota disponível (Quota Service)
4. **Notification Core** → Publica evento no Kafka
5. **Providers** → Consomem eventos e enviam notificações
6. **Audit Service** → Persiste eventos para auditoria

## 🚀 Quick Start

### Pré-requisitos

- **Docker** 20+ e **Docker Compose** 2+
- *Opcional:* Java 21 e Maven 3.9+ para desenvolvimento local

### Execução Completa

```bash
# Subir toda a infraestrutura e serviços
docker-compose up -d

# Verificar status dos containers
docker-compose ps

# Acompanhar logs em tempo real
docker-compose logs -f

# Parar todos os serviços
docker-compose down

# Remover volumes (reset completo)
docker-compose down -v
```

### Build e Execução Local (Desenvolvimento)

```bash
# Build de todos os módulos
mvn clean install -DskipTests

# Rodar serviço específico
cd services/api-gateway
mvn spring-boot:run

# Rodar com perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Endpoints Principais

| Serviço | URL | Porta | Health Check |
|---------|-----|-------|--------------|
| API Gateway | http://localhost:8081 | 8081 | /actuator/health |
| Notification Core | http://localhost:8082 | 8082 | /actuator/health |
| Quota Service | http://localhost:8083 | 8083 | /actuator/health |
| Kafka UI | http://localhost:8888 | 8888 | - |
| Prometheus | http://localhost:9090 | 9090 | - |
| Grafana | http://localhost:3000 | 3000 | - |
| WireMock (Mocks) | http://localhost:8080 | 8080 | /__admin |

### Testando a API

```bash
# 1. Autenticação (obter JWT)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"clientId":"demo-client","password":"demo123"}'

# Resposta: { "token": "eyJhbGc...", "expiresIn": 3600 }

# 2. Criar notificação (usando o token)
curl -X POST http://localhost:8081/api/notifications \
  -H "Authorization: Bearer {SEU_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "demo-client",
    "channel": "SMS",
    "recipient": "+5511999999999",
    "message": "Teste de notificação"
  }'

# 3. Consultar quota
curl -X GET http://localhost:8081/api/quotas/demo-client/SMS \
  -H "Authorization: Bearer {SEU_TOKEN}"
```

## 📦 Estrutura do Projeto

```
notificacao-pos/
├── services/                      # Microsserviços
│   ├── api-gateway/              # Gateway de autenticação e roteamento
│   ├── notification-core/        # Orquestrador de notificações
│   ├── quota-service/            # Gerenciamento de cotas
│   ├── provider-push/            # Consumer para Push Notifications
│   ├── provider-email/           # Consumer para Email
│   ├── provider-sms/             # Consumer para SMS
│   └── audit-service/            # Event Sourcing e Auditoria
├── infrastructure/
│   ├── docker/                   # Configurações de containers
│   ├── grafana/                  # Dashboards e provisioning
│   ├── prometheus/               # Configuração de métricas
│   ├── kafka/                    # Init scripts para tópicos
│   └── wiremock/                 # Mocks de provedores externos
├── docs/
│   ├── arquitetura/              # Diagramas e documentação
│   ├── postman/                  # Collections de API
│   └── sprint-reports/           # Evidências de sprints
├── scripts/
│   ├── performance-test.js       # Testes de carga (K6)
│   └── docker-start.sh           # Scripts auxiliares
├── docker-compose.yml            # Orquestração completa
└── pom.xml                       # Parent POM (multi-module)
```

## 🛠️ Stack Tecnológica

### Backend & Frameworks
- **Java 21** - LTS com Virtual Threads
- **Spring Boot 3.5.7** - Framework base
- **Spring Cloud Gateway** - API Gateway reativo
- **Spring Security** - Autenticação JWT
- **Spring Data JPA** - Persistência
- **Spring Kafka** - Integração com Kafka

### Infraestrutura & Persistência
- **PostgreSQL 15** - Banco transacional (notificações, quotas, eventos)
- **MongoDB 7** - Armazenamento de templates
- **Redis 7** - Cache distribuído e rate limiting
- **Apache Kafka 3.x** - Mensageria assíncrona
- **Zookeeper** - Coordenação do Kafka

### Resiliência & Observabilidade
- **Resilience4j** - Circuit Breaker, Retry, Rate Limiter, Bulkhead
- **Micrometer** - Métricas da aplicação
- **Prometheus** - Coleta de métricas
- **Grafana** - Dashboards e visualização
- **OpenTelemetry** - Tracing distribuído (preparado)

### DevOps & Testes
- **Docker & Docker Compose** - Containerização
- **Flyway** - Migrações de banco de dados
- **WireMock 3** - Mocks de APIs externas
- **K6** - Testes de performance
- **JUnit 5 & Mockito** - Testes unitários

## 🧪 Testes de Performance

O projeto inclui scripts de teste de carga com K6:

```bash
# Instalar K6 (Linux/macOS)
curl https://github.com/grafana/k6/releases/download/v0.47.0/k6-v0.47.0-linux-amd64.tar.gz -L | tar xvz

# Executar teste de carga
cd scripts
k6 run performance-test.js

# Teste customizado
k6 run --vus 100 --duration 120s performance-test.js
```

**Configuração do Teste:**
- Ramp-up progressivo: 0 → 200 VUs
- Duração total: ~6 minutos
- Throughput esperado: **~2000 TPS** no pico

## 📊 Observabilidade

### Grafana Dashboards

Acesse: **http://localhost:3000**
- **Usuário:** `admin`
- **Senha:** `admin123`

**Dashboards Disponíveis:**
- Business Metrics (notificações por canal, taxa de sucesso)
- Technical Metrics (latência, throughput, erros)
- Circuit Breaker Status

### Prometheus

Acesse: **http://localhost:9090**

**Métricas Disponíveis:**
- `notification_sent_total` - Total de notificações enviadas
- `notification_failed_total` - Total de falhas
- `http_server_requests_seconds` - Métricas HTTP

## 🔒 Configurações de Segurança

### Credenciais Padrão (Ambiente de Desenvolvimento)

⚠️ **ATENÇÃO:** Alterar em produção!

| Serviço | Usuário | Senha | Database |
|---------|---------|-------|----------|
| PostgreSQL | `notification` | `notification123` | `notification_db` |
| MongoDB | `notification` | `notification123` | `notification_templates` |
| Grafana | `admin` | `admin123` | - |
| API (Demo) | `demo-client` | `demo123` | - |

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 3600  # 1 hora
```

## 🎓 Contexto Acadêmico

**Projeto Aplicado - TCC**
- **Curso:** Pós-Graduação em Arquitetura de Software e Soluções
- **Instituição:** XP Educação
- **Autor:** Thiago Vieira
- **Período:** Março 2025 - Dezembro 2026

### Padrões e Conceitos Implementados

- **Microsserviços** - Arquitetura distribuída com serviços independentes
- **API Gateway Pattern** - Ponto único de entrada com autenticação
- **Event Sourcing** - Auditoria completa via eventos imutáveis
- **CQRS** - Separação de comandos e consultas (preparado)
- **Circuit Breaker** - Prevenção de falhas em cascata
- **Cache-Aside** - Otimização de leitura com Redis
- **Retry Pattern** - Resiliência em comunicações
- **Bulkhead Pattern** - Isolamento de recursos
- **Observability** - Logs, Métricas e Tracing

## 📚 Documentação Adicional

- [Arquitetura Detalhada](docs/arquitetura/componentes.md)
- [API Gateway](services/api-gateway/README.md)
- [Quota Service](services/quota-service/README.md)
- [Testes de Performance](scripts/README-PERFORMANCE.md)
- [Sprint Reports](docs/sprint-reports/)

## 📄 Licença

Projeto acadêmico desenvolvido para fins educacionais.

---

**Desenvolvido como Projeto Aplicado - Pós-Graduação XP Educação**
