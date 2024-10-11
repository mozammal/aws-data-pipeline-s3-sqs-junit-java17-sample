package org.example.handler;

import org.example.entity.WeatherEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SystemStubsExtension.class)
public class EventProducerUnitTest {
  @SystemStub
  private final EnvironmentVariables variables =
      new EnvironmentVariables("QUEUE_NAME", "fake_queue");

  @Test
  public void getWeatherEvents_whenValidEvents_returns_successfully() {
    InputStream inputStream = getClass().getResourceAsStream("/bulk_data.json");
    System.setProperty("QUEUE_NAME", "mock_queue");

    var eventProducer = new EventProducer(null, null);
    List<WeatherEvent> weatherEvents = eventProducer.getWeatherEvents(inputStream);

    assertEquals(3, weatherEvents.size());
    assertEquals("Brooklyn, NY", weatherEvents.get(0).locationName());
    assertEquals(91.0, weatherEvents.get(0).temperature(), 0.0);
    assertEquals(1564428897L, weatherEvents.get(0).timestamp(), 0);
    assertEquals(40.7, weatherEvents.get(0).latitude(), 0.0);
    assertEquals(-73.99, weatherEvents.get(0).longitude(), 0.0);

    assertEquals("Oxford, UK", weatherEvents.get(1).locationName());
    assertEquals(64.0, weatherEvents.get(1).temperature(), 0.0);
    assertEquals(1564428897L, weatherEvents.get(1).timestamp(), 0);
    assertEquals(51.75, weatherEvents.get(1).latitude(), 0.0);
    assertEquals(-1.25, weatherEvents.get(1).longitude(), 0.0);

    assertEquals("Charlottesville, VA", weatherEvents.get(2).locationName());
    assertEquals(87.0, weatherEvents.get(2).temperature(), 0.0);
    assertEquals(1564428897L, weatherEvents.get(2).timestamp(), 0);
    assertEquals(38.02, weatherEvents.get(2).latitude(), 0.0);
    assertEquals(-78.47, weatherEvents.get(2).longitude(), 0.0);
  }

  @Test
  public void getWeatherEvents_whenInvalidEvents_throws_exception() {
    InputStream inputStream = getClass().getResourceAsStream("/bad_data.json");
    var eventProducerLambda = new EventProducer(null, null);
    Assertions.assertThrows(
        RuntimeException.class, () -> eventProducerLambda.getWeatherEvents(inputStream));
  }

  @Test
  public void eventToString_whenValidEvent_returns_successfully() {
    WeatherEvent weatherEvent = new WeatherEvent("Foo, Bar", 32.0, 0L, -100.0, 100.0);

    var eventProducer = new EventProducer(null, null);
    String message = eventProducer.eventToString(weatherEvent);

    assertEquals(
        "{\"locationName\":\"Foo, Bar\",\"temperature\":32.0,\"timestamp\":0,\"longitude\":-100.0,\"latitude\":100.0}",
        message);
  }
}
