package org.example.handler;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.entity.WeatherEvent;

public final class EventProducer {
  private static final Logger logger = LogManager.getLogger(EventProducer.class);
  private static final String QUEUE_NAME = "QUEUE_NAME";
  private final AmazonS3 s3Client;
  private final AmazonSQS amazonSQS;
  private final String queueName;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public EventProducer() {
    this(
        AmazonSQSClientBuilder.standard().withRegion(System.getenv("AWS_REGION_NAME")).build(),
        AmazonS3ClientBuilder.defaultClient());
  }

  public EventProducer(AmazonSQS amazonSQS, AmazonS3 s3Client) {
    this.amazonSQS = amazonSQS;
    this.s3Client = s3Client;
    this.queueName = System.getenv(QUEUE_NAME);

    if (this.queueName == null) {
      throw new RuntimeException(String.format("%s is missing", QUEUE_NAME));
    }
  }

  public void handleRequest(S3Event s3Event) {
    List<WeatherEvent> events =
        s3Event.getRecords().stream()
            .map(this::getObjectFromS3Bucket)
            .map(this::getWeatherEvents)
            .flatMap(List::stream)
            .toList();

    events.stream().map(this::eventToString).forEach(this::publishToSQS);

    System.out.println("Published " + events.size() + " weather events to SQS");
  }

  List<WeatherEvent> getWeatherEvents(InputStream input) {
    try (InputStream is = input) {
      return List.of(objectMapper.readValue(is, WeatherEvent[].class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private InputStream getObjectFromS3Bucket(S3EventNotification.S3EventNotificationRecord record) {
    String bucket = record.getS3().getBucket().getName();
    String key = record.getS3().getObject().getKey();
    return s3Client.getObject(bucket, key).getObjectContent();
  }

  private void publishToSQS(String message) {
    logger.info("Sending event to SQS:");
    SendMessageRequest sendMessageRequest = getSendMessageRequest(message);
    amazonSQS.sendMessage(sendMessageRequest);
  }

  SendMessageRequest getSendMessageRequest(String message) {
    return new SendMessageRequest()
        .withMessageBody(message)
        .withQueueUrl(amazonSQS.getQueueUrl(queueName).getQueueUrl())
        .withDelaySeconds(3);
  }

  String eventToString(WeatherEvent weatherEvent) {
    try {
      return objectMapper.writeValueAsString(weatherEvent);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
