# BE-assignment

Spring Boot + Kotlin 기반 AI 챗봇 백엔드 과제

## 기술 스택

- Kotlin 1.9.25 / JVM 21
- Spring Boot 3.5.11 (Web, Data JPA, Actuator)
- PostgreSQL 15.8
- Gemini API (`gemini-flash-latest`)
- Spring Retry (자동 재시도)

## 프로젝트 구조

```
src/main/kotlin/org/example/beassignment/
├── controller/       # API 엔드포인트
├── service/          # 비즈니스 로직
├── client/           # Gemini API 호출
├── dto/              # 요청/응답 DTO
├── config/           # 설정 (AiProperties, WebClient)
└── common/           # 공통 (ApiResponse, ErrorCode, 예외 처리)
```

## 실행 방법

### 1. DB 시작

```bash
docker-compose up -d
```

### 2. API Key 설정

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

`application-local.yml`을 열어 Gemini API Key를 입력합니다.

```yaml
ai:
  api-key: "YOUR_GEMINI_API_KEY_HERE"
```

### 3. 애플리케이션 실행

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

> API Key 없이도 실행은 가능합니다. 이 경우 채팅 요청 시 503을 반환합니다.
> ```bash
> ./gradlew bootRun
> ```

## API 사용

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### POST /api/v1/chat

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "안녕하세요!"}'
```

**성공 응답 (200)**
```json
{
  "success": true,
  "data": { "reply": "안녕하세요! 무엇을 도와드릴까요?" },
  "message": null
}
```

**에러 응답 예시**
```json
{
  "success": false,
  "data": null,
  "message": "AI_SERVICE_UNAVAILABLE"
}
```

### GET /actuator/health

```bash
curl http://localhost:8080/actuator/health
```

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `AI_API_KEY` | `default_dummy_key` | Gemini API Key |
| `AI_BASE_URL` | `https://generativelanguage.googleapis.com` | AI API Base URL |
| `AI_MODEL_NAME` | `gemini-flash-latest` | 사용할 모델 |
| `AI_MAX_RETRIES` | `3` | 재시도 횟수 |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/beassignment` | DB URL |
