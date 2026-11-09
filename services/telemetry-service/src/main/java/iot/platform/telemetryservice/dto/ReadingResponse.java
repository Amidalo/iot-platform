package iot.platform.telemetryservice.dto;

import java.time.Instant;

public record ReadingResponse(
        Long id,
        Long sensorId,
        Double value,
        Instant recordedAt
) {}
