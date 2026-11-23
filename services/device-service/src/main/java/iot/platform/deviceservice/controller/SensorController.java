package iot.platform.deviceservice.controller;

import iot.platform.deviceservice.dto.SensorMapper;
import iot.platform.deviceservice.dto.SensorRequest;
import iot.platform.deviceservice.dto.SensorResponse;
import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/sensors")
public class SensorController
{
    private final SensorService sensorService;

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

    /**
     * Набор метрик датчика. Используется telemetry-service для проверки того,
     * что присланные показания относятся к поддерживаемым метрикам.
     */
    @GetMapping("/{id}/metrics")
    public Set<MetricType> getMetrics(@PathVariable Long id)
    {
        return sensorService.getById(id).getMetrics();
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
    public void delete(@PathVariable Long id)
    {
        sensorService.delete(id);
    }
}
