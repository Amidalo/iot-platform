package iot.platform.deviceservice.service;

import iot.platform.deviceservice.exceptions.SensorNotFoundException;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SensorService
{
    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository)
    {
        this.sensorRepository = sensorRepository;
    }

    @Transactional(readOnly = true)
    public List<Sensor> getAll()
    {
        return sensorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sensor getById(Long id)
    {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new SensorNotFoundException(id));
    }

    @Transactional
    public Sensor create(Sensor sensor)
    {
        return sensorRepository.save(sensor);
    }

    @Transactional
    public Sensor update(Long id, Sensor sensor)
    {
        Sensor existingSensor = getById(id);

        existingSensor.setName(sensor.getName());
        if (sensor.getMetrics() != null)
        {
            existingSensor.getMetrics().clear();
            existingSensor.getMetrics().addAll(sensor.getMetrics());
        }
        existingSensor.setStatus(sensor.getStatus());
        existingSensor.setUpdatedAt(Instant.now());

        return sensorRepository.save(existingSensor);
    }

    @Transactional
    public void delete(Long id)
    {
        if (!sensorRepository.existsById(id))
        {
            throw new SensorNotFoundException(id);
        }

        sensorRepository.deleteById(id);
    }
}
