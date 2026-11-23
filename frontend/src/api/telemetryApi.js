// REST-клиент для телеметрии через gateway (http://localhost:8090).
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

/** Получить все показания датчика (опционально отфильтровать по метрике) */
export async function fetchReadings(sensorId, metric) {
  const url = metric
    ? API_BASE +
      "/api/readings/sensor/" +
      sensorId +
      "?metric=" +
      encodeURIComponent(metric)
    : API_BASE + "/api/readings/sensor/" + sensorId;
  const response = await fetch(url, { headers: defaultHeaders });
  return handleResponse(response);
}

/** Создать показания датчика */
export async function createReadings(sensorId, metrics) {
  const response = await fetch(API_BASE + "/api/readings", {
    method: "POST",
    headers: defaultHeaders,
    body: JSON.stringify({ sensorId, metrics }),
  });
  return handleResponse(response);
}

/** Получить одно показание по ID */
export async function fetchReadingById(id) {
  const response = await fetch(API_BASE + "/api/readings/" + id, {
    headers: defaultHeaders,
  });
  return handleResponse(response);
}