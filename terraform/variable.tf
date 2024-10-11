variable "aws_region" {
  description = "The aws region"
  type        = string
  default     = "eu-north-1"
}

variable "zip_path" {
  description = "Zip file dployed as a lambda function"
  type        = string
  default     = "target/aws-data-pipeline-s3-sqs-junit-java17-sample.zip"
}

variable "aws_bucket_name_for_lambda_zip" {
  description = "Name of the aws bucket to upload lambda zip file"
  type        = string
  default     = "aws-bucket-name-for-lambda-zip-moz"
}

variable "folder_to_upload_test_data_file" {
  description = "folder for uploading test data files"
  default     = "folder-to-upload-test-data-file-moz"
}