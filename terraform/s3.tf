
resource "aws_s3_bucket" "uploads" {
  bucket = "${local.prefix}-files"
  tags   = local.tags
}

resource "aws_s3_bucket_public_access_block" "uploads" {
  bucket                  = aws_s3_bucket.uploads.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Presigned PUT URL 발급용 CORS 설정
resource "aws_s3_bucket_cors_configuration" "uploads" {
  bucket = aws_s3_bucket.uploads.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["*"] # 프로덕션에서는 도메인으로 제한 권장
    max_age_seconds = 3000
  }
}
