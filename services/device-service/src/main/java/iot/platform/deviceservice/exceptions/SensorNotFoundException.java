package iot.platform.deviceservice.exceptions;

public class SensorNotFoundException extends RuntimeException
{
    public SensorNotFoundException(Long sensorId)
    {
        super("Датчик с id = " + sensorId + " не найден");
    }
}
