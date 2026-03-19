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
- WebClient (Reactor Netty), 커스텀 Retry (Kotlin 코루틴 기반)

## 프로젝트 구조

```
src/main/kotlin/org/example/beassignment/
├── controller/   # API 엔드포인트
├── service/      # 비즈니스 로직
├── client/       # Gemini API 호출
├── dto/          # 요청/응답 DTO
├── entity/       # JPA 엔티티
├── repository/   # Spring Data JPA 리포지토리
├── config/       # Security, JWT, WebClient, Swagger 설정
└── common/       # ApiResponse, ErrorCode, 전역 예외 처리
```

---

## 과제 분석 방법

[Speckit](https://github.com/github/spec-kit)을 활용한 **Specification-Driven Development(SDD)** 방식으로 진행했습니다.
각 기능을 **사양 → 계획 → 작업 단위 → 구현**의 4단계로 나누어, 스펙을 먼저 확정한 뒤 개발에 착수했습니다.

기능별로 `spec.md`, `plan.md`, `data-model.md`, `quickstart.md` 등의 설계 문서를 작성했으며,
내용이 방대하여 GitHub에는 업로드하지 않고 로컬에 보관 중입니다 (추후 아카이빙 가능).
API 명세는 **Swagger UI**에 상세히 기술해두었습니다.

## AI 활용 방식

### AI 모델 선택 — Gemini

과제에서는 OpenAI를 예시로 들었지만, 3시간 제한 내 시간을 절약하기 위해 미리 발급해둔 **Gemini API 키**를 사용했습니다.
고객사 조건이 *"OpenAI 등 유명 provider는 알고 있지만 API spec에 대한 깊은 이해는 없다"* 였으므로,
provider 변경이 시연 결과에 영향을 주지 않는다고 판단했습니다.

### AI를 활용한 기획 및 기술 스택 정의

서비스 요구사항에서 **구체적 정의가 부족한 부분**을 AI로 검토하여 사전에 식별하고, 스펙을 확정한 뒤 개발을 진행했습니다.

| 미정의 항목 | 영향 범위 | 결정 |
|------------|----------|------|
| 관리자의 스레드 삭제 권한 | 인가(Authorization) 레이어 설계 | 관리자는 모든 스레드 삭제 가능 |
| 정렬 기준의 모호함 (스레드 vs 채팅) | 쿼리 설계 | 스레드는 `createdAt` 정렬, 채팅은 오름차순 고정 |
| JWT 만료 후 세션 유지 전략 | 인증 엔드포인트 수 | 재로그인 방식(stateless) 채택 |
| AI 응답 형식 (SSE vs JSON) | 클라이언트 통합 방식 | JSON 응답, 스트리밍은 인터페이스만 보존 |

이러한 모호한 지점들을 개발 전에 확정함으로써, 구현 중 방향 전환 없이 일관된 설계를 유지할 수 있었습니다.

## 확장성을 고려한 구현

### 1. AI 채팅 — Kotlin 코루틴 기반 비동기 처리

AI API 호출처럼 응답이 느린 외부 통신에 대비해, `suspend` 함수를 활용한 **논블로킹 비동기 처리**를 구현했습니다.
서비스 규모가 커지더라도 스레드 블로킹 없이 효율적으로 동시 요청을 처리할 수 있는 구조입니다.

### 2. 테스트 코드

커버리지가 높지는 않지만, **핵심 비즈니스 로직에 대한 단위 테스트**를 작성해두었습니다.
향후 유지보수와 기능 확장 시 회귀 버그를 빠르게 감지할 수 있습니다.

### 3. RAG 도입 대비 아키텍처

고객사가 향후 자사 대외비 문서를 학습시키고 싶어하는 요구사항에 대비해,
채팅 파이프라인에 **문서 검색 확장 포인트**를 미리 구성해두었습니다.

```
사용자 질문 → ContextRetriever.retrieve() → contextChunks → SystemPromptBuilder → AI 호출
                     ↑
              현재: NoOpContextRetriever (빈 리스트 반환, 기존 동작 유지)
              향후: VectorDbContextRetriever (Pinecone, Weaviate 등 연동)
```

- `ContextRetriever` 인터페이스: 질문 기반 문서 검색 추상화
- `NoOpContextRetriever`: 현재 기본 구현체 (빈 결과 반환, 기존 기능 영향 없음)
- `ChatService`에서 retrieve → chat 흐름이 이미 연결되어 있어, 벡터 DB 구현체만 교체하면 RAG 적용 가능

## 가장 어려웠던 기능

### Spring MVC + Kotlin 코루틴 환경에서의 SecurityContext 유실 문제

기존에 Java 개발을 주로 해왔기 때문에 코루틴이 낯설었지만, AI API 호출의 비동기 처리 필요성을 느끼고 이번 프로젝트를 학습 계기로 삼았습니다.
AI 클라이언트 레이어에 `suspend` 함수를 적용해 논블로킹 구조로 설계했습니다.

**증상**

기능은 정상 동작했지만, 배포 직전 테스트에서 예상치 못한 문제가 발생했습니다.

- 유효한 JWT 토큰으로 채팅 메시지 전송 → **401 Unauthorized** 반환
- 서버 로그 확인 시 AI API 호출 성공, DB 저장도 정상
- 대화 목록 조회 시 방금 보낸 메시지가 정상 조회됨
- **비즈니스 로직은 완벽하게 수행되었는데 응답만 401로 내려오는 상황**

**원인 분석**

Spring MVC와 코루틴의 동작 방식 차이가 원인이었습니다.

```
[1차 dispatch]  JWT 필터 → SecurityContext 설정 (ThreadLocal) → 코루틴 실행 → 비즈니스 로직 성공
                                    ↓
[async dispatch] 코루틴 완료 → 응답 반환 시도 → 서블릿 스레드 교체 → SecurityContext 비어있음 → 401
```

컨트롤러에 `suspend`를 붙이면 Spring MVC는 이를 비동기 요청으로 처리하며, 코루틴 완료 후 **async dispatch**가 발생합니다.
Spring Security의 `SecurityContext`는 **ThreadLocal 기반**이므로, 스레드가 바뀌면 인증 정보가 유실됩니다.
`SessionCreationPolicy.STATELESS` 설정으로 세션 복원도 불가능했습니다.

**해결**

`SecurityConfig`에 `RequestAttributeSecurityContextRepository`를 설정하여,
SecurityContext를 ThreadLocal이 아닌 **HttpServletRequest의 attribute에 저장**하도록 변경했습니다.
async dispatch에서도 동일한 request 객체를 공유하므로 인증 정보가 유실되지 않습니다.

**얻은 교훈**

코루틴 문법뿐 아니라, 서블릿 기반 프레임워크 위에서 코루틴이 실행될 때 **스레드 컨텍스트 전파 방식**에 대한 이해를 얻었습니다.
Java에서는 의식하지 않아도 되었던 ThreadLocal 기반 SecurityContext가 비동기 환경에서는 문제가 될 수 있다는 점을 체감했고,
새로운 기술 적용 시 **기존 기술 스택과의 호환성**까지 고려해야 한다는 교훈을 얻었습니다.
