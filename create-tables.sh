#!/bin/bash
# DynamoDB 테이블 생성 스크립트 (개발 환경)
# 사용법: bash create-tables.sh [--region ap-northeast-2] [--endpoint http://localhost:8000]

REGION=${AWS_REGION:-ap-northeast-2}
ENDPOINT_FLAG=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --region) REGION="$2"; shift 2 ;;
    --endpoint) ENDPOINT_FLAG="--endpoint-url $2"; shift 2 ;;
    *) shift ;;
  esac
done

CMD="aws dynamodb $ENDPOINT_FLAG --region $REGION"
echo "대상: region=$REGION ${ENDPOINT_FLAG}"

create_table() {
  local name=$1
  echo -n "[$name] 생성 중... "
  shift
  if $CMD create-table --table-name "$name" "$@" \
    --billing-mode PAY_PER_REQUEST > /dev/null 2>&1; then
    echo "완료"
  else
    echo "실패 또는 이미 존재"
  fi
}

# ─── user ────────────────────────────────────────────────────────
create_table user \
  --attribute-definitions \
    AttributeName=id,AttributeType=S \
    AttributeName=cognitoSub,AttributeType=S \
  --key-schema \
    AttributeName=id,KeyType=HASH \
  --global-secondary-indexes '[
    {
      "IndexName": "cognito-sub-index",
      "KeySchema": [{"AttributeName":"cognitoSub","KeyType":"HASH"}],
      "Projection": {"ProjectionType":"ALL"}
    }
  ]'

# ─── workspace ───────────────────────────────────────────────────
create_table workspace \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH

# ─── workspace-member ────────────────────────────────────────────
create_table workspace-member \
  --attribute-definitions \
    AttributeName=workspaceId,AttributeType=S \
    AttributeName=userId,AttributeType=S \
  --key-schema \
    AttributeName=workspaceId,KeyType=HASH \
    AttributeName=userId,KeyType=RANGE \
  --global-secondary-indexes '[
    {
      "IndexName": "user-workspaces-index",
      "KeySchema": [
        {"AttributeName":"userId","KeyType":"HASH"},
        {"AttributeName":"workspaceId","KeyType":"RANGE"}
      ],
      "Projection": {"ProjectionType":"ALL"}
    }
  ]'

# ─── channel ─────────────────────────────────────────────────────
create_table channel \
  --attribute-definitions \
    AttributeName=id,AttributeType=S \
    AttributeName=workspaceId,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --global-secondary-indexes '[
    {
      "IndexName": "workspace-channels-index",
      "KeySchema": [{"AttributeName":"workspaceId","KeyType":"HASH"}],
      "Projection": {"ProjectionType":"ALL"}
    }
  ]'

# ─── channel-member ──────────────────────────────────────────────
create_table channel-member \
  --attribute-definitions \
    AttributeName=channelId,AttributeType=S \
    AttributeName=userId,AttributeType=S \
  --key-schema \
    AttributeName=channelId,KeyType=HASH \
    AttributeName=userId,KeyType=RANGE \
  --global-secondary-indexes '[
    {
      "IndexName": "user-channels-index",
      "KeySchema": [
        {"AttributeName":"userId","KeyType":"HASH"},
        {"AttributeName":"channelId","KeyType":"RANGE"}
      ],
      "Projection": {"ProjectionType":"ALL"}
    }
  ]'

# ─── dm ──────────────────────────────────────────────────────────
create_table dm \
  --attribute-definitions \
    AttributeName=id,AttributeType=S \
    AttributeName=userAId,AttributeType=S \
    AttributeName=userBId,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --global-secondary-indexes '[
    {
      "IndexName": "dm-users-index",
      "KeySchema": [
        {"AttributeName":"userAId","KeyType":"HASH"},
        {"AttributeName":"userBId","KeyType":"RANGE"}
      ],
      "Projection": {"ProjectionType":"ALL"}
    }
  ]'

# ─── message ─────────────────────────────────────────────────────
create_table message \
  --attribute-definitions \
    AttributeName=id,AttributeType=S \
    AttributeName=roomId,AttributeType=S \
    AttributeName=createdAt,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --global-secondary-indexes '[
    {
      "IndexName": "room-messages-index",
      "KeySchema": [
        {"AttributeName":"roomId","KeyType":"HASH"},
        {"AttributeName":"createdAt","KeyType":"RANGE"}
      ],
      "Projection": {"ProjectionType":"ALL"}
    }
  ]'

# ─── device ──────────────────────────────────────────────────────
create_table device \
  --attribute-definitions \
    AttributeName=userId,AttributeType=S \
    AttributeName=deviceId,AttributeType=S \
  --key-schema \
    AttributeName=userId,KeyType=HASH \
    AttributeName=deviceId,KeyType=RANGE

echo ""
echo "모든 테이블 생성 완료"
