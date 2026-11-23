package iot.platform.gateway.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iot.platform.gateway.config.GatewayProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@RestController
public class GatewayController
{
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length");

    private static final Set<String> GATEWAY_OWNED_RESPONSE_HEADERS = Set.of(
            "access-control-allow-origin",
            "access-control-allow-methods",
            "access-control-allow-headers",
            "access-control-allow-credentials",
            "access-control-expose-headers",
            "access-control-max-age",
            "x-request-id");

    private final HttpClient httpClient;
    private final RouteResolver routeResolver;
    private final GatewayProperties properties;
    private final ObjectMapper objectMapper;

    public GatewayController(RouteResolver routeResolver,
                             GatewayProperties properties,
                             ObjectMapper objectMapper)
    {
        this.routeResolver = routeResolver;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @RequestMapping("/api/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String requestId = resolveRequestId(request);
        response.setHeader("X-Request-Id", requestId);

        String path = request.getRequestURI().substring(request.getContextPath().length());
        Optional<GatewayProperties.Route> route = routeResolver.resolve(path);
        if (route.isEmpty())
        {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "ROUTE_NOT_FOUND",
                    "Маршрут не найден", requestId);
            return;
        }

        long declaredLength = request.getContentLengthLong();
        if (declaredLength > properties.maxBodyBytes())
        {
            writeError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "PAYLOAD_TOO_LARGE",
                    "Тело запроса слишком большое", requestId);
            return;
        }

        byte[] body = readBody(request);
        if (body.length > properties.maxBodyBytes())
        {
            writeError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "PAYLOAD_TOO_LARGE",
                    "Тело запроса слишком большое", requestId);
            return;
        }

        URI target = buildTarget(route.get(), path, request.getQueryString());
        HttpRequest upstreamRequest = buildRequest(request, target, body, requestId);

        long startedAt = System.nanoTime();
        try
        {
            HttpResponse<InputStream> upstream = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofInputStream());
            copyResponse(request, response, upstream);
            log.debug("{} {} -> {} in {} ms (requestId={})",
                    request.getMethod(), request.getRequestURI(), upstream.statusCode(),
                    Duration.ofNanos(System.nanoTime() - startedAt).toMillis(), requestId);
        }
        catch (HttpTimeoutException ex)
        {
            log.warn("Upstream timeout for {} {} (requestId={})", request.getMethod(), request.getRequestURI(), requestId);
            writeError(response, HttpServletResponse.SC_GATEWAY_TIMEOUT, "UPSTREAM_TIMEOUT",
                    "Upstream не ответил вовремя", requestId);
        }
        catch (IOException ex)
        {
            log.warn("Upstream unavailable for {} {}: {} (requestId={})",
                    request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName(), requestId);
            writeError(response, HttpServletResponse.SC_BAD_GATEWAY, "UPSTREAM_UNAVAILABLE",
                    "Upstream недоступен", requestId);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            writeError(response, HttpServletResponse.SC_BAD_GATEWAY, "UPSTREAM_UNAVAILABLE",
                    "Запрос к upstream прерван", requestId);
        }
    }

    private byte[] readBody(HttpServletRequest request) throws IOException
    {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method))
        {
            return new byte[0];
        }

        long max = properties.maxBodyBytes();
        int limit = (int) Math.min(max + 1, Integer.MAX_VALUE);
        try (InputStream input = request.getInputStream())
        {
            return input.readNBytes(limit);
        }
    }

    private HttpRequest buildRequest(HttpServletRequest request, URI target, byte[] body, String requestId)
    {
        String method = request.getMethod();
        HttpRequest.BodyPublisher publisher = ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method))
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder(target)
                .timeout(properties.requestTimeout())
                .method(method, publisher);

        Collections.list(request.getHeaderNames()).forEach(name ->
        {
            if (HOP_BY_HOP.contains(name.toLowerCase()) || "x-request-id".equalsIgnoreCase(name))
            {
                return;
            }
            Collections.list(request.getHeaders(name)).forEach(value -> builder.header(name, value));
        });

        builder.header("X-Request-Id", requestId);
        return builder.build();
    }

    private void copyResponse(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpResponse<InputStream> upstream) throws IOException
    {
        response.setStatus(upstream.statusCode());

        upstream.headers().map().forEach((name, values) ->
        {
            String lower = name.toLowerCase();
            if (HOP_BY_HOP.contains(lower) || GATEWAY_OWNED_RESPONSE_HEADERS.contains(lower))
            {
                return;
            }
            values.forEach(value -> response.addHeader(name, value));
        });

        if (!"HEAD".equalsIgnoreCase(request.getMethod())
                && upstream.statusCode() != HttpServletResponse.SC_NO_CONTENT
                && upstream.statusCode() != HttpServletResponse.SC_NOT_MODIFIED)
        {
            try (InputStream body = upstream.body())
            {
                body.transferTo(response.getOutputStream());
            }
        }
        response.flushBuffer();
    }

    private URI buildTarget(GatewayProperties.Route route, String path, String query)
    {
        String base = route.target().toString().replaceAll("/+$", "");
        String url = base + path + (query == null || query.isBlank() ? "" : "?" + query);
        return URI.create(url);
    }

    private String resolveRequestId(HttpServletRequest request)
    {
        String header = request.getHeader("X-Request-Id");
        if (header != null && !header.isBlank() && header.length() <= 128)
        {
            return header;
        }
        return UUID.randomUUID().toString();
    }

    private void writeError(HttpServletResponse response, int status, String code, String message, String requestId)
            throws IOException
    {
        if (response.isCommitted())
        {
            return;
        }

        response.resetBuffer();
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("requestId", requestId);

        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.flushBuffer();
    }
}
