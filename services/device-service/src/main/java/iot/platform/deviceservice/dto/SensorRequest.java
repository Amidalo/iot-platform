package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.SensorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record SensorRequest(
        @NotBlank(message = "Название датчика обязательно") String name,
        @NotEmpty(message = "Нужна хотя бы одна метрика") Set<MetricType> metrics,
        @NotNull(message = "Статус датчика обязателен") SensorStatus status
) {}
