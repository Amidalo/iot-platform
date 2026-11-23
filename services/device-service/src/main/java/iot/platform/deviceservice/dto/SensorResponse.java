package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.SensorStatus;

import java.time.Instant;
import java.util.Set;

public record SensorResponse(
        Long id,
        String name,
        Set<MetricType> metrics,
        SensorStatus status,
        Instant updatedAt
) {}
