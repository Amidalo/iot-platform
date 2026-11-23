package iot.platform.deviceservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "sensor")
@Getter
@Setter
public class Sensor
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название датчика обязательно")
    private String name;

    /**
     * Набор метрик, которые умеет отдавать датчик.
     * Хранится в отдельной таблице sensor_metric (sensor_id, metric).
     */
    @NotEmpty(message = "Нужна хотя бы одна метрика")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sensor_metric", joinColumns = @JoinColumn(name = "sensor_id"))
    @Column(name = "metric", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<MetricType> metrics = new LinkedHashSet<>();

    @NotNull(message = "Статус датчика обязателен")
    @Enumerated(EnumType.STRING)
    private SensorStatus status;

    private Instant updatedAt;

    public Sensor() {}

    public Sensor(String name, Set<MetricType> metrics, SensorStatus status)
    {
        this.name = name;
        this.metrics = metrics == null ? new LinkedHashSet<>() : new LinkedHashSet<>(metrics);
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
