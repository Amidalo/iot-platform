package iot.platform.telemetryservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import iot.platform.telemetryservice.client.DeviceServiceClient;
import iot.platform.telemetryservice.exceptions.ReadingNotFoundException;
import iot.platform.telemetryservice.model.SensorReading;
import iot.platform.telemetryservice.repository.SensorReadingRepository;

@Service
public class SensorReadingService
{
    private final SensorReadingRepository repository;
    private final DeviceServiceClient deviceServiceClient;

    public SensorReadingService(SensorReadingRepository repository, DeviceServiceClient deviceServiceClient)
    {
        this.repository = repository;
        this.deviceServiceClient = deviceServiceClient;
    }

    public SensorReading create(SensorReading reading)
    {
        deviceServiceClient.verifySensorExists(reading.getSensorId());
        return repository.save(reading);
    }

    public List<SensorReading> getBySensorId(Long sensorId)
    {
        return repository.findBySensorIdOrderByRecordedAtDesc(sensorId);
    }

    public SensorReading getById(Long id)
    {
        return repository.findById(id)
                .orElseThrow(() -> new ReadingNotFoundException(id));
    }
}
