package iot.platform.telemetryservice.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import iot.platform.telemetryservice.dto.ReadingRequest;
import iot.platform.telemetryservice.exceptions.ReadingNotFoundException;
import iot.platform.telemetryservice.model.MetricType;
import iot.platform.telemetryservice.model.SensorReading;
import iot.platform.telemetryservice.service.SensorReadingService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorReadingController.class)
class SensorReadingControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorReadingService service;

    @Autowired
    private ObjectMapper objectMapper;

    private SensorReading reading(Long id, MetricType metric, double value)
    {
        SensorReading reading = new SensorReading(1L, metric, value, Instant.now());
        reading.setId(id);
        return reading;
    }

    @Test
    void create_shouldReturnSavedReadings() throws Exception
    {
        ReadingRequest request = new ReadingRequest(1L, Map.of(
                "TEMPERATURE", 23.5,
                "HUMIDITY", 60.0));

        when(service.create(eq(1L), anyMap())).thenReturn(List.of(
                reading(1L, MetricType.TEMPERATURE, 23.5),
                reading(2L, MetricType.HUMIDITY, 60.0)));

        mockMvc.perform(post("/api/readings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sensorId").value(1));
    }

    @Test
    void create_shouldReturn400_whenMetricsAreEmpty() throws Exception
    {
        ReadingRequest request = new ReadingRequest(1L, Map.of());

        mockMvc.perform(post("/api/readings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.metrics").value("Нужна хотя бы одна метрика"));
    }

    @Test
    void getBySensorId_shouldReturnAllReadings() throws Exception
    {
        when(service.getBySensorId(1L, null))
                .thenReturn(List.of(reading(1L, MetricType.TEMPERATURE, 23.5)));

        mockMvc.perform(get("/api/readings/sensor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].metric").value("TEMPERATURE"));
    }

    @Test
    void getBySensorId_shouldPassMetricFilter() throws Exception
    {
        when(service.getBySensorId(1L, MetricType.HUMIDITY))
                .thenReturn(List.of(reading(2L, MetricType.HUMIDITY, 60.0)));

        mockMvc.perform(get("/api/readings/sensor/1").param("metric", "HUMIDITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].metric").value("HUMIDITY"));

        verify(service).getBySensorId(1L, MetricType.HUMIDITY);
    }

    @Test
    void getById_shouldReturnReading() throws Exception
    {
        when(service.getById(1L)).thenReturn(reading(1L, MetricType.TEMPERATURE, 23.5));

        mockMvc.perform(get("/api/readings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.metric").value("TEMPERATURE"));
    }

    @Test
    void getById_shouldReturn404_whenReadingDoesNotExist() throws Exception
    {
        when(service.getById(99L)).thenThrow(new ReadingNotFoundException(99L));

        mockMvc.perform(get("/api/readings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Показание с id = 99 не найдено"));
    }
}
