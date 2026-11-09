package iot.platform.telemetryservice.dto;

import iot.platform.telemetryservice.model.SensorReading;

public class ReadingMapper
{
    public static SensorReading toEntity(ReadingRequest request)
    {
        return new SensorReading(request.sensorId(), request.value());
    }

    public static ReadingResponse toResponse(SensorReading reading)
    {
        return new ReadingResponse(
                reading.getId(),
                reading.getSensorId(),
                reading.getValue(),
                reading.getRecordedAt()
        );
    }
}
