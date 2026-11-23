package iot.platform.deviceservice.controller;

import iot.platform.deviceservice.dto.SensorRequest;
import iot.platform.deviceservice.model.MetricType;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.model.SensorStatus;
import iot.platform.deviceservice.service.SensorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensorController.class)
class SensorControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorService sensorService;

    @Autowired
    private ObjectMapper objectMapper;

    private Sensor sensor()
    {
        Sensor sensor = new Sensor(
                "Датчик кухня",
                Set.of(MetricType.TEMPERATURE, MetricType.HUMIDITY),
                SensorStatus.ONLINE);
        sensor.setId(1L);
        return sensor;
    }

    @Test
    void getAll_shouldReturnList() throws Exception
    {
        when(sensorService.getAll()).thenReturn(List.of(sensor()));

        mockMvc.perform(get("/api/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Датчик кухня"))
                .andExpect(jsonPath("$[0].metrics", hasItems("TEMPERATURE", "HUMIDITY")));
    }

    @Test
    void getById_shouldReturnSensor() throws Exception
    {
        when(sensorService.getById(1L)).thenReturn(sensor());

        mockMvc.perform(get("/api/sensors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ONLINE"))
                .andExpect(jsonPath("$.metrics", hasItems("TEMPERATURE", "HUMIDITY")));
    }

    @Test
    void getMetrics_shouldReturnMetricList() throws Exception
    {
        when(sensorService.getById(1L)).thenReturn(sensor());

        mockMvc.perform(get("/api/sensors/1/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("TEMPERATURE", "HUMIDITY")));
    }

    @Test
    void create_shouldReturn201_whenValid() throws Exception
    {
        SensorRequest request = new SensorRequest(
                "Датчик кухня",
                Set.of(MetricType.TEMPERATURE, MetricType.HUMIDITY),
                SensorStatus.ONLINE);
        Sensor created = sensor();

        when(sensorService.create(any(Sensor.class))).thenReturn(created);

        mockMvc.perform(post("/api/sensors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.metrics", hasItems("TEMPERATURE", "HUMIDITY")));
    }

    @Test
    void create_shouldReturn400_whenNameBlank() throws Exception
    {
        SensorRequest invalid = new SensorRequest(
                "",
                Set.of(MetricType.TEMPERATURE),
                SensorStatus.ONLINE);

        mockMvc.perform(post("/api/sensors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название датчика обязательно"));
    }

    @Test
    void create_shouldReturn400_whenMetricsEmpty() throws Exception
    {
        SensorRequest invalid = new SensorRequest(
                "Датчик кухня",
                Set.of(),
                SensorStatus.ONLINE);

        mockMvc.perform(post("/api/sensors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.metrics").value("Нужна хотя бы одна метрика"));
    }

    @Test
    void delete_shouldReturn204() throws Exception
    {
        mockMvc.perform(delete("/api/sensors/1"))
                .andExpect(status().isNoContent());
    }
}
