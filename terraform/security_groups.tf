
# ──────────────────────────────────────────
# ALB SG — 인터넷 → 80/443 허용
# ──────────────────────────────────────────
resource "aws_security_group" "alb" {
  name   = "${local.prefix}-alb-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port       = local.app_port
    to_port         = local.app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_task.id]
  }
  tags = merge(local.tags, { Name = "${local.prefix}-alb-sg" })
}

# ──────────────────────────────────────────
# ECS Task SG — ALB 에서만 앱 포트 허용
# ──────────────────────────────────────────
resource "aws_security_group" "ecs_task" {
  name   = "${local.prefix}-ecs-task-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port       = local.app_port
    to_port         = local.app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  # 아웃바운드: DynamoDB/S3(VPC Endpoint), Redis, Cognito(NAT), ECR(VPC Endpoint)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(local.tags, { Name = "${local.prefix}-ecs-task-sg" })
}

# ──────────────────────────────────────────
# Redis SG — ECS Task 에서만 6379 허용
# ──────────────────────────────────────────
resource "aws_security_group" "redis" {
  name   = "${local.prefix}-redis-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_task.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = merge(local.tags, { Name = "${local.prefix}-redis-sg" })
}

# ──────────────────────────────────────────
# VPC Endpoint SG — ECS Task 에서 HTTPS 허용
# ──────────────────────────────────────────
resource "aws_security_group" "vpc_endpoint" {
  name   = "${local.prefix}-vpc-endpoint-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_task.id]
  }
  tags = merge(local.tags, { Name = "${local.prefix}-vpc-endpoint-sg" })
}
