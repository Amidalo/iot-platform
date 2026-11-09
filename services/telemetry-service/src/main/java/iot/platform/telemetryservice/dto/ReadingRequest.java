package iot.platform.telemetryservice.dto;

import jakarta.validation.constraints.NotNull;

public record ReadingRequest(
    @NotNull(message = "sensorId обязателен") Long sensorId,
    @NotNull(message = "value обязательно") Double value
) {} 
