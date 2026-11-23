package iot.platform.telemetryservice.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Entity
@Setter
@Getter
@Table(name = "sensor_reading")
public class SensorReading
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "sensorId обязателен")
    private Long sensorId;

    @NotNull(message = "metric обязательна")
    @Enumerated(EnumType.STRING)
    private MetricType metric;

    @NotNull(message = "value обязательно")
    private Double value;

    private Instant recordedAt;

    public SensorReading() {}

    public SensorReading(Long sensorId, MetricType metric, Double value, Instant recordedAt)
    {
        this.sensorId = sensorId;
        this.metric = metric;
        this.value = value;
        this.recordedAt = recordedAt;
    }
}
