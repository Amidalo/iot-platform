package iot.platform.telemetryservice.exceptions;

import iot.platform.telemetryservice.model.MetricType;

public class UnsupportedMetricException extends RuntimeException
{
    public UnsupportedMetricException(Long sensorId, MetricType metric)
    {
        super("Датчик с id = " + sensorId + " не поддерживает метрику " + metric);
    }
}
