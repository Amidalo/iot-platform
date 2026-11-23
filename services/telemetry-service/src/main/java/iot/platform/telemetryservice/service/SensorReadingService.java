package iot.platform.telemetryservice.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iot.platform.telemetryservice.client.DeviceServiceClient;
import iot.platform.telemetryservice.dto.ReadingMapper;
import iot.platform.telemetryservice.exceptions.InvalidReadingException;
import iot.platform.telemetryservice.exceptions.ReadingNotFoundException;
import iot.platform.telemetryservice.exceptions.UnsupportedMetricException;
import iot.platform.telemetryservice.model.MetricType;
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

    /**
     * Сохраняет пакет показаний одного датчика.
     * Все метрики проверяются до записи, поэтому частичного сохранения не бывает.
     */
    @Transactional
    public List<SensorReading> create(Long sensorId, Map<String, Double> metrics)
    {
        Map<MetricType, Double> normalized = normalize(sensorId, metrics);
        Instant recordedAt = Instant.now();

        List<SensorReading> readings = normalized.entrySet().stream()
                .map(entry -> ReadingMapper.toEntity(sensorId, entry.getKey(), entry.getValue(), recordedAt))
                .toList();

        return repository.saveAll(readings);
    }

    @Transactional(readOnly = true)
    public List<SensorReading> getBySensorId(Long sensorId, MetricType metric)
    {
        if (metric == null)
        {
            return repository.findBySensorIdOrderByRecordedAtDesc(sensorId);
        }
        return repository.findBySensorIdAndMetricOrderByRecordedAtDesc(sensorId, metric);
    }

    @Transactional(readOnly = true)
    public SensorReading getById(Long id)
    {
        return repository.findById(id)
                .orElseThrow(() -> new ReadingNotFoundException(id));
    }

    private Map<MetricType, Double> normalize(Long sensorId, Map<String, Double> metrics)
    {
        if (metrics == null || metrics.isEmpty())
        {
            throw new InvalidReadingException("Нужна хотя бы одна метрика");
        }

        Map<MetricType, Double> parsed = new LinkedHashMap<>();

        for (Map.Entry<String, Double> entry : metrics.entrySet())
        {
            MetricType metric = parseMetric(entry.getKey());
            Double value = entry.getValue();

            if (value == null || !Double.isFinite(value))
            {
                throw new InvalidReadingException("Некорректное значение метрики " + metric + ": " + value);
            }
            if (parsed.putIfAbsent(metric, value) != null)
            {
                throw new InvalidReadingException("Метрика указана дважды: " + metric);
            }
        }

        Set<MetricType> supported = deviceServiceClient.getMetricTypes(sensorId);
        for (MetricType metric : parsed.keySet())
        {
            if (supported == null || !supported.contains(metric))
            {
                throw new UnsupportedMetricException(sensorId, metric);
            }
        }

        return parsed;
    }

    private MetricType parseMetric(String rawName)
    {
        if (rawName == null || rawName.isBlank())
        {
            throw new InvalidReadingException("Название метрики не может быть пустым");
        }

        try
        {
            return MetricType.valueOf(rawName.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex)
        {
            throw new InvalidReadingException("Неизвестная метрика: " + rawName);
        }
    }
}
