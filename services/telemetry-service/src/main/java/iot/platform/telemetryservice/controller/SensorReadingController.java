package iot.platform.telemetryservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import iot.platform.telemetryservice.dto.ReadingMapper;
import iot.platform.telemetryservice.dto.ReadingRequest;
import iot.platform.telemetryservice.dto.ReadingResponse;
import iot.platform.telemetryservice.model.MetricType;
import iot.platform.telemetryservice.service.SensorReadingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController
{
    private final SensorReadingService service;

    public SensorReadingController(SensorReadingService service)
    {
        this.service = service;
    }

    /**
     * Принимает пакет показаний одного датчика и возвращает сохранённые строки.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ReadingResponse> create(@Valid @RequestBody ReadingRequest request)
    {
        return service.create(request.sensorId(), request.metrics()).stream()
                .map(ReadingMapper::toResponse)
                .toList();
    }

    @GetMapping("/sensor/{sensorId}")
    public List<ReadingResponse> getBySensorId(
            @PathVariable Long sensorId,
            @RequestParam(required = false) MetricType metric)
    {
        return service.getBySensorId(sensorId, metric).stream()
                .map(ReadingMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ReadingResponse getById(@PathVariable Long id)
    {
        return ReadingMapper.toResponse(service.getById(id));
    }
}
