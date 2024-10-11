package org.example.handler;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SystemStubsExtension.class)
public class EventProducerFunctionalTest {
  @SystemStub
  private final EnvironmentVariables variables =
      new EnvironmentVariables("QUEUE_NAME", "fake_queue");

  @Test
  public void handleRequest_whenS3ValidEventsTriggered_publishToSQS_successfully() {
    AmazonSQS sqs = Mockito.mock(AmazonSQS.class);
    AmazonS3 s3 = Mockito.mock(AmazonS3.class);
    final PojoSerializer<S3Event> s3EventSerializer =
        LambdaEventSerializers.serializerFor(S3Event.class, ClassLoader.getSystemClassLoader());

    S3Event s3Event = s3EventSerializer.fromJson(getClass().getResourceAsStream("/s3_event.json"));
    String bucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
    String key = s3Event.getRecords().get(0).getS3().getObject().getKey();

    S3Object s3Object = new S3Object();
    s3Object.setObjectContent(getClass().getResourceAsStream(String.format("/%s", key)));
    when(s3.getObject(bucket, key)).thenReturn(s3Object);

    var eventProducer = new EventProducer(sqs, s3);
    GetQueueUrlResult mockResult = new GetQueueUrlResult().withQueueUrl("fake-queue");
    when(sqs.getQueueUrl(anyString())).thenReturn(mockResult);
    eventProducer.handleRequest(s3Event);
    String queueName = System.getenv("QUEUE_NAME");

    ArgumentCaptor<SendMessageRequest> sendMessageRequestArgumentCaptor =
        ArgumentCaptor.forClass(SendMessageRequest.class);
    Mockito.verify(sqs, Mockito.times(3)).sendMessage(sendMessageRequestArgumentCaptor.capture());
    List<SendMessageRequest> sendMessageRequests = sendMessageRequestArgumentCaptor.getAllValues();
    List<String> messageBody =
        sendMessageRequests.stream().map(SendMessageRequest::getMessageBody).toList();

    assertIterableEquals(
        List.of(
            "{\"locationName\":\"Brooklyn, NY\",\"temperature\":91.0,\"timestamp\":1564428897,\"longitude\":-73.99,\"latitude\":40.7}",
            "{\"locationName\":\"Oxford, UK\",\"temperature\":64.0,\"timestamp\":1564428898,\"longitude\":-1.25,\"latitude\":51.75}",
            "{\"locationName\":\"Charlottesville, VA\",\"temperature\":87.0,\"timestamp\":1564428899,\"longitude\":-78.47,\"latitude\":38.02}"),
        messageBody);
  }

  @Test
  public void handleRequest_whenS3InvalidEventsTriggered_throws_exception() {
    AmazonSQS sqs = Mockito.mock(AmazonSQS.class);
    AmazonS3 s3 = Mockito.mock(AmazonS3.class);
    final PojoSerializer<S3Event> s3EventSerializer =
        LambdaEventSerializers.serializerFor(S3Event.class, ClassLoader.getSystemClassLoader());

    S3Event s3Event =
        s3EventSerializer.fromJson(getClass().getResourceAsStream("/s3_event_bad_data.json"));
    String bucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
    String key = s3Event.getRecords().get(0).getS3().getObject().getKey();

    S3Object s3Object = new S3Object();
    s3Object.setObjectContent(getClass().getResourceAsStream(String.format("/%s", key)));
    when(s3.getObject(bucket, key)).thenReturn(s3Object);

    var eventProducer = new EventProducer(sqs, s3);
    Assertions.assertThrows(RuntimeException.class, () -> eventProducer.handleRequest(s3Event));
  }

  @Test
  public void whenSQSQueue_missing_throws_exception() {
    AmazonSQS sqs = Mockito.mock(AmazonSQS.class);
    AmazonS3 s3 = Mockito.mock(AmazonS3.class);

    variables.set("QUEUE_NAME", null);

    Assertions.assertThrows(RuntimeException.class, () -> new EventProducer(sqs, s3));
  }
}
