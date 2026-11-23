package iot.platform.gateway.config;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(
        Duration connectTimeout,
        Duration requestTimeout,
        long maxBodyBytes,
        Cors cors,
        List<Route> routes)
{
    public GatewayProperties
    {
        if (connectTimeout == null)
        {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (requestTimeout == null)
        {
            requestTimeout = Duration.ofSeconds(10);
        }
        if (maxBodyBytes <= 0)
        {
            maxBodyBytes = 1024 * 1024;
        }
        if (cors == null)
        {
            cors = new Cors(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        }
        routes = routes == null ? List.of() : List.copyOf(routes);
    }

    public record Cors(List<String> allowedOrigins)
    {
        public Cors
        {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        }
    }

    public record Route(String prefix, URI target)
    {
        public Route
        {
            if (prefix == null || !prefix.startsWith("/"))
            {
                throw new IllegalArgumentException("gateway route prefix must start with /");
            }
            if (target == null)
            {
                throw new IllegalArgumentException("gateway route target must not be null");
            }
        }
    }
}
