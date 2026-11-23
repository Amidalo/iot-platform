package iot.platform.telemetryservice.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import iot.platform.telemetryservice.client.DeviceServiceClient;
import iot.platform.telemetryservice.exceptions.InvalidReadingException;
import iot.platform.telemetryservice.exceptions.InvalidSensorReferenceException;
import iot.platform.telemetryservice.exceptions.ReadingNotFoundException;
import iot.platform.telemetryservice.exceptions.UnsupportedMetricException;
import iot.platform.telemetryservice.model.MetricType;
import iot.platform.telemetryservice.model.SensorReading;
import iot.platform.telemetryservice.repository.SensorReadingRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest
{
    @Mock
    private SensorReadingRepository repository;

    @Mock
    private DeviceServiceClient deviceServiceClient;

    @InjectMocks
    private SensorReadingService service;

    @Test
    void create_shouldSaveAllMetricsOfOneMessage()
    {
        when(deviceServiceClient.getMetricTypes(1L))
                .thenReturn(Set.of(MetricType.TEMPERATURE, MetricType.HUMIDITY));
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SensorReading> result = service.create(1L, Map.of(
                "TEMPERATURE", 23.5,
                "HUMIDITY", 60.0));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SensorReading::getMetric)
                .containsExactlyInAnyOrder(MetricType.TEMPERATURE, MetricType.HUMIDITY);
        assertThat(result).extracting(SensorReading::getValue)
                .containsExactlyInAnyOrder(23.5, 60.0);

        Instant recordedAt = result.get(0).getRecordedAt();
        assertThat(result).allMatch(reading -> recordedAt.equals(reading.getRecordedAt()));
        verify(repository).saveAll(anyList());
    }

    @Test
    void create_shouldNormalizeMetricNames()
    {
        when(deviceServiceClient.getMetricTypes(1L)).thenReturn(Set.of(MetricType.TEMPERATURE));
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SensorReading> result = service.create(1L, Map.of("temperature", 23.5));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetric()).isEqualTo(MetricType.TEMPERATURE);
    }

    @Test
    void create_shouldThrow_whenSensorDoesNotExist()
    {
        when(deviceServiceClient.getMetricTypes(99L))
                .thenThrow(new InvalidSensorReferenceException(99L));

        assertThatThrownBy(() -> service.create(99L, Map.of("TEMPERATURE", 23.5)))
                .isInstanceOf(InvalidSensorReferenceException.class);

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void create_shouldThrow_whenMetricIsNotSupportedBySensor()
    {
        when(deviceServiceClient.getMetricTypes(1L)).thenReturn(Set.of(MetricType.TEMPERATURE));

        assertThatThrownBy(() -> service.create(1L, Map.of("HUMIDITY", 60.0)))
                .isInstanceOf(UnsupportedMetricException.class)
                .hasMessageContaining("HUMIDITY");

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void create_shouldThrow_whenMetricNameIsUnknown()
    {
        assertThatThrownBy(() -> service.create(1L, Map.of("CO2", 400.0)))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("CO2");

        verifyNoInteractions(deviceServiceClient, repository);
    }

    @Test
    void create_shouldThrow_whenValueIsNotFinite()
    {
        assertThatThrownBy(() -> service.create(1L, Map.of("TEMPERATURE", Double.NaN)))
                .isInstanceOf(InvalidReadingException.class);

        verifyNoInteractions(deviceServiceClient, repository);
    }

    @Test
    void create_shouldThrow_whenMetricsAreEmpty()
    {
        assertThatThrownBy(() -> service.create(1L, Map.of()))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("хотя бы одна метрика");

        verifyNoInteractions(deviceServiceClient, repository);
    }

    @Test
    void create_shouldThrow_whenSameMetricIsPassedTwice()
    {
        Map<String, Double> metrics = new LinkedHashMap<>();
        metrics.put("TEMPERATURE", 23.5);
        metrics.put("temperature", 24.0);

        assertThatThrownBy(() -> service.create(1L, metrics))
                .isInstanceOf(InvalidReadingException.class)
                .hasMessageContaining("дважды");

        verifyNoInteractions(deviceServiceClient, repository);
    }

    @Test
    void getBySensorId_shouldReturnAllReadings_whenMetricIsNull()
    {
        List<SensorReading> readings = List.of(
                new SensorReading(1L, MetricType.TEMPERATURE, 23.5, Instant.now()));
        when(repository.findBySensorIdOrderByRecordedAtDesc(1L)).thenReturn(readings);

        List<SensorReading> result = service.getBySensorId(1L, null);

        assertThat(result).isEqualTo(readings);
        verify(repository).findBySensorIdOrderByRecordedAtDesc(1L);
    }

    @Test
    void getBySensorId_shouldFilterByMetric_whenMetricIsSet()
    {
        List<SensorReading> readings = List.of(
                new SensorReading(1L, MetricType.TEMPERATURE, 23.5, Instant.now()));
        when(repository.findBySensorIdAndMetricOrderByRecordedAtDesc(1L, MetricType.TEMPERATURE))
                .thenReturn(readings);

        List<SensorReading> result = service.getBySensorId(1L, MetricType.TEMPERATURE);

        assertThat(result).isEqualTo(readings);
        verify(repository).findBySensorIdAndMetricOrderByRecordedAtDesc(1L, MetricType.TEMPERATURE);
    }

    @Test
    void getById_shouldThrow_whenReadingDoesNotExist()
    {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ReadingNotFoundException.class);
    }
}
