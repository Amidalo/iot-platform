package iot.platform.deviceservice.service;

import iot.platform.deviceservice.exceptions.SensorNotFoundException;
import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.model.SensorStatus;
import iot.platform.deviceservice.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest
{
    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorService sensorService;

    private Sensor sensor;

    @BeforeEach
    void setup()
    {
        sensor = new Sensor(
                "Датчик кухня",
                Set.of(MetricType.TEMPERATURE, MetricType.HUMIDITY),
                SensorStatus.ONLINE);
        sensor.setId(1L);
    }

    @Test
    void getById_shouldReturnSensor_whenExists()
    {
        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));

        Sensor result = sensorService.getById(1L);

        assertThat(result.getName()).isEqualTo("Датчик кухня");
        assertThat(result.getMetrics()).containsExactlyInAnyOrder(MetricType.TEMPERATURE, MetricType.HUMIDITY);
    }

    @Test
    void getById_shouldThrow_whenNotExists()
    {
        when(sensorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensorService.getById(99L))
                .isInstanceOf(SensorNotFoundException.class);
    }

    @Test
    void getAll_shouldReturnAllSensors()
    {
        when(sensorRepository.findAll()).thenReturn(List.of(sensor));

        List<Sensor> result = sensorService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnSensor()
    {
        when(sensorRepository.save(sensor)).thenReturn(sensor);

        Sensor result = sensorService.create(sensor);

        assertThat(result).isEqualTo(sensor);
        verify(sensorRepository).save(sensor);
    }

    @Test
    void update_shouldReplaceMetricsAndRefreshUpdatedAt()
    {
        Instant oldUpdatedAt = Instant.parse("2026-01-01T00:00:00Z");
        sensor.setUpdatedAt(oldUpdatedAt);

        Sensor patch = new Sensor(
                "Датчик кухня №2",
                Set.of(MetricType.PRESSURE),
                SensorStatus.OFFLINE);

        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        when(sensorRepository.save(sensor)).thenReturn(sensor);

        Sensor result = sensorService.update(1L, patch);

        assertThat(result.getName()).isEqualTo("Датчик кухня №2");
        assertThat(result.getMetrics()).containsExactly(MetricType.PRESSURE);
        assertThat(result.getStatus()).isEqualTo(SensorStatus.OFFLINE);
        assertThat(result.getUpdatedAt()).isAfter(oldUpdatedAt);
        verify(sensorRepository).save(sensor);
    }

    @Test
    void delete_shouldThrow_whenNotExists()
    {
        when(sensorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> sensorService.delete(99L))
                .isInstanceOf(SensorNotFoundException.class);

        verify(sensorRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldDelete_whenExists()
    {
        when(sensorRepository.existsById(1L)).thenReturn(true);

        sensorService.delete(1L);

        verify(sensorRepository).deleteById(1L);
    }
}
