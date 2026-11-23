import { useCallback, useEffect, useMemo, useState } from "react";
import { MoreHorizontal, RefreshCw } from "lucide-react";

import { DeviceList } from "../components/devices/DeviceList";
import { DeviceInfo } from "../components/dashboard/DeviceInfo";
import { TelemetryCard } from "../components/dashboard/TelemetryCard";
import { MetricSwitcher } from "../components/dashboard/MetricSwitcher";
import { PlaceholderSection } from "../components/common/PlaceholderSection";

import { fetchSensors } from "../api/sensorApi";
import { fetchReadings } from "../api/telemetryApi";
import { mapSensorToUi } from "../data/metricMap";
import { sectionDescriptions } from "../data/sectionDescriptions";

export default function DashboardPage({ activeSection }) {
  const [selectedDeviceId, setSelectedDeviceId] = useState(null);
  const [activeMetricId, setActiveMetricId] = useState(null);
  const [search, setSearch] = useState("");
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const selectedDevice =
    devices.find((device) => device.id === selectedDeviceId) ??
    devices[0] ??
    null;

  const activeMetric =
    selectedDevice?.metrics.find((m) => m.id === activeMetricId) ??
    selectedDevice?.metrics[0] ??
    null;

  const handleSelectDevice = useCallback(
    (deviceId) => {
      const device = devices.find((d) => d.id === deviceId);
      if (!device) return;
      setSelectedDeviceId(deviceId);
      setActiveMetricId(device.metrics[0]?.id ?? null);
    },
    [devices],
  );

  const filteredDevices = useMemo(
    () =>
      devices.filter((device) =>
        `${device.name} ${device.location}`
          .toLowerCase()
          .includes(search.toLowerCase()),
      ),
    [devices, search],
  );

  // Загрузка списка устройств — один раз при монтировании
  useEffect(() => {
    let ignore = false;
    async function loadDevices() {
      try {
        setLoading(true);
        setError(null);
        const data = await fetchSensors();
        if (!ignore) {
          setDevices(data.map(mapSensorToUi));
          if (data.length > 0 && !selectedDeviceId) {
            setSelectedDeviceId(String(data[0].id));
            setActiveMetricId(String(data[0].metrics?.[0]?.id ?? null));
          }
        }
      } catch (err) {
        if (!ignore) setError(err.message);
      } finally {
        if (!ignore) setLoading(false);
      }
    }
    loadDevices();
    return () => {
      ignore = true;
    };
  }, []);

  // Загрузка телеметрии для выбранного устройства и метрики
  useEffect(() => {
    if (!selectedDeviceId || !activeMetricId) return;
    const device = devices.find((d) => d.id === selectedDeviceId);
    const metric = device?.metrics.find((m) => m.id === activeMetricId);
    if (!device || !metric) return;
    let ignore = false;
    async function loadReadings() {
      try {
        const readings = await fetchReadings(
          device.id,
          metric.kind.toUpperCase(),
        );
        if (!ignore && readings && readings.length > 0) {
          setDevices((prev) =>
            prev.map((d) => {
              if (d.id !== device.id) return d;
              return {
                ...d,
                metrics: d.metrics.map((m) => {
                  if (m.id !== metric.id) return m;
                  const latest = readings[0];
                  const history = readings
                    .slice()
                    .reverse()
                    .map((r) => ({
                      time: new Date(r.recordedAt).toLocaleTimeString("ru-RU", {
                        hour: "2-digit",
                        minute: "2-digit",
                      }),
                      value: r.value,
                    }));
                  return {
                    ...m,
                    value: latest?.value ?? m.value,
                    change: "—",
                    history: history.length > 0 ? history : m.history,
                  };
                }),
              };
            }),
          );
        }
      } catch (err) {
        console.warn("Не удалось загрузить телеметрию:", err.message);
      }
    }
    loadReadings();
    return () => {
      ignore = true;
    };
  }, [selectedDeviceId, activeMetricId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-[#0b1220] text-slate-100 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-flex h-8 w-8 rounded-full border-4 border-slate-700 border-t-sky-500 animate-spin mb-2" />
          <p className="text-slate-400 text-sm">Загрузка устройств...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-[#0b1220] text-slate-100 flex items-center justify-center">
        <div className="text-center p-6 rounded-2xl border border-red-900/30 bg-red-950/10">
          <p className="text-red-400 font-medium mb-2">
            Ошибка загрузки устройств
          </p>
          <p className="text-sm text-slate-400">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 rounded-lg border border-slate-600 bg-slate-800 text-sm font-medium text-slate-200 hover:bg-slate-700 transition"
          >
            Повторить
          </button>
        </div>
      </div>
    );
  }

  if (!selectedDevice) {
    return (
      <div className="min-h-screen bg-[#0b1220] text-slate-100 flex items-center justify-center">
        <div className="text-center">
          <p className="text-slate-400 text-sm">Нет доступных устройств</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0b1220] text-slate-100">
      {activeSection !== "Дашборды" ? (
        <PlaceholderSection
          title={activeSection}
          description={sectionDescriptions[activeSection]}
        />
      ) : (
        <main className="grid min-h-[calc(100vh-72px)] grid-cols-1 lg:grid-cols-[290px_minmax(0,1fr)]">
          <DeviceList
            devices={filteredDevices}
            totalCount={devices.length}
            selectedDeviceId={selectedDeviceId}
            onSelectDevice={handleSelectDevice}
            search={search}
            onSearchChange={setSearch}
          />

          <section className="relative min-w-0 overflow-hidden bg-[#0b1423]">
            <div className="relative mx-auto max-w-[1500px] p-5 sm:p-8">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <p className="text-[10px] font-bold tracking-[0.18em] text-slate-500">
                    ПРОСТРАНСТВО / ДАШБОРДЫ
                  </p>
                  <h2 className="mt-2 text-xl font-bold">Обзор телеметрии</h2>
                </div>

                <div className="flex items-center gap-2">
                  <span className="flex items-center gap-2 rounded-full border border-emerald-900 bg-emerald-950/60 px-3 py-2 text-[10px] font-bold tracking-wider text-emerald-300">
                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                    LIVE DATA
                  </span>
                  <button
                    className="grid h-9 w-9 place-items-center rounded-lg border border-slate-700 bg-slate-900/70 text-slate-400 hover:text-white"
                    aria-label="Обновить данные"
                    onClick={() => window.location.reload()}
                  >
                    <RefreshCw size={16} />
                  </button>
                  <button
                    className="grid h-9 w-9 place-items-center rounded-lg border border-slate-700 bg-slate-900/70 text-slate-400 hover:text-white"
                    aria-label="Дополнительные действия"
                  >
                    <MoreHorizontal size={18} />
                  </button>
                </div>
              </div>

              <div className="mt-8 grid items-start gap-7 xl:grid-cols-[minmax(170px,0.8fr)_minmax(350px,1.2fr)]">
                <DeviceInfo device={selectedDevice} />
                <TelemetryCard
                  device={selectedDevice}
                  metric={activeMetric}
                  metricIndex={selectedDevice.metrics.findIndex(
                    (m) => m.id === activeMetricId,
                  )}
                />
              </div>

              <MetricSwitcher
                metrics={selectedDevice.metrics}
                activeMetricId={activeMetricId}
                onChangeMetric={setActiveMetricId}
              />
            </div>
          </section>
        </main>
      )}
    </div>
  );
}
