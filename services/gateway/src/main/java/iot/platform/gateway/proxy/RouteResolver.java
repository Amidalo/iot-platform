package iot.platform.gateway.proxy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import iot.platform.gateway.config.GatewayProperties;

@Component
public class RouteResolver
{
    private final List<GatewayProperties.Route> routes;

    public RouteResolver(GatewayProperties properties)
    {
        this.routes = properties.routes().stream()
                .sorted(Comparator.comparingInt((GatewayProperties.Route route) -> route.prefix().length()).reversed())
                .toList();
    }

    public Optional<GatewayProperties.Route> resolve(String path)
    {
        if (path == null || path.isEmpty())
        {
            return Optional.empty();
        }

        return routes.stream()
                .filter(route -> path.equals(route.prefix()) || path.startsWith(route.prefix() + "/"))
                .findFirst();
    }
}
