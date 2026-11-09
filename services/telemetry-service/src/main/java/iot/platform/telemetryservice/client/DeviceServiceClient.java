package iot.platform.telemetryservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import iot.platform.telemetryservice.exceptions.InvalidSensorReferenceException;

@Component
public class DeviceServiceClient
{
    private final WebClient webClient;

    public DeviceServiceClient(@Qualifier("deviceServiceWebClient") WebClient webClient)
    {
        this.webClient = webClient;
    }

    public void verifySensorExists(Long sensorId)
    {
        try
        {
            webClient.get()
                    .uri("/api/sensors/{id}", sensorId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }
        catch (WebClientResponseException.NotFound ex)
        {
            throw new InvalidSensorReferenceException(sensorId);
        }
    }
}
