# Performance Testing Scripts

Script para teste de carga da API de notificações com geração aleatória de dados para SMS, PUSH e EMAIL.

### 1. K6 Script
**Arquivo:** `performance-test.js`

Teste de performance usando K6 com métricas avançadas.

#### Instalação do K6
```bash
# Windows (via Chocolatey)
choco install k6

# Linux
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# macOS (via Homebrew)
brew install k6
```

#### Execução
```bash
cd scripts

# Executar com configuração padrão (ramp-up gradual até 200 VUs)
k6 run performance-test.js

# Executar com configurações customizadas
k6 run --vus 50 --duration 60s performance-test.js

# Executar e gerar relatório HTML
k6 run --out json=results.json performance-test.js
```

#### Configuração do Teste
O script está configurado com os seguintes estágios:
- 30s: Ramp-up de 0 → 50 VUs
- 1m: Ramp-up de 50 → 100 VUs
- 2m: Mantém 100 VUs (teste de sustentação)
- 30s: Ramp-up de 100 → 200 VUs
- 2m: Mantém 200 VUs (teste de pico)
- 30s: Ramp-down de 200 → 0

**TPS Esperado:** ~1000-2000 requisições/segundo no pico
