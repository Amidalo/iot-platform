package iot.platform.telemetryservice.dto;

import iot.platform.telemetryservice.model.MetricType;

import java.time.Instant;

public record ReadingResponse(
        Long id,
        Long sensorId,
        MetricType metric,
        Double value,
        Instant recordedAt
) {}
