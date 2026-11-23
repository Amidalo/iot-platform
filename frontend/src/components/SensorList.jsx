import { useEffect, useState } from "react";
import { fetchSensors, deleteSensor } from "../api/sensorApi";
import SensorCard from "./SensorCard";
import SensorForm from './SensorForm';

function SensorList() {
  const [sensors, setSensors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;

    async function loadSensors() {
      try {
        setLoading(true);
        const data = await fetchSensors();
        if (!ignore) {
          setSensors(data);
          setError(null);
        }
      } catch (err) {
        if (!ignore) setError(err.message);
      } finally {
        if (!ignore) setLoading(false);
      }
    }

    loadSensors();
    return () => { ignore = true; };
  }, []);

  async function handleDelete(id) {
    try {
      await deleteSensor(id);
      setSensors((prev) => prev.filter((sensor) => sensor.id !== id));
    } catch (err) {
      setError(err.message);
    }
  }

  function handleCreated(newSensor) {
    setSensors((prev) => [...prev, newSensor]);
  }

  if (loading) return <p>Загрузка...</p>;
  if (error) return <p>Ошибка: {error}</p>;

  return (
    <div>
      <SensorForm onCreated={handleCreated} />
      <h2>Список датчиков</h2>
      {sensors.length === 0 ? (
        <p>Датчиков пока нет</p>
      ) : (
        sensors.map((sensor) => (
          <SensorCard key={sensor.id} sensor={sensor} onDelete={handleDelete} />
        ))
      )}
    </div>
  );
}

export default SensorList;