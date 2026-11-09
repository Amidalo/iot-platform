package iot.platform.telemetryservice.mqtt;

import iot.platform.telemetryservice.model.SensorReading;
import iot.platform.telemetryservice.service.SensorReadingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttSensorSubscriber implements MqttCallbackExtended
{
    private static final Logger log = LoggerFactory.getLogger(MqttSensorSubscriber.class);
    private static final int QOS = 1;

    private final SensorReadingService readingService;
    private final ObjectMapper objectMapper;
    private final String brokerUrl;
    private final String clientId;
    private final String topicFilter;

    private MqttClient client;

    public MqttSensorSubscriber(SensorReadingService readingService,
                                ObjectMapper objectMapper,
                                @Value("${app.mqtt.broker-url}") String brokerUrl,
                                @Value("${app.mqtt.client-id}") String clientId,
                                @Value("${app.mqtt.topic-filter}") String topicFilter)
    {
        this.readingService = readingService;
        this.objectMapper = objectMapper;
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topicFilter = topicFilter;
    }

    @PostConstruct
    public void start() throws MqttException
    {
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        client.setCallback(this);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        client.connect(options);
    }

    @PreDestroy
    public void stop() throws MqttException
    {
        if (client != null && client.isConnected())
        {
            client.disconnect();
        }
        if (client != null)
        {
            client.close();
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri)
    {
        // Подписываемся здесь, а не в start(): после обрыва связи подписки пропадают,
        // и этот метод вызывается при каждом (пере)подключении.
        try
        {
            client.subscribe(topicFilter, QOS);
            log.info("Подписались на {} ({})", topicFilter, reconnect ? "после переподключения" : "первое подключение");
        }
        catch (MqttException e)
        {
            log.error("Не удалось подписаться на {}", topicFilter, e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
    {
        // ВАЖНО: любое исключение отсюда Paho воспринимает как ошибку клиента и рвёт соединение.
        // Поэтому ловим всё сами.
        try
        {
            // Топик вида sensors/{id}/reading
            String[] parts = topic.split("/");
            if (parts.length != 3)
            {
                log.warn("Неожиданный формат топика: {}", topic);
                return;
            }
            Long sensorId = Long.parseLong(parts[1]);

            JsonNode valueNode = objectMapper.readTree(message.getPayload()).get("value");
            if (valueNode == null || !valueNode.isNumber())
            {
                log.warn("В сообщении из {} нет числового поля value", topic);
                return;
            }

            readingService.create(new SensorReading(sensorId, valueNode.asDouble()));
            log.debug("Сохранено показание: датчик {} = {}", sensorId, valueNode.asDouble());
        }
        catch (NumberFormatException e)
        {
            log.warn("sensorId в топике {} не число", topic);
        }
        catch (Exception e)
        {
            log.warn("Не удалось обработать сообщение из {}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause)
    {
        log.warn("Соединение с MQTT-брокером потеряно: {}", cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}
