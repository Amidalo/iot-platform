package iot.platform.deviceservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    private SensorType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SensorStatus status;

    private Instant updatedAt;

    public Sensor() {}

    public Sensor(String name, SensorType type, SensorStatus status) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
