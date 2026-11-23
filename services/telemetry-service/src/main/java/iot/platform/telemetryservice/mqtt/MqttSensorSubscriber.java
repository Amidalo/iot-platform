package iot.platform.telemetryservice.mqtt;

import java.util.Map;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import iot.platform.telemetryservice.service.SensorReadingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttSensorSubscriber implements MqttCallbackExtended
{
    private static final Logger log = LoggerFactory.getLogger(MqttSensorSubscriber.class);
    private static final int QOS = 1;

    private final SensorReadingService readingService;
    private final MqttPayloadParser payloadParser;
    private final String brokerUrl;
    private final String clientId;
    private final String topicFilter;

    private MqttClient client;

    public MqttSensorSubscriber(SensorReadingService readingService,
                                MqttPayloadParser payloadParser,
                                @Value("${app.mqtt.broker-url}") String brokerUrl,
                                @Value("${app.mqtt.client-id}") String clientId,
                                @Value("${app.mqtt.topic-filter}") String topicFilter)
    {
        this.readingService = readingService;
        this.payloadParser = payloadParser;
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
            Long sensorId = parseSensorId(topic);
            if (sensorId == null)
            {
                return;
            }

            Map<String, Double> metrics = payloadParser.parse(message.getPayload());
            int saved = readingService.create(sensorId, metrics).size();

            log.debug("Сохранено {} показаний для датчика {}", saved, sensorId);
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

    private Long parseSensorId(String topic)
    {
        // Топик вида sensors/{id}/reading
        String[] parts = topic.split("/");
        if (parts.length != 3 || !"sensors".equals(parts[0]) || !"reading".equals(parts[2]))
        {
            log.warn("Неожиданный формат топика: {}", topic);
            return null;
        }

        return Long.parseLong(parts[1]);
    }

    @Override
    public void connectionLost(Throwable cause)
    {
        log.warn("Соединение с MQTT-брокером потеряно: {}", cause == null ? "unknown" : cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}
