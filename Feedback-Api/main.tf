terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_dynamodb_table" "feedbacks" {
  name           = "feedbacks"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }
}

resource "aws_sns_topic" "critical_feedbacks" {
  name = "critical-feedbacks"
}

resource "aws_s3_bucket" "feedback_reports" {
  bucket = "feedback-reports-bucket" # Choose a unique name

  tags = {
    Name        = "Feedback Reports"
    Environment = "Prod"
  }
}

resource "aws_iam_role" "lambda_role" {
  name = "lambda_execution_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_policy" "lambda_policy" {
  name        = "lambda_policy"
  description = "Policy for Lambda functions"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "dynamodb:PutItem",
          "dynamodb:Scan",
          "sns:Publish",
          "s3:PutObject",
          "ses:SendEmail",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Effect   = "Allow"
        Resource = [
          aws_dynamodb_table.feedbacks.arn,
          aws_sns_topic.critical_feedbacks.arn,
          "${aws_s3_bucket.feedback_reports.arn}/*",
          "arn:aws:ses:us-east-1:*:identity/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_policy_attachment" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_policy.arn
}

# API Gateway, Lambda function resources would be defined here as well
# For brevity, I'm focusing on the core resources.
# You would use aws_lambda_function, aws_api_gateway_rest_api, etc.
