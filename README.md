# 2026-ACC-EWHA-CHATTR-BE

Chattr 백엔드 서버 — Spring Boot + AWS DynamoDB + Cognito

## 기술 스택

- **Java 17** / Spring Boot 4
- **AWS DynamoDB** (Enhanced Client v2)
- **AWS Cognito** — JWT 기반 인증
- **Terraform** — 프로덕션 인프라 관리

---

## 로컬 개발 환경 실행

### 사전 준비
- Docker
- JDK 17
- (프로덕션 배포 시) Terraform, AWS CLI

### 1. DynamoDB Local 컨테이너 시작

```bash
docker-compose up -d
```

포트 `8000`에 DynamoDB Local이 실행됩니다.

### 2. 환경변수 설정

```bash
export DYNAMODB_ENDPOINT=http://localhost:8000
export AWS_REGION=ap-northeast-2
export COGNITO_USER_POOL_ID=local-dummy   # 로컬에선 아무 값이나 가능
```

### 3. 앱 실행

```bash
./gradlew bootRun
```

앱이 시작될 때 `DynamoDbTableInitializer`가 DynamoDB Local에 7개 테이블과 GSI를 자동 생성합니다.
이미 테이블이 존재하면 그냥 넘어가므로 재시작해도 안전합니다.

---

## 프로덕션 배포

### 1. Terraform으로 DynamoDB 테이블 생성

```bash
cd terraform
terraform init
terraform apply -var="env=prod"
```

완료 후 출력되는 `table_names`를 환경변수로 설정합니다.

```
table_names = {
  TABLE_CHANNEL          = "chattr-prod-channel"
  TABLE_CHANNEL_MEMBER   = "chattr-prod-channel-member"
  TABLE_DM               = "chattr-prod-dm"
  TABLE_MESSAGE          = "chattr-prod-message"
  TABLE_USER             = "chattr-prod-user"
  TABLE_WORKSPACE        = "chattr-prod-workspace"
  TABLE_WORKSPACE_MEMBER = "chattr-prod-workspace-member"
}
```

### 2. 필수 환경변수

| 변수 | 설명 |
|------|------|
| `AWS_REGION` | AWS 리전 (기본값: `ap-northeast-2`) |
| `COGNITO_USER_POOL_ID` | Cognito User Pool ID |
| `TABLE_USER` | DynamoDB 유저 테이블명 |
| `TABLE_WORKSPACE` | DynamoDB 워크스페이스 테이블명 |
| `TABLE_WORKSPACE_MEMBER` | DynamoDB 워크스페이스 멤버 테이블명 |
| `TABLE_CHANNEL` | DynamoDB 채널 테이블명 |
| `TABLE_CHANNEL_MEMBER` | DynamoDB 채널 멤버 테이블명 |
| `TABLE_DM` | DynamoDB DM 테이블명 |
| `TABLE_MESSAGE` | DynamoDB 메시지 테이블명 |

`DYNAMODB_ENDPOINT`를 세팅하지 않으면 실제 AWS DynamoDB에 연결됩니다.

---

## DynamoDB 테이블 구조

| 테이블 | PK | SK | GSI |
|--------|----|----|-----|
| `chattr-{env}-user` | `id` | — | `cognito-sub-index` (PK: `cognitoSub`) |
| `chattr-{env}-workspace` | `id` | — | — |
| `chattr-{env}-workspace-member` | `workspaceId` | `userId` | `user-workspaces-index` (PK: `userId`, SK: `workspaceId`) |
| `chattr-{env}-channel` | `id` | — | `workspace-channels-index` (PK: `workspaceId`) |
| `chattr-{env}-channel-member` | `channelId` | `userId` | `user-channels-index` (PK: `userId`, SK: `channelId`) |
| `chattr-{env}-dm` | `id` | — | `dm-users-index` (PK: `userAId`, SK: `userBId`) |
| `chattr-{env}-message` | `id` | — | `room-messages-index` (PK: `roomId`, SK: `createdAt`), TTL: `ttl` |

---

## 패키지 구조

도메인 단위로 패키지를 구성합니다. 각 도메인 폴더 아래 `controller/`, `service/`, `dto/`, `entity/`, `repository/` 레이어를 둡니다.

```
src/main/java/com/acc/chattr/
│
├── ChattrApplication.java
│
├── config/                              # 스프링 설정
│   ├── DynamoDbConfig.java              # DynamoDB 클라이언트 & 테이블 빈 등록
│   ├── DynamoDbTableInitializer.java    # 로컬 실행 시 테이블 자동 생성
│   └── SecurityConfig.java             # Cognito JWT 인증 설정
│
├── security/
│   └── CognitoUserSyncFilter.java       # JWT 검증 후 첫 로그인 시 유저 자동 생성
│
├── common/                              # 공통 인프라 (도메인 무관)
│   ├── code/
│   │   ├── Code.java                    # 에러코드 인터페이스
│   │   ├── BusinessErrorCode.java       # 도메인 에러코드
│   │   └── GeneralErrorCode.java        # HTTP 공통 에러코드
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── GeneralException.java
│   │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│   └── response/
│       └── Response.java             # 공통 응답 래퍼
│
└── domain/                              # 도메인별 패키지 (기능 단위)
    ├── common/
    │   └── BaseEntity.java              # createdAt, deletedAt 공통 필드
    │
    ├── health/                          # 각 도메인은 아래 레이어를 필요한 만큼 가짐
    │   ├── controller/
    │   │   └── HealthController.java
    │   └── dto/
    │       └── HealthResponse.java
    │
    ├── user/
    │   ├── controller/                  # (미구현)
    │   ├── service/                     # (미구현)
    │   ├── dto/                         # (미구현)
    │   ├── entity/
    │   │   └── User.java
    │   └── repository/
    │       ├── UserRepository.java
    │       └── UserDynamoRepository.java
    │
    ├── workspace/
    │   ├── controller/                  # (미구현)
    │   ├── service/                     # (미구현)
    │   ├── dto/                         # (미구현)
    │   ├── entity/
    │   │   ├── Workspace.java
    │   │   ├── WorkspaceMember.java
    │   │   └── WorkspaceRole.java       # ADMIN | MEMBER
    │   └── repository/
    │       └── WorkspaceRoleConverter.java
    │
    ├── channel/
    │   ├── controller/                  # (미구현)
    │   ├── service/                     # (미구현)
    │   ├── dto/                         # (미구현)
    │   ├── entity/
    │   │   ├── Channel.java
    │   │   └── ChannelMember.java
    │   └── repository/                  # (미구현)
    │
    ├── dm/
    │   ├── controller/                  # (미구현)
    │   ├── service/                     # (미구현)
    │   ├── dto/                         # (미구현)
    │   ├── entity/
    │   │   └── Dm.java
    │   └── repository/                  # (미구현)
    │
    └── message/
        ├── controller/                  # (미구현)
        ├── service/                     # (미구현)
        ├── dto/                         # (미구현)
        ├── entity/
        │   ├── Message.java
        │   ├── MessageAttachment.java
        │   └── RoomType.java            # CHANNEL | DM
        └── repository/
            └── RoomTypeConverter.java
```

---

## 공통 응답 구조

모든 API는 `Response<T>`로 감싸서 반환합니다.

```json
{
  "success": true,
  "statusCode": 200,
  "message": "OK",
  "data": { }
}
```

### 응답 생성 방법

```java
// 데이터 있는 성공 응답
return ResponseEntity.ok(Response.ok(data));

// 데이터 없는 성공 응답 (생성/삭제 등)
return ResponseEntity.ok(Response.ok());

// 실패 응답 — GlobalExceptionHandler가 자동 처리
throw new BusinessException(BusinessErrorCode.CHANNEL_NOT_FOUND);
throw new GeneralException(GeneralErrorCode.UNAUTHORIZED);
```

---

## 에러 코드

### BusinessErrorCode — 도메인 비즈니스 예외

`BusinessException`을 throw하면 `GlobalExceptionHandler`가 자동으로 에러 응답을 만듭니다.

| 코드 | HTTP | 메시지 |
|------|------|--------|
| `USER_NOT_FOUND` | 404 | 존재하지 않는 사용자입니다. |
| `WORKSPACE_NOT_FOUND` | 404 | 존재하지 않는 워크스페이스입니다. |
| `WORKSPACE_MEMBER_NOT_FOUND` | 404 | 워크스페이스 멤버가 아닙니다. |
| `WORKSPACE_MEMBER_ALREADY_EXISTS` | 409 | 이미 워크스페이스에 가입된 사용자입니다. |
| `NOT_WORKSPACE_ADMIN` | 403 | 워크스페이스 관리자만 가능한 작업입니다. |
| `CHANNEL_NOT_FOUND` | 404 | 존재하지 않는 채널입니다. |
| `CHANNEL_MEMBER_NOT_FOUND` | 404 | 채널 멤버가 아닙니다. |
| `CHANNEL_MEMBER_ALREADY_EXISTS` | 409 | 이미 채널에 참여한 사용자입니다. |
| `DM_NOT_FOUND` | 404 | 존재하지 않는 DM입니다. |
| `DM_ALREADY_EXISTS` | 409 | 이미 DM이 존재합니다. |
| `DM_NOT_PARTICIPANT` | 403 | DM 참여자가 아닙니다. |
| `MESSAGE_NOT_FOUND` | 404 | 존재하지 않는 메시지입니다. |
| `MESSAGE_NOT_SENDER` | 403 | 본인이 보낸 메시지만 수정/삭제할 수 있습니다. |

### GeneralErrorCode — HTTP/공통 예외

Spring MVC, Security, Validation 예외는 `GlobalExceptionHandler`가 자동으로 아래 코드로 매핑합니다.

| 코드 | HTTP | 트리거 상황 |
|------|------|------------|
| `VALIDATION_ERROR` | 400 | `@Valid` 실패, `ConstraintViolation` |
| `INVALID_REQUEST_PARAMETER` | 400 | 파라미터 누락·타입 불일치 |
| `BAD_REQUEST` | 400 | JSON 파싱 실패 등 |
| `UNAUTHORIZED` | 401 | 미인증 접근, 유효하지 않은 토큰 |
| `FORBIDDEN` | 403 | 인증은 됐지만 권한 없음 |
| `NOT_FOUND` | 404 | 존재하지 않는 엔드포인트 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content-Type 불일치 |
| `INTERNAL_SERVER_ERROR` | 500 | 처리되지 않은 예외 |

### 에러 응답 예시

```json
{
  "success": false,
  "statusCode": 404,
  "message": "존재하지 않는 채널입니다.",
  "data": null
}
```

---

## 인증 흐름

1. 클라이언트가 Cognito에서 **Access Token** 발급
2. `Authorization: Bearer <Access Token>` 헤더로 요청
3. Spring Security가 Cognito JWKS로 서명 검증
4. `CognitoUserSyncFilter`가 첫 로그인 시 DynamoDB에 유저 자동 생성
