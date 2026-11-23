import { useState } from 'react';
import { createSensor } from '../api/sensorApi';

const SENSOR_TYPES = ['TEMPERATURE', 'HUMIDITY', 'PRESSURE', 'MOTION', 'LIGHT'];
const SENSOR_STATUSES = ['ONLINE', 'OFFLINE'];

function SensorForm({ onCreated }) {
  const [name, setName] = useState('');
  const [type, setType] = useState(SENSOR_TYPES[0]);
  const [status, setStatus] = useState(SENSOR_STATUSES[0]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const created = await createSensor({ name, type, status });
      onCreated(created);
      setName('');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Название датчика"
        value={name}
        onChange={(e) => setName(e.target.value)}
        required
      />
      <select value={type} onChange={(e) => setType(e.target.value)}>
        {SENSOR_TYPES.map((t) => (
          <option key={t} value={t}>{t}</option>
        ))}
      </select>
      <select value={status} onChange={(e) => setStatus(e.target.value)}>
        {SENSOR_STATUSES.map((s) => (
          <option key={s} value={s}>{s}</option>
        ))}
      </select>
      <button type="submit" disabled={submitting}>
        {submitting ? 'Добавление...' : 'Добавить датчик'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
}

export default SensorForm;