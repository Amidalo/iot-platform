package iot.platform.deviceservice.dto;

import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.model.SensorStatus;
import iot.platform.deviceservice.model.SensorType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class SensorMapperTest
{
    @Test
    void toEntity_shouldMapAllFieldsFromRequest()
    {
        SensorRequest sensorRequest = new SensorRequest
                ("Датчик кухня", SensorType.TEMPERATURE, SensorStatus.ONLINE);

        Sensor sensor = SensorMapper.toEntity(sensorRequest);

        assertThat(sensor.getName()).isEqualTo("Датчик кухня");
        assertThat(sensor.getType()).isEqualTo(SensorType.TEMPERATURE);
        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.ONLINE);
    }

    @Test
    void toResponse_shouldMapAllFieldsFromEntity()
    {
        Sensor sensor = new Sensor("Датчик кухня", SensorType.TEMPERATURE, SensorStatus.ONLINE);
        sensor.setId(1L);
        Instant now = Instant.now();
        sensor.setUpdatedAt(now);

        SensorResponse response = SensorMapper.toResponse(sensor);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Датчик кухня");
        assertThat(response.type()).isEqualTo(SensorType.TEMPERATURE);
        assertThat(response.status()).isEqualTo(SensorStatus.ONLINE);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
