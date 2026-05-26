
# ──────────────────────────────────────────
# ECS Cluster
# ──────────────────────────────────────────
resource "aws_ecs_cluster" "main" {
  name = "${local.prefix}-cluster"
  tags = local.tags
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.prefix}-app"
  retention_in_days = 7
  tags              = local.tags
}

# ──────────────────────────────────────────
# IAM — Task Execution Role (ECR pull, CloudWatch logs)
# ──────────────────────────────────────────
resource "aws_iam_role" "ecs_exec" {
  name = "${local.prefix}-ecs-exec-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
  tags = local.tags
}

resource "aws_iam_role_policy_attachment" "ecs_exec" {
  role       = aws_iam_role.ecs_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ──────────────────────────────────────────
# IAM — Task Role (DynamoDB, S3 접근)
# ──────────────────────────────────────────
resource "aws_iam_role" "ecs_task" {
  name = "${local.prefix}-ecs-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
  tags = local.tags
}

resource "aws_iam_role_policy" "ecs_task" {
  name = "${local.prefix}-ecs-task-policy"
  role = aws_iam_role.ecs_task.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem", "dynamodb:PutItem", "dynamodb:UpdateItem",
          "dynamodb:DeleteItem", "dynamodb:Query", "dynamodb:Scan",
          "dynamodb:BatchGetItem", "dynamodb:BatchWriteItem"
        ]
        Resource = "arn:aws:dynamodb:${var.aws_region}:*:table/${local.prefix}-*"
      },
      {
        Effect   = "Allow"
        Action   = ["s3:PutObject", "s3:GetObject"]
        Resource = "${aws_s3_bucket.uploads.arn}/*"
      }
    ]
  })
}

# ──────────────────────────────────────────
# Task Definition
# ──────────────────────────────────────────
resource "aws_ecs_task_definition" "app" {
  family                   = "${local.prefix}-app"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_cpu
  memory                   = var.ecs_memory
  execution_role_arn       = aws_iam_role.ecs_exec.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "app"
    image     = "${aws_ecr_repository.app.repository_url}:latest"
    essential = true
    portMappings = [{
      containerPort = local.app_port
      protocol      = "tcp"
    }]
    environment = [
      { name = "AWS_REGION",             value = var.aws_region },
      { name = "COGNITO_USER_POOL_ID",   value = var.cognito_user_pool_id },
      { name = "S3_BUCKET",              value = aws_s3_bucket.uploads.bucket },
      { name = "TABLE_USER",             value = aws_dynamodb_table.user.name },
      { name = "TABLE_WORKSPACE",        value = aws_dynamodb_table.workspace.name },
      { name = "TABLE_WORKSPACE_MEMBER", value = aws_dynamodb_table.workspace_member.name },
      { name = "TABLE_CHANNEL",          value = aws_dynamodb_table.channel.name },
      { name = "TABLE_CHANNEL_MEMBER",   value = aws_dynamodb_table.channel_member.name },
      { name = "TABLE_DM",               value = aws_dynamodb_table.dm.name },
      { name = "TABLE_MESSAGE",          value = aws_dynamodb_table.message.name },
      { name = "TABLE_DEVICE",           value = aws_dynamodb_table.device.name },
      { name = "REDIS_HOST",             value = aws_elasticache_replication_group.main.primary_endpoint_address },
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])

  tags = local.tags
}

# ──────────────────────────────────────────
# ECS Service — 프라이빗 서브넷, multi-AZ
# ──────────────────────────────────────────
resource "aws_ecs_service" "app" {
  name            = "${local.prefix}-app"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.ecs_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_task.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "app"
    container_port   = local.app_port
  }

  depends_on = [aws_lb_listener.http]
  tags       = local.tags
}
