# Sistema de Notificações Multi-Canal

Sistema distribuído de notificações baseado em microsserviços com controle de cotas, resiliência e observabilidade completa.

## 🎯 Visão Geral

Sistema desenvolvido como Projeto Aplicado da Pós-Graduação em Arquitetura de Software e Soluções (XP Educação), demonstrando aplicação prática de padrões modernos de arquitetura distribuída.

### Funcionalidades Principais

- ✅ **Notificações Multi-Canal**: Email, SMS, Push, WhatsApp
- ✅ **Controle de Cotas**: Gerenciamento de limites por cliente e canal
- ✅ **Autenticação JWT**: Gateway centralizado com segurança
- ✅ **Resiliência**: Circuit Breaker, Fallback, Retry
- ✅ **Observabilidade**: Tracing distribuído, métricas, logs
- ✅ **Event Sourcing**: Auditoria completa com Kafka
- ✅ **CQRS**: Separação de comandos e consultas
- ✅ **Cache**: Redis para alta performance

## 🏗️ Arquitetura
```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ HTTPS + JWT
       ▼
┌────────────────────────────────────────┐
│       API Gateway (8081)               │
│  • Autenticação JWT                    │
│  • Roteamento                          │
│  • Circuit Breaker                     │
│  • Rate Limiting                       │
└────┬──────────────────┬────────────────┘
     │                  │
     ▼                  ▼
┌──────────────┐   ┌──────────────┐
│Notification  │   │Quota Service │
│Core (8082)   │   │   (8083)     │
└──────┬───────┘   └──────┬───────┘
       │                  │
       ▼                  ▼
┌─────────────────────────────────┐
│     Infraestrutura              │
│  • Kafka (mensageria)           │
│  • PostgreSQL (transacional)    │
│  • MongoDB (templates)          │
│  • Redis (cache)                │
│  • WireMock (mocks)             │
└─────────────────────────────────┘
```

## 🚀 Quick Start

### Pré-requisitos

- Docker 20+
- Docker Compose 2+
- Java 21 (opcional, para desenvolvimento local)
- Maven 3.9+ (opcional, para desenvolvimento local)

### Subir Todo o Sistema
```bash
# Clone o repositório
git clone https://github.com/thiagosv/notificacao-pos.git
cd notification-system

# Subir todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps

# Ver logs
docker-compose logs -f
```

### Acessar os Serviços

| Serviço | URL | Porta |
|---------|-----|-------|
| API Gateway | http://localhost:8081 | 8081 |
| Quota Service | http://localhost:8083 | 8083 |
| PostgreSQL | localhost | 5432 |
| Redis | localhost | 6379 |
| MongoDB | localhost | 27017 |
| Kafka | localhost | 9093 |
| WireMock | http://localhost:8080 | 8080 |
| Prometheus | http://localhost:9090 | 9090 |
| Grafana | http://localhost:3000 | 3000 |

## 📦 Estrutura do Projeto
```
notification-system/
├── services/
│   ├── api-gateway/          # Gateway com autenticação
│   ├── quota-service/        # Controle de cotas
│   ├── notification-core/    # Orquestrador (Sprint 2)
│   ├── provider-email/       # Provider Email (Sprint 2)
│   ├── provider-sms/         # Provider SMS (Sprint 2)
│   ├── provider-push/        # Provider Push (Sprint 2)
│   ├── event-store-service/  # Event Sourcing (Sprint 3)
│   └── query-service/        # CQRS Query (Sprint 3)
├── infrastructure/
│   ├── docker/               # Dockerfiles customizados
│   ├── observability/        # Prometheus, Grafana, OTEL
│   └── wiremock/             # Mocks de provedores
├── docs/
│   ├── architecture/         # Diagramas e ADRs
│   ├── postman/              # Collections
│   └── sprint-reports/       # Evidências das sprints
├── scripts/                  # Scripts úteis
├── docker-compose.yml
├── pom.xml                   # Parent POM
└── README.md
```

## 🛠️ Stack Tecnológico

### Backend
- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud Gateway**
- **Spring Security + JWT**
- **Spring Data JPA**
- **Spring Kafka**

### Infraestrutura
- **PostgreSQL 15** - Banco transacional
- **MongoDB 7** - Templates e documentos
- **Redis 7** - Cache e rate limiting
- **Apache Kafka 3.x** - Mensageria
- **WireMock 3** - Mocks

### Resiliência e Observabilidade
- **Resilience4j** - Circuit Breaker, Retry, Rate Limiter
- **OpenTelemetry** - Tracing distribuído
- **Prometheus** - Métricas
- **Grafana** - Visualização
- **Flyway** - Migrações de banco

## 📊 Roadmap de Sprints

### ✅ Sprint 1 (Concluída)
- [x] Infraestrutura completa via Docker Compose
- [x] API Gateway com autenticação JWT
- [x] Quota Service com cache Redis
- [x] Circuit Breaker e Fallback
- [x] Persistência PostgreSQL + Flyway

### 🚧 Sprint 2 (Em Andamento)
- [x] Notification Core Service
- [x] Provider Email (WireMock)
- [x] Provider SMS (WireMock)
- [x] Provider Push (WireMock)
- [x] Observabilidade completa (OTEL + Prometheus + Grafana)
- [x] Testes de carga

### 📅 Sprint 3 (Planejada)
- [ ] Event Sourcing com Kafka
- [ ] CQRS (Command/Query Separation)
- [ ] Query Service
- [ ] Templates MongoDB
- [ ] Dashboard Grafana com métricas de negócio
- [ ] Documentação completa

## 📚 Documentação Detalhada

- [API Gateway](services/api-gateway/README.md)
- [Quota Service](services/quota-service/README.md)
- [Arquitetura](docs/architecture/README.md)
- [Diagramas de Sequência](docs/architecture/diagrams/)

## 📊 Métricas e Monitoramento

### Grafana

Acesse: http://localhost:3000
- User: `admin`
- Password: `admin123`

Dashboards disponíveis:
- Business Metrics
- Technical Metrics
- Circuit Breakers Status

## 🔒 Segurança

### Credenciais Padrão (DEV APENAS)

**API Gateway:**
- Client ID: `demo-client`
- Password: `demo123`

**PostgreSQL:**
- User: `notification`
- Password: `notification123`
- Database: `notification_db`

**MongoDB:**
- User: `notification`
- Password: `notification123`

**Grafana:**
- User: `admin`
- Password: `admin123`

⚠️ **IMPORTANTE:** Trocar todas as credenciais em produção!

## 🎓 Contexto Acadêmico

**Projeto Aplicado - TCC**
- **Curso:** Pós-Graduação em Arquitetura de Software e Soluções
- **Instituição:** XP Educação
- **Autor:** Thiago Vieira
- **Período:** Novembro 2025 - Dezembro 2025

### Conceitos Aplicados

- Microsserviços
- Event Sourcing
- CQRS
- Circuit Breaker Pattern
- API Gateway Pattern
- Cache-Aside Pattern
- Repository Pattern
- Observabilidade (Three Pillars)

## 🤝 Contribuição

Este é um projeto acadêmico. Sugestões e melhorias são bem-vindas via issues.

## 📄 Licença

Projeto acadêmico - Todos os direitos reservados.

---

**Desenvolvido durante a Pós-Graduação XP Educação**