package iot.platform.gateway.proxy;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import iot.platform.gateway.config.GatewayProperties;

import static org.assertj.core.api.Assertions.assertThat;

class RouteResolverTest
{
    private final RouteResolver resolver = new RouteResolver(new GatewayProperties(
            null,
            null,
            0,
            null,
            List.of(
                    new GatewayProperties.Route("/api", URI.create("http://api")),
                    new GatewayProperties.Route("/api/sensors", URI.create("http://sensors")),
                    new GatewayProperties.Route("/api/readings", URI.create("http://readings")))));

    @Test
    void resolve_shouldUseLongestPrefix()
    {
        Optional<GatewayProperties.Route> route = resolver.resolve("/api/sensors/1");

        assertThat(route).isPresent();
        assertThat(route.get().target()).isEqualTo(URI.create("http://sensors"));
    }

    @Test
    void resolve_shouldMatchExactPrefix()
    {
        assertThat(resolver.resolve("/api/sensors")).isPresent();
        assertThat(resolver.resolve("/api/readings")).isPresent();
    }

    @Test
    void resolve_shouldRespectSegmentBoundary()
    {
        RouteResolver sensorsOnly = new RouteResolver(new GatewayProperties(
                null,
                null,
                0,
                null,
                List.of(
                        new GatewayProperties.Route("/api/sensors", URI.create("http://sensors")),
                        new GatewayProperties.Route("/api/readings", URI.create("http://readings")))));

        assertThat(sensorsOnly.resolve("/api/sensorsfoo")).isEmpty();
        assertThat(sensorsOnly.resolve("/api/readings-history")).isEmpty();
    }

    @Test
    void resolve_shouldReturnEmptyForUnknownPath()
    {
        assertThat(resolver.resolve("/other")).isEmpty();
        assertThat(resolver.resolve("")).isEmpty();
    }
}
