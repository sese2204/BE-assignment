# BE-assignment Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-19

## Active Technologies
- Kotlin 1.9.25 on JVM 21 + Spring Boot 3.5.11 (Web MVC, Data JPA, Security), Spring WebFlux (002-user-auth-chat)
- PostgreSQL 15.8 (local via Docker Compose) (002-user-auth-chat)
- Kotlin 1.9.25 on JVM 21 + Spring Boot 3.5.11 (Web MVC, Data JPA, Security), Spring WebFlux (WebClient) (003-feedback-analytics-report)

- Kotlin 1.9.25 on JVM 21 + Spring Boot 3.5.11, Spring Web MVC, Spring Data JPA, Spring WebFlux (001-ai-boilerplate)

## Project Structure

```text
src/main/kotlin/org/example/beassignment/
├── common/           ← ApiResponse, ErrorCode, BusinessException, GlobalExceptionHandler
├── config/           ← AiProperties (@ConfigurationProperties), WebClientConfig
└── chat/             ← controller/, service/, client/, dto/
```

## Commands

```bash
# Start DB
docker-compose up -d

# Run app (no API key — safe for eval)
./gradlew bootRun

# Run app with real AI key
AI_API_KEY=sk-... ./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew build
```

## Code Style

- Kotlin idiomatic style: data classes for DTOs, sealed classes for domain errors
- Spring Boot conventions: constructor injection, `@ConfigurationProperties` over `@Value`
- All responses MUST use `ApiResponse<T>` wrapper
- All exceptions MUST flow through `GlobalExceptionHandler`
- Conversation with user: **Korean** | Specification/code documents: **English**

## Recent Changes
- 003-feedback-analytics-report: Added Kotlin 1.9.25 on JVM 21 + Spring Boot 3.5.11 (Web MVC, Data JPA, Security), Spring WebFlux (WebClient)
- 002-user-auth-chat: Added Kotlin 1.9.25 on JVM 21 + Spring Boot 3.5.11 (Web MVC, Data JPA, Security), Spring WebFlux

- 001-ai-boilerplate: Added Kotlin 1.9.25 on JVM 21 + Spring Boot 3.5.11, Spring Web MVC, Spring Data JPA, Spring WebFlux

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
