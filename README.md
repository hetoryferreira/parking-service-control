# Estapar Parking Service

> Um serviço Spring Boot 3 limpo e testável para gerenciar fluxos de estacionamento (ENTRADA → ESTACIONADO → SAÍDA), preços dinâmicos, consultas de receita e configuração de garagem — criado para impressionar os revisores com clareza, registro e testes.
<p align="center">
  <a href="https://openjdk.org/projects/jdk/21/"><img alt="Java" src="https://img.shields.io/badge/Java-21-blue"></a>
  <a href="https://spring.io/projects/spring-boot"><img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen"></a>
  <img alt="Build" src="https://img.shields.io/badge/Build-Gradle-02303A">
  <img alt="Tests" src="https://img.shields.io/badge/Tests-JUnit%205%20%7C%20Mockito%20%7C%20AssertJ-orange">
</p>

---

## ✨ Highlights

- **Webhook flow**: ENTRY (abre sessão), PARKED (vincula setor/vaga) e EXIT (fecha e calcula valor).
- **Dynamic pricing** configurável via `application.yml` (limiares, fatores, arredondamento, janela grátis).
- **Endpoints limpos** com validações, respostas consistentes e **tratamento de exceções centralizado**.
- **Camadas bem definidas** (Controller → Service → Repository) + **DTOs** para respostas REST.
- **Logs em inglês com MDC** (placa, correlação, etc.) facilitando troubleshooting.
- **Testes unitários** para services e controllers com Mockito + MockMvc.

---

## 🧱 Arquitetura (alto nível)

```
Controller (REST + validation)
      ↓
 Service (regras, transações, logs)
      ↓
 Repository (JPA/Hibernate)
      ↓
 H2 (dev/test)  | MySQL (prod-ready)
```

Principais domínios:
- `Garage`, `GarageSector`, `Spot`
- `ParkingSession` + `SessionStatus` (ENTRY, PARKED, EXIT)

Principais serviços:
- `ParkingServiceImpl` (ciclo de vida da sessão)
- `PricingServiceImpl` (preço dinâmico + cálculo de cobrança)
- `GarageServiceImpl` (operações sobre garage)
- `GarageSectorServiceImpl` (alocação em setor + leitura de setores)
- `SpotServiceImpl` (vaga próxima, ocupação, busca por garagem)

---

## ⚙️ Requisitos

- **Java 21**
- **Gradle 8+**
- Docker para publicar imagem)

---

## 🚀 Como rodar

### 1) Local (perfil `dev` com H2)

```bash
./gradlew clean bootRun --args='--spring.profiles.active=dev'
```

Acesse:
- API: `http://localhost:3003`
- Swagger UI: `http://localhost:3003/swagger`

### 2) Docker (opcional)

```bash
# Build da imagem
docker build -t estapar/parking-api:local .

# Execução
docker run --rm -p 3003:3003 \
  -e SPRING_PROFILES_ACTIVE=dev \
  estapar/parking-api:local
```

---

## 🔧 Configurações importantes (`application-dev.yml`)

```yaml
server:
  port: 3003

spring:
  jpa:
    open-in-view: false
    properties:
      hibernate.format_sql: true
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql

logging:
  level:
    root: INFO
    org.hibernate.SQL: debug
    org.hibernate.orm.jdbc.bind: trace

pricing:
  thresholds:
    t25: 0.25
    t50: 0.50
    t75: 0.75
  factors:
    discount10: 0.90   # -10% quando < 25%
    base: 1.00         #  0% quando <= 50%
    surcharge10: 1.10  # +10% quando <= 75%
    surcharge25: 1.25  # +25% quando > 75%
  price-scale: 2
  price-rounding: HALF_UP
  free-minutes: 30
```

---

## 📚 Endpoints principais

### 1) Webhook: ENTRY / PARKED / EXIT
`POST /webhook`

- **ENTRY**: cria sessão com preço efetivo calculado na hora da entrada (apenas `garage_code` e `license_plate`).
- **PARKED**: resolve setor via coordenadas e aloca a vaga.
- **EXIT**: encerra e calcula o valor (considera janela grátis e tarifa por hora cheia).

Exemplos de carga JSON (simplificado):
```json
// ENTRY
{
  "event_type": "ENTRY",
  "license_plate": "ABC1D23",
  "garage_code": "1"
}

// PARKED
{
  "event_type": "PARKED",
  "license_plate": "ABC1D23",
  "garage_code": "1",
  "lat": -23.561584,
  "lng": -46.656081
}

// EXIT
{
  "event_type": "EXIT",
  "license_plate": "ABC1D23",
  "garage_code": "1",
  "exit_time": "2025-01-01T12:00:00-03:00"
}
```

### 2) Configuração da Garagem
`GET /garage?garage_code=1`

Resposta:
```json
{
  "garage": [
    { "sector": "A", "basePrice": 10.00, "max_capacity": 100 },
    { "sector": "B", "basePrice": 10.00, "max_capacity": 80 }
  ],
  "spots": [
    { "id": 1, "sector": "A", "lat": -23.561684, "lng": -46.655981 }
  ]
}
```

### 3) Receita do dia por setor
`GET /revenue?date=2025-01-01&sector=A&garage_code=1`

Resposta:
```json
{
  "amount": 37.50,
  "currency": "BRL",
  "timestamp": "2025-01-01T12:00:00.000Z"
}
```

> A data é sempre tratada como **um único dia** `[00:00, 23:59:59]` (window `[inclusive, exclusive next day)` internamente).

---

## 💸 Regras de Preço (Pricing)

1. **Preço dinâmico** na entrada, baseado na **ocupação da garagem**:
    - `< 25%` → base × `0.90`
    - `<= 50%` → base × `1.00`
    - `<= 75%` → base × `1.10`
    - `> 75%` → base × `1.25`

2. **Cobrança** (EXIT):
    - **Janela grátis**: `pricing.free-minutes` (default: **30 min**)
    - Depois da janela, cobra **por hora cheia** (arredonda para cima)
    - Usa o preço efetivo salvo na sessão (fallback: recalcula pelo serviço)

---


## 📦 Estrutura (resumida)

```
src/main/java/com/estapar/parking
  ├─ application/controller/          # REST endpoints
  ├─ application/service/             # contracts
  ├─ application/request/             # DTOs de entrada/saída
  ├─ application/error/               # exceptions + handler
  ├─ application/support/             # MDC, properties
  └─ application/domain/              # model, repo, view
