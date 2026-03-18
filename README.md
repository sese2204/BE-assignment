# BE-assignment

Spring Boot + Kotlin 기반 AI 챗봇 백엔드 과제

## 실행 방법

**1. DB 시작**
```bash
docker-compose up -d
```

**2. API Key 설정**
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# application-local.yml 열어서 Gemini API Key 입력
```

**3. 서버 실행**
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

> API Key 없이도 실행 가능 (`./gradlew bootRun`). 채팅 요청 시 503 반환.

---

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health`

## 기술 스택

- Kotlin 1.9.25 / JVM 21 / Spring Boot 3.5.11
- PostgreSQL 15.8 (Docker Compose)
- Gemini API (`gemini-flash-latest`)
- Spring Retry, WebClient (Reactor Netty)

## 프로젝트 구조

```
src/main/kotlin/org/example/beassignment/
├── controller/   # API 엔드포인트
├── service/      # 비즈니스 로직
├── client/       # Gemini API 호출
├── dto/          # 요청/응답 DTO
├── config/       # AiProperties, WebClient 설정
└── common/       # ApiResponse, ErrorCode, 전역 예외 처리
```
