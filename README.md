# Banking Enterprise Platform

Secure, event-driven digital banking platform: a React SPA, Spring Boot microservices, Keycloak for identity,
Camunda for long-running processes, Kafka for event streaming, and Apache Camel bridging the legacy core banking MQ.

* **Architecture blueprint:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) (integration, workflow, IAM, transactional integrity, compliance)
* **Backlog:** Jira space [BEP](https://ezayoub.atlassian.net/jira/software/projects/BEP/boards), epics BEP-1 to BEP-4

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Cloud Gateway, Spring Data JPA, Flyway, PostgreSQL 16 |
| Integration | Apache Camel 4.18 LTS, JMS (ActiveMQ Classic locally, IBM MQ in bank environments), Apache Kafka 4 (KRaft) |
| Workflow | Camunda 7 embedded engine (BPMN 2.0), Cockpit and Tasklist |
| Identity | Keycloak 26 (OIDC, PKCE, realm roles, audience restriction) |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS 4, keycloak-js, Axios |

> **Support status:** Spring Boot 3.5, Spring Cloud 2025.0 and Camunda 7 Community Edition are past their open-source
> end of life. They are fine for building and learning, but choose an upgrade or extended-support path before going
> live. See [Runtime support status](docs/ARCHITECTURE.md#runtime-support-status-checked-november-2026).

## Repository layout

```
banking-enterprise-platform/
├── docker/                          Local infrastructure (Docker Compose)
│   ├── docker-compose.yml           PostgreSQL, Keycloak, Kafka (KRaft), ActiveMQ
│   ├── stack.sh                     up | down | reset | status | logs
│   ├── keycloak/banking-realm.json  Realm, clients, roles and demo users (imported on start)
│   ├── postgres/init/               One database and owner role per bounded context
│   └── scripts/                     Helpers, e.g. send a sample core banking message
├── backend/                         Maven multi-module project
│   ├── banking-common/              API envelope, error codes, events, audit, Keycloak role mapping
│   ├── banking-gateway/             Spring Cloud Gateway: JWT validation, routing, CORS   :8080
│   ├── banking-core-service/        Accounts and transactions, REST and Kafka ledger      :8081
│   ├── banking-integration-camel/   Core MQ to Kafka routes (EIP)                         :8082
│   └── banking-workflow-camunda/    Loan approval BPMN process, delegates, Cockpit         :8083
├── frontend/                        React SPA                                              :5173
│   └── src/{api,auth,components,features,hooks,lib}
└── docs/ARCHITECTURE.md
```

## Getting started

### Prerequisites

* Docker with Docker Compose v2
* Java 21 and Maven 3.9+
* Node.js 22+

### 1. Start the infrastructure

```bash
cp docker/.env.example docker/.env      # optional: change the local passwords
./docker/stack.sh up
```

| Service | URL | Credentials (local only) |
|---|---|---|
| Keycloak admin | http://localhost:8180/admin | `admin` / `admin` |
| ActiveMQ console | http://localhost:8161/admin | `admin` / `admin` |
| Kafka | `localhost:9092` | none |
| PostgreSQL | `localhost:5432` | see `docker/.env.example` |

### 2. Build and run the backend

```bash
cd backend
mvn clean install

# one terminal per service
mvn -pl banking-core-service spring-boot:run -Dspring-boot.run.profiles=dev   # dev profile loads demo accounts
mvn -pl banking-gateway spring-boot:run
mvn -pl banking-integration-camel spring-boot:run
mvn -pl banking-workflow-camunda spring-boot:run
```

Camunda Cockpit and Tasklist: http://localhost:8083/camunda (`admin` / `admin`).
Core service API docs: http://localhost:8081/swagger-ui.html

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 and sign in as `customer.demo`.

### Demo users (realm `banking-realm`)

| Username | Password | Role |
|---|---|---|
| `customer.demo` | `Customer#2026!` | CUSTOMER |
| `teller.demo` | `Teller#2026!!` | TELLER |
| `underwriter.demo` | `Underwriter#2026!` | UNDERWRITER |
| `admin.demo` | `PlatformAdmin#2026!` | ADMIN |

These accounts exist only in the local realm export. Never import that file into a shared environment.

## Try the flows

**Call the API with a token** (the `banking-dev-cli` client exists only for local testing):

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/banking-realm/protocol/openid-connect/token \
  -d grant_type=password -d client_id=banking-dev-cli \
  -d username=customer.demo -d 'password=Customer#2026!' | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/me | jq
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/accounts | jq
```

**Legacy core → Kafka → ledger:** put a core banking message on the MQ, then watch the balance change on the dashboard.

```bash
./docker/scripts/send-sample-core-message.sh           # valid credit of 125.50 EUR
./docker/scripts/send-sample-core-message.sh invalid   # ends on CORE.BANKING.TRANSACTIONS.DLQ
```

**Loan approval (Camunda):** submit an application from the Loans page, then open Tasklist, claim the
"Underwriter review" task and approve or reject it. To test compensation, set the process variable
`simulateDisbursementFailure` to `true` in Cockpit before approving.

## Tests

```bash
cd backend && mvn verify      # unit and slice tests
cd frontend && npm run build  # type-check and production build
```

CI runs the same commands on every push and pull request (`.github/workflows/ci.yml`).

## Conventions

* Money is `BigDecimal` / `NUMERIC(19,4)` in the backend and is only formatted, never computed, in the browser.
* Every API response uses the `{ success, data, error }` envelope from `banking-common`.
* Every event carries an id, a key, a schema version and a correlation id. Every consumer is idempotent.
* No secrets in Git: local defaults live in `.env.example`, real values come from a secrets manager.
