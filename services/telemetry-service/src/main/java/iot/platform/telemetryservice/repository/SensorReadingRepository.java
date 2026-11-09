package iot.platform.telemetryservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import iot.platform.telemetryservice.model.SensorReading;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long>
{
    List<SensorReading> findBySensorIdOrderByRecordedAtDesc(Long sensorId);    
}
