package iot.platform.deviceservice.repository;

import iot.platform.deviceservice.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long>
{

}
