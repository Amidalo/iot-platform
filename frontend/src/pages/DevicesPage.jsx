import { useEffect, useState } from "react";
import {
  fetchSensors,
  createSensor,
  deleteSensor,
  updateSensor,
} from "../api/sensorApi";
import { mapSensorToUi } from "../data/metricMap";

const STATUS_LABELS = {
  online: "В сети",
  offline: "Не в сети",
};

const STATUS_BADGE = {
  online: "bg-emerald-950/40 border-emerald-900 text-emerald-300",
  offline: "bg-red-950/30 border-red-900 text-red-300",
};

const METRIC_TYPES = [
  { value: "TEMPERATURE", label: "Температура" },
  { value: "HUMIDITY", label: "Влажность" },
  { value: "PRESSURE", label: "Давление" },
  { value: "MOTION", label: "Движение" },
  { value: "LIGHT", label: "Освещённость" },
];

export default function DevicesPage() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [formOpen, setFormOpen] = useState(false);
  const [name, setName] = useState("");
  const [status, setStatus] = useState("ONLINE");
  const [metrics, setMetrics] = useState(["TEMPERATURE"]);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);

  useEffect(() => {
    let ignore = false;
    async function load() {
      try {
        setLoading(true);
        const data = await fetchSensors();
        if (!ignore) {
          setDevices(data.map(mapSensorToUi));
          setError(null);
        }
      } catch (err) {
        if (!ignore) setError(err.message);
      } finally {
        if (!ignore) setLoading(false);
      }
    }
    load();
    return () => { ignore = true; };
  }, []);

  async function handleDelete(id) {
    if (!window.confirm("Удалить устройство?")) return;
    try {
      await deleteSensor(id);
      setDevices((prev) => prev.filter((d) => d.id !== String(id)));
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      const created = await createSensor({
        name,
        metrics: new Set(metrics),
        status,
      });
      setDevices((prev) => [...prev, mapSensorToUi(created)]);
      setName("");
      setStatus("ONLINE");
      setMetrics(["TEMPERATURE"]);
      setFormOpen(false);
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(device) {
    const newName = window.prompt("Новое название:", device.name);
    if (newName === null || newName.trim() === "") return;
    try {
      await updateSensor(device.id, {
        name: newName.trim(),
        metrics: new Set(device.metrics.map((m) => m.label)),
        status: device.status === "online" ? "ONLINE" : "OFFLINE",
      });
      setDevices((prev) =>
        prev.map((d) =>
          d.id === device.id ? { ...d, name: newName.trim() } : d
        )
      );
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <div className="p-6 text-center">Загрузка устройств...</div>;
  if (error) return <div className="p-6 text-center text-red-400">Ошибка: {error}</div>;

  return (
    <div className="min-h-screen bg-[#0b1220] text-slate-100 p-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <p className="text-[10px] font-bold tracking-[0.18em] text-slate-500 uppercase">
            Устройства
          </p>
          <h1 className="mt-1 text-2xl font-bold">Управление устройствами</h1>
        </div>
        <button
          onClick={() => setFormOpen(true)}
          className="flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-600 bg-slate-800 text-sm font-medium text-slate-200 hover:bg-slate-700 hover:text-white transition"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          Добавить устройство
        </button>
      </div>

      {formOpen && (
        <div className="mb-6 rounded-2xl border border-slate-700 bg-[#111c2e]/95 p-6 shadow-lg">
          <h3 className="mb-4 text-lg font-bold">Новое устройство</h3>
          <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-end">
            <div className="col-span-1 sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-400 mb-1">
                Название
              </label>
              <input
                type="text"
                className="w-full px-3 py-2 rounded-lg border border-slate-600 bg-slate-900 text-slate-100 focus:border-sky-500 focus:outline-none"
                placeholder="Например: Метеостанция №1"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1">
                Статус
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-600 bg-slate-900 text-slate-100 focus:border-sky-500 focus:outline-none"
              >
                <option value="ONLINE">Онлайн</option>
                <option value="OFFLINE">Оффлайн</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1">
                Метрики
              </label>
              <select
                multiple
                value={metrics}
                onChange={(e) =>
                  setMetrics(Array.from(e.target.selectedOptions, (o) => o.value))
                }
                className="w-full px-3 py-2 rounded-lg border border-slate-600 bg-slate-900 text-slate-100 focus:border-sky-500 focus:outline-none min-h-[100px]"
              >
                {METRIC_TYPES.map((m) => (
                  <option key={m.value} value={m.value}>
                    {m.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-span-3 flex gap-2">
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 px-4 py-2 rounded-lg bg-sky-600 text-white font-medium hover:bg-sky-500 disabled:opacity-50 transition"
              >
                {submitting ? "Создание..." : "Создать"}
              </button>
              <button
                type="button"
                onClick={() => setFormOpen(false)}
                className="flex-1 px-4 py-2 rounded-lg border border-slate-600 bg-slate-800 text-slate-300 hover:bg-slate-700 transition"
              >
                Отмена
              </button>
            </div>
            {formError && (
              <div className="col-span-3 mt-2 text-sm text-red-400">{formError}</div>
            )}
          </form>
        </div>
      )}

      <div className="rounded-2xl border border-slate-700 bg-[#111c2e]/95 p-6 shadow-lg overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-700">
              <th className="pb-3 text-xs font-bold uppercase tracking-wider text-slate-500">
                Название
              </th>
              <th className="pb-3 text-xs font-bold uppercase tracking-wider text-slate-500">
                Статус
              </th>
              <th className="pb-3 text-xs font-bold uppercase tracking-wider text-slate-500">
                Метрики
              </th>
              <th className="pb-3 text-xs font-bold uppercase tracking-wider text-slate-500 text-right">
                Действия
              </th>
            </tr>
          </thead>
          <tbody>
            {devices.length === 0 ? (
              <tr>
                <td
                  colSpan={4}
                  className="p-8 text-center text-slate-500"
                >
                  Устройств пока нет. Добавьте первое устройство.
                </td>
              </tr>
            ) : (
              devices.map((device) => (
                <tr
                  key={device.id}
                  className="border-b border-slate-800 hover:bg-slate-900/50 transition"
                >
                  <td className="py-4">
                    <div className="font-medium text-slate-100">{device.name}</div>
                    <div className="text-[11px] text-slate-500 mt-1">
                      ID: {device.id}
                    </div>
                  </td>
                  <td className="py-4">
                    <span
                      className={`inline-flex items-center gap-1.5 rounded-full border px-2 py-0.5 text-[10px] font-bold ${
                        STATUS_BADGE[device.status]
                      }`}
                    >
                      <span
                        className={`h-1.5 w-1.5 rounded-full ${
                          device.status === "online"
                            ? "bg-emerald-400"
                            : "bg-red-400"
                        }`}
                      />
                      {STATUS_LABELS[device.status]}
                    </span>
                  </td>
                  <td className="py-4 text-slate-300">
                    {device.metrics
                      .map((m) => m.label)
                      .join(", ") || "—"}
                  </td>
                  <td className="py-4 text-right">
                    <button
                      onClick={() => handleUpdate(device)}
                      className="text-xs text-sky-400 hover:text-sky-300 underline mr-3"
                    >
                      Редактировать
                    </button>
                    <button
                      onClick={() => handleDelete(device.id)}
                      className="text-xs text-red-400 hover:text-red-300 underline"
                    >
                      Удалить
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}