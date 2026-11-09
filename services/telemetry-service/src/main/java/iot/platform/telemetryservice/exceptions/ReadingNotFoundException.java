package iot.platform.telemetryservice.exceptions;

public class ReadingNotFoundException extends RuntimeException
{
    public ReadingNotFoundException(Long id)
    {
        super("Показание с id = " + id + " не найдено");
    }
}
