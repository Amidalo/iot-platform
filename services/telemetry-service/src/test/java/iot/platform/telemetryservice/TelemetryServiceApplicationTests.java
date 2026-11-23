package iot.platform.telemetryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.mqtt.enabled=false")
class TelemetryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
