package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.Sensor;

import java.util.LinkedHashSet;
import java.util.Set;

public class SensorMapper
{
    public static Sensor toEntity(SensorRequest request)
    {
        return new Sensor(request.name(), request.metrics(), request.status());
    }

    public static SensorResponse toResponse(Sensor sensor)
    {
        return new SensorResponse(
                sensor.getId(),
                sensor.getName(),
                copyMetrics(sensor.getMetrics()),
                sensor.getStatus(),
                sensor.getUpdatedAt()
        );
    }

    private static Set<MetricType> copyMetrics(Set<MetricType> metrics)
    {
        return metrics == null ? Set.of() : new LinkedHashSet<>(metrics);
    }
}
