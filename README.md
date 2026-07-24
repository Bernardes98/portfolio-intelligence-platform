# Portfolio Intelligence Platform

Backend inicial organizado na arquitetura tradicional:

```text
REST/Controller → Service → Repository → PostgreSQL
```

A aplicação permite cadastrar clientes investidores e criar análises de carteira, servindo como base para o futuro processamento e consolidação de relatórios de investimentos.

## Tecnologias

- Java 25
- Spring Boot 4.1
- Gradle Wrapper 9.1
- PostgreSQL 18
- Flyway
- Spring Data JPA
- Bean Validation
- ProblemDetail
- Spring Security
- Swagger / OpenAPI
- Docker Compose
- Testcontainers
- JUnit 5
- Mockito

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

## Pré-requisitos

Antes de executar o projeto, é necessário possuir:

- Java 25
- Docker Desktop
- Git

O Gradle não precisa estar instalado globalmente, pois o projeto utiliza Gradle Wrapper.

## Executar o projeto

Na raiz do projeto, suba o PostgreSQL:

```powershell
docker compose up -d postgres
```

Confira se o container está rodando:

```powershell
docker compose ps
```

Entre na pasta do backend:

```powershell
cd backend
```

Execute os testes e gere o build:

```powershell
.\gradlew.bat clean build
```

Inicie a aplicação:

```powershell
.\gradlew.bat bootRun
```

A API ficará disponível em:

```text
http://localhost:8080
```

## Swagger

Com a aplicação em execução, acesse a interface do Swagger para visualizar e testar as rotas:

```text
http://localhost:8080/swagger-ui/index.html
```

Também é possível acessar pelo endereço:

```text
http://localhost:8080/swagger-ui.html
```

O contrato OpenAPI em formato JSON está disponível em:

```text
http://localhost:8080/v3/api-docs
```

No Swagger, utilize o botão **Try it out** para preencher os parâmetros e executar as requisições diretamente pelo navegador.

## Rotas disponíveis

### Clientes

Cadastrar um cliente:

```http
POST /api/v1/clients
```

Consultar um cliente pelo ID:

```http
GET /api/v1/clients/{id}
```

### Análises

Criar uma análise para um cliente:

```http
POST /api/v1/clients/{clientId}/analyses
```

Listar as análises de um cliente:

```http
GET /api/v1/clients/{clientId}/analyses
```

Consultar uma análise pelo ID:

```http
GET /api/v1/analyses/{analysisId}
```

## Health check

Para verificar se a aplicação está funcionando:

```text
http://localhost:8080/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

## Requisições pelo IntelliJ

Exemplos de chamadas da API também estão disponíveis no arquivo:

```text
backend/requests.http
```

## Executar os testes

Dentro da pasta `backend`:

```powershell
.\gradlew.bat test
```

Ou execute o build completo:

```powershell
.\gradlew.bat clean build
```

Os testes de integração utilizam Testcontainers para criar uma instância temporária do PostgreSQL durante a execução.

## Parar o ambiente

Para encerrar a aplicação, pressione:

```text
Ctrl + C
```

Para desligar o PostgreSQL:

```powershell
docker compose down
```

Para desligar e remover também o volume local do banco:

```powershell
docker compose down -v
```