package iot.platform.telemetryservice.service;

import iot.platform.telemetryservice.client.DeviceServiceClient;
import iot.platform.telemetryservice.exceptions.InvalidSensorReferenceException;
import iot.platform.telemetryservice.model.SensorReading;
import iot.platform.telemetryservice.repository.SensorReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void create_shouldSave_whenSensorExists()
    {
        SensorReading reading = new SensorReading(1L, 23.5);
        doNothing().when(deviceServiceClient).verifySensorExists(1L);
        when(repository.save(reading)).thenReturn(reading);

        SensorReading result = service.create(reading);

        assertThat(result.getValue()).isEqualTo(23.5);
        verify(repository).save(reading);
    }

    @Test
    void create_shouldThrow_whenSensorDoesNotExist()
    {
        SensorReading reading = new SensorReading(99L, 23.5);
        doThrow(new InvalidSensorReferenceException(99L))
                .when(deviceServiceClient).verifySensorExists(99L);

        assertThatThrownBy(() -> service.create(reading))
                .isInstanceOf(InvalidSensorReferenceException.class);

        verify(repository, never()).save(any());
    }
}
