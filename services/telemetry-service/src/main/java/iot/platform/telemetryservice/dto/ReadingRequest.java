package iot.platform.telemetryservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Пакет показаний одного датчика: одна метрика может отсутствовать,
 * но хотя бы одна должна быть передана.
 * Ключи — имена MetricType (регистр не важен), значения — числа.
 */
public record ReadingRequest(
        @NotNull(message = "sensorId обязателен") Long sensorId,
        @NotEmpty(message = "Нужна хотя бы одна метрика") Map<String, Double> metrics
) {}
