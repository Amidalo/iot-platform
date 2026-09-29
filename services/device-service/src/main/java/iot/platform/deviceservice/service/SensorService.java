package iot.platform.deviceservice.service;

import iot.platform.deviceservice.exceptions.SensorNotFoundException;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorService
{
    private final SensorRepository sensorRepository;

    @Autowired
    public SensorService(SensorRepository sensorRepository)
    {
        this.sensorRepository = sensorRepository;
    }

    public List<Sensor> getAll()
    {
        return sensorRepository.findAll();
    }

    public Sensor getById(Long id)
    {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new SensorNotFoundException(id));
    }

    public Sensor create(Sensor sensor)
    {
        return sensorRepository.save(sensor);
    }

    public Sensor update(Long id, Sensor sensor)
    {
        Sensor existingSensor = getById(id);

        existingSensor.setName(sensor.getName());
        existingSensor.setType(sensor.getType());
        existingSensor.setStatus(sensor.getStatus());

        return sensorRepository.save(existingSensor);

    }

    public void delete(Long id)
    {
        if (!sensorRepository.existsById(id))
        {
            throw new SensorNotFoundException(id);
        }

        sensorRepository.deleteById(id);
    }
}
