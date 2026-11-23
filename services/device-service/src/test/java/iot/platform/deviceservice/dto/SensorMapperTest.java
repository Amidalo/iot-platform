package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.model.SensorStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SensorMapperTest
{
    @Test
    void toEntity_shouldMapAllFieldsFromRequest()
    {
        SensorRequest sensorRequest = new SensorRequest(
                "Датчик кухня",
                Set.of(MetricType.TEMPERATURE, MetricType.HUMIDITY),
                SensorStatus.ONLINE);

        Sensor sensor = SensorMapper.toEntity(sensorRequest);

        assertThat(sensor.getName()).isEqualTo("Датчик кухня");
        assertThat(sensor.getMetrics()).containsExactlyInAnyOrder(MetricType.TEMPERATURE, MetricType.HUMIDITY);
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.ONLINE);
    }

    @Test
    void toResponse_shouldMapAllFieldsFromEntity()
    {
        Sensor sensor = new Sensor(
                "Датчик кухня",
                Set.of(MetricType.TEMPERATURE, MetricType.HUMIDITY),
                SensorStatus.ONLINE);
        sensor.setId(1L);
        Instant now = Instant.now();
        sensor.setUpdatedAt(now);

        SensorResponse response = SensorMapper.toResponse(sensor);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Датчик кухня");
        assertThat(response.metrics()).containsExactlyInAnyOrder(MetricType.TEMPERATURE, MetricType.HUMIDITY);
        assertThat(response.status()).isEqualTo(SensorStatus.ONLINE);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
