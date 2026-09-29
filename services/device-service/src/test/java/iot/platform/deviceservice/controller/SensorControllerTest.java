package iot.platform.deviceservice.controller;


import iot.platform.deviceservice.dto.SensorRequest;
import iot.platform.deviceservice.model.Sensor;
import iot.platform.deviceservice.model.SensorStatus;
import iot.platform.deviceservice.model.SensorType;
import iot.platform.deviceservice.service.SensorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

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

    @Test
    void getAll_shouldReturnList() throws Exception
    {
        Sensor sensor = new Sensor("Датчик кухня", SensorType.TEMPERATURE, SensorStatus.ONLINE);
        sensor.setId(1L);

        when(sensorService.getAll()).thenReturn(List.of(sensor));

        mockMvc.perform(get("/api/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Датчик кухня"));
    }

    @Test
    void getById_shouldReturnSensor() throws Exception
    {
        Sensor sensor = new Sensor("Датчик кухня", SensorType.TEMPERATURE, SensorStatus.ONLINE);
        sensor.setId(1L);

        when(sensorService.getById(1L)).thenReturn(sensor);

        mockMvc.perform(get("/api/sensors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ONLINE"));
    }

    @Test
    void create_shouldReturn201_whenValid() throws Exception
    {
        SensorRequest request = new SensorRequest("Датчик кухня", SensorType.TEMPERATURE, SensorStatus.ONLINE);

        Sensor created = new Sensor("Датчик кухня", SensorType.TEMPERATURE, SensorStatus.ONLINE);
        created.setId(1L);

        when(sensorService.create(any(Sensor.class))).thenReturn(created);

        mockMvc.perform(post("/api/sensors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_shouldReturn400_whenNameBlank() throws Exception
    {
        SensorRequest invalid = new SensorRequest("", SensorType.TEMPERATURE, SensorStatus.ONLINE);

        mockMvc.perform(post("/api/sensors")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название датчика обязательно"));
    }

    @Test
    void delete_shouldReturn204() throws Exception
    {
        mockMvc.perform(delete("/api/sensors/1"))
                .andExpect(status().isNoContent());
    }
}