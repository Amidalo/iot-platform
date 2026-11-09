package iot.platform.telemetryservice.exceptions;

public class InvalidSensorReferenceException extends RuntimeException
{
    public InvalidSensorReferenceException(Long sensorId)
    {
        super("Датчик с id = " + sensorId + " не существует в device-service");
    }
}
