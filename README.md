# aws-s3-sns-junit-java17-sample

This project contains source code and supporting files for an aws lambda application
that you can deploy with Terraform. This time we'll use aws command line to upload a list of
weather events in a JSON file to S3. The producer then consume the s3-events and publish the records
to a SQS queue. "A Lambda consumer consumes the messages from the SQS queue and then logs them to AWS CloudWatch Logs

It includes the following files and folders.

- /src/main/org/java/example - Code for the application's Lambda function.
- /src/test/java/org/example - Unit tests for the application code.
- /terraform - terraform IaC files for the application's AWS resources.

The application leverages multiple AWS resources, including Lambda functions, SQS, and S3.

## Deploy the sample application

You may need the following tools.

* java17 - [Install the Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Terraform - [Install Terraform](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli)

* To build and deploy your application for the first time, run the following in your shell:

```bash
export/set AWS_ACCESS_KEY_ID=ur_aws_access_key
export/set AWS_SECRET_ACCESS_KEY=ur_aws_secret
```

* create two S3 buckets— one for build artifacts and another for test JSON file

```bash
aws s3 mb s3://aws-bucket-name-for-lambda-zip-moz
aws s3 mb s3://folder-to-upload-test-data-file-moz
```

* Run the following commands to build the artifact

```bash
git clone https://github.com/mozammal/aws-data-pipeline-s3-sqs-junit-java17-sample.git
cd aws-data-pipeline-s3-sqs-junit-java17-sample
mvn clean package
```

* Navigate to the Terraform folder and deploy the application on AWS using the following commands

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

* Now upload the test JSON file and monitor the logged events in CloudWatch

```bash
cd ..
aws s3 cp sampledata.json s3://folder-to-upload-test-data-file-moz/sampledata.json
```

## Cleanup

To delete the sample application that you created, you can run the following:

```bash
cd terraform
terraform  destroy
```
