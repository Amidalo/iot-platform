package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.SensorStatus;
import iot.platform.deviceservice.model.SensorType;

import java.time.Instant;

public record SensorResponse(
        Long id,
        String name,
        SensorType type,
        SensorStatus status,
        Instant updatedAt
) {}
