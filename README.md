# Portfolio Intelligence Platform

Backend inicial organizado na arquitetura tradicional:

```text
REST/Controller → Service → Repository → PostgreSQL
```

## Tecnologias

- Java 25
- Spring Boot 4.1
- Gradle Wrapper 9.1
- PostgreSQL 18
- Flyway
- Spring Data JPA
- Validation
- ProblemDetail
- Docker Compose
- Testcontainers

## Estrutura

```text
com.portfoliointelligence
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
├── config
└── PortfolioIntelligenceApplication
```

## Executar

Na raiz:

```powershell
docker compose up -d postgres
```

No backend:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

Health check:

```text
http://localhost:8080/actuator/health
```

As chamadas da API estão em `backend/requests.http`.
