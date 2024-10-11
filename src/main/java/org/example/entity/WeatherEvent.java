package org.example.entity;

public record WeatherEvent(
    String locationName, Double temperature, Long timestamp, Double longitude, Double latitude) {}
