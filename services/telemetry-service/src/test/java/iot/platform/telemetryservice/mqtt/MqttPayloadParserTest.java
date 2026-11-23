package iot.platform.telemetryservice.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import iot.platform.telemetryservice.exceptions.InvalidReadingException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MqttPayloadParserTest
{
    private final MqttPayloadParser parser = new MqttPayloadParser(new ObjectMapper());

    private Map<String, Double> parse(String json)
    {
        return parser.parse(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parse_shouldReadMetricsFromNestedObject()
    {
        Map<String, Double> metrics = parse("""
                {"metrics": {"TEMPERATURE": 23.5, "HUMIDITY": 60.0}}
                """);

        assertThat(metrics)
                .containsEntry("TEMPERATURE", 23.5)
                .containsEntry("HUMIDITY", 60.0)
                .hasSize(2);
    }

    @Test
    void parse_shouldReadFlatMetrics()
    {
        Map<String, Double> metrics = parse("""
                {"TEMPERATURE": 23.5, "HUMIDITY": 60.0}
                """);

        assertThat(metrics)
                .containsEntry("TEMPERATURE", 23.5)
                .containsEntry("HUMIDITY", 60.0);
    }

    @Test
    void parse_shouldIgnoreExtraFieldsNextToMetrics()
    {
        Map<String, Double> metrics = parse("""
                {"metrics": {"TEMPERATURE": 23.5}, "timestamp": "2026-11-10T00:00:00Z"}
                """);

        assertThat(metrics)
                .containsEntry("TEMPERATURE", 23.5)
                .hasSize(1);
    }

    @Test
    void parse_shouldRejectNonNumericValue()
    {
        assertThatThrownBy(() -> parse("""
                {"metrics": {"TEMPERATURE": "23.5"}}
                """))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("должно быть числом");
    }

    @Test
    void parse_shouldRejectEmptyMetrics()
    {
        assertThatThrownBy(() -> parse("""
                {"metrics": {}}
                """))
                .isInstanceOf(InvalidReadingException.class);
    }

    @Test
    void parse_shouldRejectMalformedJson()
    {
        assertThatThrownBy(() -> parse("{not-json"))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("корректным JSON");
    }

    @Test
    void parse_shouldRejectNonObjectMetrics()
    {
        assertThatThrownBy(() -> parse("""
                {"metrics": [23.5]}
                """))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("должно быть объектом");
    }
}
