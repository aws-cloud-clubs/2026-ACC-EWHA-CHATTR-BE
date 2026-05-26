# 2026-ACC-EWHA-CHATTR-BE

Chattr 백엔드 서버 — Spring Boot + AWS DynamoDB + Cognito

## 기술 스택

- **Java 17** / Spring Boot 4
- **AWS DynamoDB** (Enhanced Client v2)
- **AWS Cognito** — JWT 기반 인증 (회원가입/로그인 API 포함)
- **AWS S3** — 파일 업로드 / Presigned URL

---

## 로컬 개발 환경 실행

### 사전 준비

- JDK 17
- AWS CLI (실제 AWS DynamoDB 사용 시)

### 1. 환경변수 설정

`.env.example`을 복사해서 `.env`를 만들고 값을 채웁니다.

```bash
cp .env.example .env
# .env 파일 편집
```

로컬에서 실제 AWS DynamoDB를 사용하는 경우 `DYNAMODB_ENDPOINT`를 비워두면 됩니다.

```bash
export $(grep -v '^#' .env | xargs)
```

### 2. DynamoDB 테이블 생성

테이블이 없는 경우 `create-tables.sh`로 생성합니다.

```bash
# 실제 AWS (리전 기본값: ap-northeast-2)
bash create-tables.sh

# 엔드포인트 직접 지정 (DynamoDB Local 등)
bash create-tables.sh --endpoint http://localhost:8000
```

> `DynamoDbTableInitializer`가 로컬 실행 시(`DYNAMODB_ENDPOINT` 세팅된 경우) 테이블을 자동 생성합니다. 이미 존재하면 무시합니다.

### 3. 앱 실행

```bash
./gradlew bootRun
```

---

## 필수 환경변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `AWS_REGION` | AWS 리전 | `ap-northeast-2` |
| `COGNITO_USER_POOL_ID` | Cognito User Pool ID | — |
| `COGNITO_CLIENT_ID` | Cognito 앱 클라이언트 ID | — |
| `COGNITO_CLIENT_SECRET` | Cognito 앱 클라이언트 시크릿 | — |
| `DYNAMODB_ENDPOINT` | DynamoDB 엔드포인트 (비우면 AWS 기본값) | — |
| `TABLE_USER` | user 테이블명 | `user` |
| `TABLE_WORKSPACE` | workspace 테이블명 | `workspace` |
| `TABLE_WORKSPACE_MEMBER` | workspace-member 테이블명 | `workspace-member` |
| `TABLE_CHANNEL` | channel 테이블명 | `channel` |
| `TABLE_CHANNEL_MEMBER` | channel-member 테이블명 | `channel-member` |
| `TABLE_DM` | dm 테이블명 | `dm` |
| `TABLE_MESSAGE` | message 테이블명 | `message` |
| `TABLE_DEVICE` | device 테이블명 | `device` |
| `S3_BUCKET` | S3 버킷명 | `chattr-dev-files` |
| `S3_PRESIGN_EXPIRY_MINUTES` | Presigned URL 유효 시간(분) | `10` |
| `S3_ENDPOINT` | S3 엔드포인트 (비우면 AWS 기본값) | — |

---

## DynamoDB 테이블 구조

| 테이블 | PK | SK | GSI |
|--------|----|----|-----|
| `user` | `id` | — | `cognito-sub-index` (PK: `cognitoSub`) |
| `workspace` | `id` | — | — |
| `workspace-member` | `workspaceId` | `userId` | `user-workspaces-index` (PK: `userId`, SK: `workspaceId`) |
| `channel` | `id` | — | `workspace-channels-index` (PK: `workspaceId`) |
| `channel-member` | `channelId` | `userId` | `user-channels-index` (PK: `userId`, SK: `channelId`) |
| `dm` | `id` | — | `dm-users-index` (PK: `userAId`, SK: `userBId`) |
| `message` | `id` | — | `room-messages-index` (PK: `roomId`, SK: `createdAt`), TTL: `ttl` |
| `device` | `userId` | `deviceId` | — |

> 테이블명은 환경변수(`TABLE_*`)로 재정의 가능합니다.

---

## 패키지 구조

```
src/main/java/com/acc/chattr/
│
├── ChattrApplication.java
│
├── config/
│   ├── CognitoConfig.java           # CognitoIdentityProviderClient 빈
│   ├── DynamoDbConfig.java          # DynamoDB 클라이언트 & 테이블 빈 등록
│   ├── DynamoDbTableInitializer.java # 로컬 실행 시 테이블 자동 생성
│   ├── OpenApiConfig.java           # Swagger / SpringDoc 설정
│   ├── S3Config.java                # S3 클라이언트 빈
│   └── SecurityConfig.java          # Cognito JWT 인증 설정
│
├── security/
│   ├── CognitoUserSyncFilter.java   # JWT 인증 후 첫 요청 시 DynamoDB 유저 자동 생성
│   └── JwtAuthenticationEntryPoint.java # 미인증 요청 시 JSON 401 반환
│
├── common/
│   ├── code/
│   │   ├── Code.java
│   │   ├── BusinessErrorCode.java   # 도메인 에러코드
│   │   └── GeneralErrorCode.java    # HTTP 공통 + Cognito 에러코드
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── GeneralException.java
│   │   └── GlobalExceptionHandler.java
│   ├── response/
│   │   ├── CursorPageResponse.java  # 커서 기반 페이지네이션 응답
│   │   ├── PageResponse.java        # 오프셋 기반 페이지네이션 응답 (소규모 데이터 전용)
│   │   └── Response.java            # 공통 응답 래퍼
│   └── util/
│       └── CursorUtils.java         # DynamoDB LastEvaluatedKey ↔ Base64 커서 변환
│
└── domain/
    ├── common/
    │   └── BaseEntity.java          # createdAt, deletedAt 공통 필드
    │
    ├── auth/
    │   ├── controller/
    │   │   └── AuthController.java  # /auth/signup, /auth/login, /auth/refresh, /auth/logout 등
    │   ├── dto/
    │   │   ├── LoginRequest.java
    │   │   ├── SignupRequest.java
    │   │   ├── RefreshRequest.java
    │   │   ├── TokenResponse.java
    │   │   ├── DeviceResponse.java
    │   │   └── RegisterDeviceRequest.java
    │   ├── entity/
    │   │   └── Device.java
    │   ├── repository/
    │   │   ├── DeviceRepository.java
    │   │   └── DeviceDynamoRepository.java
    │   └── service/
    │       ├── CognitoAuthService.java
    │       └── DeviceService.java
    │
    ├── health/
    │   ├── controller/
    │   │   └── HealthController.java
    │   └── dto/
    │       └── HealthResponse.java
    │
    ├── user/
    │   ├── controller/
    │   │   └── UserController.java
    │   ├── dto/
    │   │   └── UserResponse.java
    │   ├── entity/
    │   │   └── User.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   └── UserDynamoRepository.java
    │   └── service/
    │       └── UserService.java
    │
    ├── workspace/
    │   ├── controller/
    │   │   └── WorkspaceController.java
    │   ├── dto/
    │   │   ├── WorkspaceCreateRequest.java
    │   │   ├── WorkspaceUpdateRequest.java
    │   │   ├── WorkspaceResponse.java
    │   │   ├── WorkspaceMemberResponse.java
    │   │   ├── InviteRequest.java
    │   │   └── ChangeRoleRequest.java
    │   ├── entity/
    │   │   ├── Workspace.java
    │   │   ├── WorkspaceMember.java
    │   │   └── WorkspaceRole.java   # ADMIN | MEMBER
    │   ├── repository/
    │   │   ├── WorkspaceRepository.java
    │   │   ├── WorkspaceDynamoRepository.java
    │   │   ├── WorkspaceMemberRepository.java
    │   │   ├── WorkspaceMemberDynamoRepository.java
    │   │   └── WorkspaceRoleConverter.java
    │   └── service/
    │       └── WorkspaceService.java
    │
    ├── channel/
    │   ├── controller/
    │   │   └── ChannelController.java
    │   ├── dto/
    │   │   ├── ChannelCreateRequest.java
    │   │   ├── ChannelUpdateRequest.java
    │   │   ├── ChannelResponse.java
    │   │   ├── ChannelMemberResponse.java
    │   │   └── AddMemberRequest.java
    │   ├── entity/
    │   │   ├── Channel.java
    │   │   └── ChannelMember.java
    │   ├── repository/
    │   │   ├── ChannelRepository.java
    │   │   ├── ChannelDynamoRepository.java
    │   │   ├── ChannelMemberRepository.java
    │   │   └── ChannelMemberDynamoRepository.java
    │   └── service/
    │       └── ChannelService.java
    │
    ├── file/
    │   ├── controller/
    │   │   └── FileController.java  # S3 Presigned URL 발급
    │   ├── dto/
    │   │   ├── PresignRequest.java
    │   │   └── PresignResponse.java
    │   └── service/
    │       └── FileService.java
    │
    ├── dm/
    │   └── entity/
    │       └── Dm.java
    │
    └── message/
        ├── entity/
        │   ├── Message.java
        │   ├── MessageAttachment.java
        │   └── RoomType.java        # CHANNEL | DM
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

### 커서 기반 페이지네이션 응답

`GET /users`, `GET /channels` 등 목록 조회 API에서 사용합니다.  
DynamoDB `LastEvaluatedKey` 기반으로 동작하여 전체 데이터를 로드하지 않습니다.

```json
{
  "success": true,
  "statusCode": 200,
  "message": "OK",
  "data": {
    "content": [ ],
    "size": 20,
    "nextCursor": "eyJpZCI6ImFiYzEyMyJ9",
    "hasNext": true
  }
}
```

| 필드 | 설명 |
|------|------|
| `content` | 현재 페이지 데이터 목록 |
| `size` | 실제 반환된 항목 수 |
| `nextCursor` | 다음 페이지 요청 시 사용할 커서 (`null`이면 마지막 페이지) |
| `hasNext` | 다음 페이지 존재 여부 |

**첫 페이지 요청** — `cursor` 파라미터 생략:
```
GET /channels?workspaceId={id}&size=20
GET /users?size=20
```

**다음 페이지 요청** — 응답의 `nextCursor` 값을 그대로 전달:
```
GET /channels?workspaceId={id}&size=20&cursor=eyJpZCI6ImFiYzEyMyJ9
GET /users?size=20&cursor=eyJpZCI6ImFiYzEyMyJ9
```

> `nextCursor`가 `null`이 될 때까지 반복 요청하면 전체 데이터를 순회할 수 있습니다.

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

| 코드 | HTTP | 메시지 |
|------|------|--------|
| `USER_NOT_FOUND` | 404 | 존재하지 않는 사용자입니다. |
| `WORKSPACE_NOT_FOUND` | 404 | 존재하지 않는 워크스페이스입니다. |
| `WORKSPACE_MEMBER_NOT_FOUND` | 404 | 워크스페이스 멤버가 아닙니다. |
| `WORKSPACE_MEMBER_ALREADY_EXISTS` | 409 | 이미 워크스페이스에 가입된 사용자입니다. |
| `NOT_WORKSPACE_ADMIN` | 403 | 워크스페이스 관리자만 가능한 작업입니다. |
| `LAST_WORKSPACE_ADMIN` | 409 | 워크스페이스에 최소 한 명의 관리자가 있어야 합니다. |
| `WORKSPACE_INVITATION_NOT_FOUND` | 404 | 초대 내역이 없습니다. |
| `CHANNEL_NOT_FOUND` | 404 | 존재하지 않는 채널입니다. |
| `CHANNEL_MEMBER_NOT_FOUND` | 404 | 채널 멤버가 아닙니다. |
| `CHANNEL_MEMBER_ALREADY_EXISTS` | 409 | 이미 채널에 참여한 사용자입니다. |
| `NOT_CHANNEL_MANAGER` | 403 | 채널 관리자(생성자 또는 워크스페이스 관리자)만 가능한 작업입니다. |
| `DM_NOT_FOUND` | 404 | 존재하지 않는 DM입니다. |
| `DM_ALREADY_EXISTS` | 409 | 이미 DM이 존재합니다. |
| `DM_NOT_PARTICIPANT` | 403 | DM 참여자가 아닙니다. |
| `MESSAGE_NOT_FOUND` | 404 | 존재하지 않는 메시지입니다. |
| `MESSAGE_NOT_SENDER` | 403 | 본인이 보낸 메시지만 수정/삭제할 수 있습니다. |

### GeneralErrorCode — HTTP/공통 예외

| 코드 | HTTP | 트리거 상황 |
|------|------|------------|
| `VALIDATION_ERROR` | 400 | `@Valid` 실패, `ConstraintViolation`, 빈 값 등 |
| `INVALID_REQUEST_PARAMETER` | 400 | 파라미터 누락·타입 불일치·잘못된 커서 |
| `BAD_REQUEST` | 400 | JSON 파싱 실패 등 |
| `UNAUTHORIZED` | 401 | 미인증 접근 |
| `INVALID_TOKEN` | 401 | 유효하지 않은 토큰 |
| `TOKEN_EXPIRED` | 401 | 만료된 토큰 |
| `INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호 오류 |
| `FORBIDDEN` | 403 | 인증은 됐지만 권한 없음 |
| `NOT_FOUND` | 404 | 존재하지 않는 엔드포인트 |
| `USER_ALREADY_EXISTS` | 409 | 이미 가입된 이메일 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content-Type 불일치 |
| `TOO_MANY_REQUESTS` | 429 | Cognito 요청 횟수 초과 (`Retry-After: 30` 헤더 포함) |
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

### 회원가입 / 로그인 (백엔드 API)

```
POST /auth/signup   — 회원가입 (Cognito 계정 생성 + 즉시 활성화)
POST /auth/login    — 로그인 → idToken / accessToken / refreshToken 반환
POST /auth/refresh  — Refresh Token으로 토큰 갱신 (username 필드 필요)
POST /auth/logout   — 로그아웃 (Cognito Refresh Token 전체 무효화 + 디바이스 세션 삭제)
```

### API 요청 인증

1. `/auth/login` 응답의 **`idToken`** 을 사용합니다 (accessToken ❌)
2. 요청 헤더에 `Authorization: Bearer <idToken>` 추가
3. Spring Security가 Cognito JWKS로 서명·발급자 검증
4. `CognitoUserSyncFilter`가 첫 요청 시 DynamoDB에 유저 자동 생성

> Swagger UI: Authorize 버튼 → `Bearer <idToken>` 입력

### 토큰 갱신 시 주의

`POST /auth/refresh` 요청 시 로그인 응답의 `username` 값을 함께 전달해야 합니다.  
Cognito App Client Secret 사용 시 `SECRET_HASH` 계산에 필요합니다.

```json
{
  "refreshToken": "...",
  "username": "user@example.com"
}
```

### 로그아웃 동작

`POST /auth/logout`은 **모든 기기에서 로그아웃**합니다.

- Cognito `AdminUserGlobalSignOut` 호출 → Refresh Token 전체 무효화
- DB의 디바이스 세션 레코드 전체 삭제
- 이미 발급된 ID/Access Token은 만료 시까지 유효합니다 (Cognito 구조상 즉시 무효화 불가)
