package iot.platform.telemetryservice.controller;

import iot.platform.telemetryservice.dto.ReadingMapper;
import iot.platform.telemetryservice.dto.ReadingRequest;
import iot.platform.telemetryservice.dto.ReadingResponse;
import iot.platform.telemetryservice.model.SensorReading;
import iot.platform.telemetryservice.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController
{
    private final SensorReadingService service;

    public SensorReadingController(SensorReadingService service)
    {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingResponse create(@Valid @RequestBody ReadingRequest request)
    {
        SensorReading reading = service.create(ReadingMapper.toEntity(request));
        return ReadingMapper.toResponse(reading);
    }

    @GetMapping("/sensor/{sensorId}")
    public List<ReadingResponse> getBySensorId(@PathVariable Long sensorId)
    {
        return service.getBySensorId(sensorId).stream()
                .map(ReadingMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ReadingResponse getById(@PathVariable Long id)
    {
        return ReadingMapper.toResponse(service.getById(id));
    }
}
