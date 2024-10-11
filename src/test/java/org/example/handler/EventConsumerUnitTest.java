package org.example.handler;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.example.entity.WeatherEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventConsumerUnitTest {
  @Test
  public void toWeatherEvent_whenValidEvents_returns_successfully() {
    String message =
        "{\"locationName\":\"Brooklyn, NY\",\"temperature\":91.0,\"timestamp\":1564428897,\"longitude\":-73.99,\"latitude\":40.7}";

    var eventConsumer = new EventConsumer();
    WeatherEvent weatherEvent = eventConsumer.toWeatherEvent(message);

    Assertions.assertEquals("Brooklyn, NY", weatherEvent.locationName());
    Assertions.assertEquals(91.0, weatherEvent.temperature(), 0.0);
    Assertions.assertEquals(1564428897L, weatherEvent.timestamp(), 0);
    Assertions.assertEquals(40.7, weatherEvent.latitude(), 0.0);
    Assertions.assertEquals(-73.99, weatherEvent.longitude(), 0.0);
  }

  @Test
  public void toWeatherEvent_whenInValidEvents_returns_successfully() {
    String message =
        "{\"locationName\":\"Brooklyn, NY\",\"temperature\":91.0,\"timestamp\":\"Wrong data type\",\"longitude\":-73.99,\"latitude\":40.7}";
    var eventConsumer = new EventConsumer();

    Assertions.assertThrows(RuntimeException.class, () -> eventConsumer.toWeatherEvent(message));
  }

  @Test
  public void logWeatherEvent_whenValidEvents_returns_successfully() throws Exception {
    WeatherEvent weatherEvent = new WeatherEvent("Foo, Bar", 32.0, 0L, -100.0, 100.0);
    String EOL = System.getProperty("line.separator");

    String actual =
        SystemLambda.tapSystemOut(
            () -> {
              var eventConsumer = new EventConsumer();
              eventConsumer.logWeatherEvent(weatherEvent);
            });

    Assertions.assertEquals(
        "Received weather event from SQS:"
            + EOL
            + "WeatherEvent[locationName=Foo, Bar, temperature=32.0, timestamp=0, longitude=-100.0, latitude=100.0]"
            + EOL,
        actual);
  }
}
