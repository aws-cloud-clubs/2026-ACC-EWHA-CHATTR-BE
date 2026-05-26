locals {
  prefix = "chattr-${var.env}"
  tags = {
    Project     = "chattr"
    Environment = var.env
  }
}

# ──────────────────────────────────────────
# user
# PK: id
# GSI cognito-sub-index: PK cognitoSub
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "user" {
  name         = "${local.prefix}-user"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
  attribute {
    name = "cognitoSub"
    type = "S"
  }

  global_secondary_index {
    name            = "cognito-sub-index"
    hash_key        = "cognitoSub"
    projection_type = "ALL"
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# workspace
# PK: id
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "workspace" {
  name         = "${local.prefix}-workspace"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# workspace-member
# PK: workspaceId  SK: userId
# GSI user-workspaces-index: PK userId, SK workspaceId
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "workspace_member" {
  name         = "${local.prefix}-workspace-member"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "workspaceId"
  range_key    = "userId"

  attribute {
    name = "workspaceId"
    type = "S"
  }
  attribute {
    name = "userId"
    type = "S"
  }

  global_secondary_index {
    name            = "user-workspaces-index"
    hash_key        = "userId"
    range_key       = "workspaceId"
    projection_type = "ALL"
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# channel
# PK: id
# GSI workspace-channels-index: PK workspaceId
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "channel" {
  name         = "${local.prefix}-channel"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
  attribute {
    name = "workspaceId"
    type = "S"
  }

  global_secondary_index {
    name            = "workspace-channels-index"
    hash_key        = "workspaceId"
    projection_type = "ALL"
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# channel-member
# PK: channelId  SK: userId
# GSI user-channels-index: PK userId, SK channelId
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "channel_member" {
  name         = "${local.prefix}-channel-member"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "channelId"
  range_key    = "userId"

  attribute {
    name = "channelId"
    type = "S"
  }
  attribute {
    name = "userId"
    type = "S"
  }

  global_secondary_index {
    name            = "user-channels-index"
    hash_key        = "userId"
    range_key       = "channelId"
    projection_type = "ALL"
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# dm
# PK: id
# GSI dm-users-index: PK userAId, SK userBId
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "dm" {
  name         = "${local.prefix}-dm"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
  attribute {
    name = "userAId"
    type = "S"
  }
  attribute {
    name = "userBId"
    type = "S"
  }

  global_secondary_index {
    name            = "dm-users-index"
    hash_key        = "userAId"
    range_key       = "userBId"
    projection_type = "ALL"
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# message
# PK: id
# GSI room-messages-index: PK roomId, SK createdAt
# TTL: ttl (epoch seconds — set via Message.expireAfter())
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "message" {
  name         = "${local.prefix}-message"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
  attribute {
    name = "roomId"
    type = "S"
  }
  attribute {
    name = "createdAt"
    type = "S"
  }

  global_secondary_index {
    name            = "room-messages-index"
    hash_key        = "roomId"
    range_key       = "createdAt"
    projection_type = "ALL"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  tags = local.tags
}

# ──────────────────────────────────────────
# device
# PK: userId  SK: deviceId
# ──────────────────────────────────────────
resource "aws_dynamodb_table" "device" {
  name         = "${local.prefix}-device"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"
  range_key    = "deviceId"

  attribute {
    name = "userId"
    type = "S"
  }
  attribute {
    name = "deviceId"
    type = "S"
  }

  tags = local.tags
}
