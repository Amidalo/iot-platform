package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.SensorStatus;
import iot.platform.deviceservice.model.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SensorRequest(
        @NotBlank(message = "Название датчика обязательно") String name,
        @NotNull SensorType type,
        @NotNull SensorStatus status
) {}