package iot.platform.deviceservice.model;

/**
 * Тип измеряемой величины. Один датчик может поддерживать несколько метрик.
 */
public enum MetricType {
    TEMPERATURE,
    HUMIDITY,
    PRESSURE,
    MOTION,
    LIGHT
}
