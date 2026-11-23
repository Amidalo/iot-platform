package iot.platform.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer
{
    private final GatewayProperties properties;

    public CorsConfig(GatewayProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        String[] origins = properties.cors().allowedOrigins().toArray(String[]::new);
        if (origins.length == 0)
        {
            return;
        }

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "X-Request-Id", "If-Match")
                .exposedHeaders("X-Request-Id")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
