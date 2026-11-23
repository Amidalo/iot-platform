// REST-клиент для взаимодействия с бэкендом через gateway (http://localhost:8090).
// Адрес настраивается через переменную окружения VITE_API_BASE.

const API_BASE = import.meta.env.VITE_API_BASE || "";

const defaultHeaders = {
  "Content-Type": "application/json",
};

const handleResponse = async (response) => {
  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error("Ошибка " + response.status + (body ? ": " + body : ""));
  }
  if (response.status === 204) return null;
  return response.json();
};

export async function fetchSensors() {
  const response = await fetch(API_BASE + "/api/sensors", { headers: defaultHeaders });
  return handleResponse(response);
}

export async function fetchSensorById(id) {
  const response = await fetch(API_BASE + "/api/sensors/" + id, { headers: defaultHeaders });
  return handleResponse(response);
}

export async function createSensor(sensor) {
  const response = await fetch(API_BASE + "/api/sensors", {
    method: "POST", headers: defaultHeaders, body: JSON.stringify(sensor),
  });
  return handleResponse(response);
}

export async function updateSensor(id, sensor) {
  const response = await fetch(API_BASE + "/api/sensors/" + id, {
    method: "PUT", headers: defaultHeaders, body: JSON.stringify(sensor),
  });
  return handleResponse(response);
}

export async function deleteSensor(id) {
  const response = await fetch(API_BASE + "/api/sensors/" + id, {
    method: "DELETE", headers: defaultHeaders,
  });
  return handleResponse(response);
}

export async function fetchReadings(sensorId, metric) {
  const url = metric
    ? API_BASE + "/api/readings/sensor/" + sensorId + "?metric=" + encodeURIComponent(metric)
    : API_BASE + "/api/readings/sensor/" + sensorId;
  const response = await fetch(url, { headers: defaultHeaders });
  return handleResponse(response);
}
