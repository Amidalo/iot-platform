package iot.platform.telemetryservice.exceptions;

public class InvalidReadingException extends RuntimeException
{
    public InvalidReadingException(String message)
    {
        super(message);
    }
}
