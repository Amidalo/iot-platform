package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.Sensor;

public class SensorMapper
{
    public static Sensor toEntity(SensorRequest request)
    {
        return new Sensor(request.name(), request.type(), request.status());
    }

    public static SensorResponse toResponse(Sensor sensor)
    {
        return new SensorResponse(
                sensor.getId(),
                sensor.getName(),
                sensor.getType(),
                sensor.getStatus(),
                sensor.getUpdatedAt()
        );
    }
}