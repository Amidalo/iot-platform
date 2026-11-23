function SensorCard({ sensor, onDelete }) {
  return (
    <div className="sensor-card">
      <h3>{sensor.name}</h3>
      <p>Тип: {sensor.type}</p>
      <p>Статус: {sensor.status}</p>
      <button onClick={() => onDelete(sensor.id)}>Удалить</button>
    </div>
  );
}

export default SensorCard;
