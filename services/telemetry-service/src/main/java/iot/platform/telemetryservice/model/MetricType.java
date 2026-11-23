package iot.platform.telemetryservice.model;

/**
 * Метрика датчика. Значения должны совпадать с MetricType в device-service:
 * это часть контракта между сервисами.
 */
public enum MetricType {
    TEMPERATURE,
    HUMIDITY,
    PRESSURE,
    MOTION,
    LIGHT
}
