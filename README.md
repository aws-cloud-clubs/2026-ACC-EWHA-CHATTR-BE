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

## 인증 흐름

1. 클라이언트가 Cognito에서 **Access Token** 발급
2. `Authorization: Bearer <Access Token>` 헤더로 요청
3. Spring Security가 Cognito JWKS로 서명 검증
4. `CognitoUserSyncFilter`가 첫 로그인 시 DynamoDB에 유저 자동 생성
