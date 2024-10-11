package org.example.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.WeatherEvent;

public final class EventConsumer {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public void handleRequest(SQSEvent event) {
    event.getRecords().stream()
        .map(SQSEvent.SQSMessage::getBody)
        .map(this::toWeatherEvent)
        .forEach(this::logWeatherEvent);
  }

  void logWeatherEvent(WeatherEvent weatherEvent) {
    System.out.println("Received weather event from SQS:");
    System.out.println(weatherEvent);
  }

  WeatherEvent toWeatherEvent(String message) {
    try {
      return objectMapper.readValue(message, WeatherEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
