package iot.platform.deviceservice.controller;

import iot.platform.deviceservice.dto.SensorMapper;
import iot.platform.deviceservice.dto.SensorRequest;
import iot.platform.deviceservice.dto.SensorResponse;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController
{
    private final SensorService sensorService;

    @Autowired
    public SensorController(SensorService sensorService)
    {
        this.sensorService = sensorService;
    }

    @GetMapping
    public List<SensorResponse> getAll()
    {
        return sensorService.getAll().stream()
                .map(SensorMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SensorResponse getById(@PathVariable Long id)
    {
        Sensor sensor = sensorService.getById(id);
        return SensorMapper.toResponse(sensor);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorResponse create(@Valid @RequestBody SensorRequest sensorRequest)
    {
        Sensor sensor = sensorService.create(SensorMapper.toEntity(sensorRequest));
        return SensorMapper.toResponse(sensor);
    }

    @PutMapping("/{id}")
    public SensorResponse update(@PathVariable Long id, @Valid @RequestBody SensorRequest sensorRequest)
    {
        Sensor updatedSensor = sensorService.update(id, SensorMapper.toEntity(sensorRequest));
        return SensorMapper.toResponse(updatedSensor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id)
    {
        sensorService.delete(id);
    }
}
