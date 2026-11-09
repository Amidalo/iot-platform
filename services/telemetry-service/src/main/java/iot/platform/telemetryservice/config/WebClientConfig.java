package iot.platform.telemetryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig
{
    @Value("${app.device-service.url}")
    private String deviceServiceUrl;

    @Bean
    public WebClient deviceServiceWebClient()
    {
        return WebClient.builder()
                .baseUrl(deviceServiceUrl)
                .build();
    }
}
