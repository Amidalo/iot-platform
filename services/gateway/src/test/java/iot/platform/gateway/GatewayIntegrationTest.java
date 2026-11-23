package iot.platform.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIntegrationTest
{
    private static final HttpServer UPSTREAM;
    private static final int UPSTREAM_PORT;

    static
    {
        try
        {
            UPSTREAM = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            UPSTREAM.createContext("/api/sensors", GatewayIntegrationTest::handle);
            UPSTREAM.start();
            UPSTREAM_PORT = UPSTREAM.getAddress().getPort();
        }
        catch (IOException ex)
        {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void routes(DynamicPropertyRegistry registry)
    {
        registry.add("gateway.routes[0].prefix", () -> "/api/sensors");
        registry.add("gateway.routes[0].target", () -> "http://127.0.0.1:" + UPSTREAM_PORT);
        registry.add("gateway.routes[1].prefix", () -> "/api/broken");
        registry.add("gateway.routes[1].target", () -> "http://127.0.0.1:1");
    }

    @AfterAll
    static void stopUpstream()
    {
        UPSTREAM.stop(0);
    }

    @Test
    void shouldProxyGetWithQueryAndRequestId() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/sensors/echo?x=1&y=two"))
                .header("X-Request-Id", "test-request-1")
                .header("Origin", "http://localhost:5173")
                .GET()
                .build();

        HttpResponse<String> response = send(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Request-Id")).contains("test-request-1");
        assertThat(response.headers().allValues("Access-Control-Allow-Origin"))
                .containsExactly("http://localhost:5173");
        assertThat(response.body())
                .contains("\"method\":\"GET\"")
                .contains("\"path\":\"/api/sensors/echo\"")
                .contains("\"query\":\"x=1&y=two\"")
                .contains("\"requestId\":\"test-request-1\"");
    }

    @Test
    void shouldProxyExactPrefix() throws Exception
    {
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/sensors")).GET().build());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"path\":\"/api/sensors\"");
    }

    @Test
    void shouldProxyPostBody() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/sensors/echo"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"test\"}"))
                .build();

        HttpResponse<String> response = send(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"method\":\"POST\"");
        assertThat(response.body()).contains("\"body\":\"{\\\"name\\\":\\\"test\\\"}\"");
    }

    @Test
    void shouldReturn404ForUnknownRoute() throws Exception
    {
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/unknown")).GET().build());

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("ROUTE_NOT_FOUND");
    }

    @Test
    void shouldReturn502WhenUpstreamIsUnavailable() throws Exception
    {
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/broken/test")).GET().build());

        assertThat(response.statusCode()).isEqualTo(502);
        assertThat(response.body()).contains("UPSTREAM_UNAVAILABLE");
    }

    @Test
    void shouldAnswerCorsPreflight() throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/sensors/echo"))
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = send(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
                .contains("http://localhost:5173");
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception
    {
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path)
    {
        return URI.create("http://127.0.0.1:" + gatewayPort + path);
    }

    private static void handle(HttpExchange exchange) throws IOException
    {
        byte[] requestBody = exchange.getRequestBody().readAllBytes();
        String requestId = exchange.getRequestHeaders().getFirst("X-Request-Id");
        String body = requestBody.length == 0 ? "" : new String(requestBody, StandardCharsets.UTF_8);

        String response = "{" +
                "\"method\":\"" + exchange.getRequestMethod() + "\"," +
                "\"path\":\"" + exchange.getRequestURI().getPath() + "\"," +
                "\"query\":\"" + (exchange.getRequestURI().getQuery() == null ? "" : exchange.getRequestURI().getQuery()) + "\"," +
                "\"requestId\":\"" + (requestId == null ? "" : requestId) + "\"," +
                "\"body\":\"" + body.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" +
                "}";

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://upstream");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
