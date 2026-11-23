package iot.platform.telemetryservice.mqtt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import iot.platform.telemetryservice.exceptions.InvalidReadingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Разбирает MQTT-payload с несколькими метриками.
 *
 * Поддерживаются два формата:
 *   {"metrics": {"TEMPERATURE": 23.5, "HUMIDITY": 60.0}}
 *   {"TEMPERATURE": 23.5, "HUMIDITY": 60.0}
 */
@Component
public class MqttPayloadParser
{
    private final ObjectMapper objectMapper;

    public MqttPayloadParser(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public Map<String, Double> parse(byte[] payload)
    {
        JsonNode root;
        try
        {
            root = objectMapper.readTree(payload);
        }
        catch (Exception ex)
        {
            throw new InvalidReadingException("Payload не является корректным JSON");
        }

        if (root == null || !root.isObject())
        {
            throw new InvalidReadingException("Payload должен быть JSON-объектом");
        }

        JsonNode metricsNode = root.get("metrics");
        if (metricsNode != null)
        {
            if (!metricsNode.isObject())
            {
                throw new InvalidReadingException("Поле metrics должно быть объектом");
            }
            return readMetrics(metricsNode);
        }

        return readMetrics(root);
    }

    private Map<String, Double> readMetrics(JsonNode node)
    {
        Map<String, Double> metrics = new LinkedHashMap<>();

        for (Map.Entry<String, JsonNode> entry : node.properties())
        {
            String name = entry.getKey();
            JsonNode valueNode = entry.getValue();

            if (name == null || name.isBlank())
            {
                throw new InvalidReadingException("Название метрики не может быть пустым");
            }
            if (valueNode == null || !valueNode.isNumber())
            {
                throw new InvalidReadingException("Значение метрики " + name + " должно быть числом");
            }

            metrics.put(name, valueNode.asDouble());
        }

        if (metrics.isEmpty())
        {
            throw new InvalidReadingException("В сообщении нет ни одной метрики");
        }

        return metrics;
    }
}
