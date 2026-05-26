output "alb_dns" {
  description = "ALB DNS — 앱 접근 URL"
  value       = "http://${aws_lb.main.dns_name}"
}

output "ecr_repository_url" {
  description = "ECR 이미지 push URL"
  value       = aws_ecr_repository.app.repository_url
}

output "redis_primary_endpoint" {
  description = "ElastiCache Redis primary endpoint"
  value       = aws_elasticache_replication_group.main.primary_endpoint_address
}

output "s3_bucket" {
  description = "S3 업로드 버킷 이름"
  value       = aws_s3_bucket.uploads.bucket
}

output "table_names" {
  description = "DynamoDB 테이블 이름 (Spring Boot 환경변수용)"
  value = {
    TABLE_USER             = aws_dynamodb_table.user.name
    TABLE_WORKSPACE        = aws_dynamodb_table.workspace.name
    TABLE_WORKSPACE_MEMBER = aws_dynamodb_table.workspace_member.name
    TABLE_CHANNEL          = aws_dynamodb_table.channel.name
    TABLE_CHANNEL_MEMBER   = aws_dynamodb_table.channel_member.name
    TABLE_DM               = aws_dynamodb_table.dm.name
    TABLE_MESSAGE          = aws_dynamodb_table.message.name
    TABLE_DEVICE           = aws_dynamodb_table.device.name
  }
}
