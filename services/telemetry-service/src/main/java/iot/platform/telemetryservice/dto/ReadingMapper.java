package iot.platform.telemetryservice.dto;

import iot.platform.telemetryservice.model.MetricType;
import iot.platform.telemetryservice.model.SensorReading;

import java.time.Instant;

public class ReadingMapper
{
    public static SensorReading toEntity(Long sensorId, MetricType metric, Double value, Instant recordedAt)
    {
        return new SensorReading(sensorId, metric, value, recordedAt);
    }

    public static ReadingResponse toResponse(SensorReading reading)
    {
        return new ReadingResponse(
                reading.getId(),
                reading.getSensorId(),
                reading.getMetric(),
                reading.getValue(),
                reading.getRecordedAt()
        );
    }
}
