package iot.platform.telemetryservice.client;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import iot.platform.telemetryservice.exceptions.InvalidSensorReferenceException;
import iot.platform.telemetryservice.model.MetricType;

@Component
public class DeviceServiceClient
{
    private final WebClient webClient;

    public DeviceServiceClient(@Qualifier("deviceServiceWebClient") WebClient webClient)
    {
        this.webClient = webClient;
    }

    /**
     * Возвращает метрики, которые поддерживает датчик.
     * 404 от device-service означает, что датчика не существует.
     */
    public Set<MetricType> getMetricTypes(Long sensorId)
    {
        try
        {
            List<MetricType> metrics = webClient.get()
                    .uri("/api/sensors/{id}/metrics", sensorId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<MetricType>>() {})
                    .block();

            return metrics == null ? Set.of() : new LinkedHashSet<>(metrics);
        }
        catch (WebClientResponseException.NotFound ex)
        {
            throw new InvalidSensorReferenceException(sensorId);
        }
    }
}
