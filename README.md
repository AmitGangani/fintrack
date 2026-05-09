# FinTrack

FinTrack is a personal finance backend built with Spring Boot microservices. It supports user authentication, account management, transactions, monthly summaries, and category budgets.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security with JWT
- Spring Cloud Gateway
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Flyway
- Docker Compose

## Services

- `auth-service` - registration, login, and JWT authentication
- `account-service` - user accounts and balance updates
- `transaction-service` - income, expenses, summaries, and transaction events
- `budget-service` - monthly budgets and spending tracking
- `api-gateway` - single entry point for all APIs

## Run Locally

```bash
docker compose up --build
```

API Gateway runs on:

```text
http://localhost:8080
```

## Main APIs

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/accounts`
- `GET /api/transactions`
- `GET /api/transactions/summary`
- `GET /api/budgets/monthly`

Protected APIs require a bearer JWT from login.

## Build Services

```bash
./mvnw clean package -DskipTests
```

Run the command inside any service directory.
