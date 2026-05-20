output "table_names" {
  description = "DynamoDB table names to set as env vars in the Spring Boot app"
  value = {
    TABLE_USER             = aws_dynamodb_table.user.name
    TABLE_WORKSPACE        = aws_dynamodb_table.workspace.name
    TABLE_WORKSPACE_MEMBER = aws_dynamodb_table.workspace_member.name
    TABLE_CHANNEL          = aws_dynamodb_table.channel.name
    TABLE_CHANNEL_MEMBER   = aws_dynamodb_table.channel_member.name
    TABLE_DM               = aws_dynamodb_table.dm.name
    TABLE_MESSAGE          = aws_dynamodb_table.message.name
  }
}
