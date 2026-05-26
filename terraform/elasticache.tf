
resource "aws_elasticache_subnet_group" "main" {
  name       = "${local.prefix}-redis-subnet-group"
  subnet_ids = aws_subnet.private[*].id
  tags       = local.tags
}

# Primary + Replica — 서로 다른 AZ에 배치 (multi_az_enabled)
resource "aws_elasticache_replication_group" "main" {
  replication_group_id = "${local.prefix}-redis"
  description          = "Redis for ${local.prefix}"

  node_type            = var.redis_node_type
  port                 = 6379
  parameter_group_name = "default.redis7"

  num_cache_clusters         = 2 # primary 1 + replica 1
  automatic_failover_enabled = true
  multi_az_enabled           = true

  preferred_cache_cluster_azs = [
    data.aws_availability_zones.available.names[0],
    data.aws_availability_zones.available.names[1],
  ]

  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [aws_security_group.redis.id]

  at_rest_encryption_enabled = true
  transit_encryption_enabled = false

  tags = local.tags
}
