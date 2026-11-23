import { Activity } from "lucide-react";
import { metricIcons } from "../../data/metricIcons";

export const MetricSwitcher = ({ metrics, activeMetricId, onChangeMetric }) => {
  const activeIndex = metrics.findIndex((m) => m.id === activeMetricId);
  const activeMetric = metrics[activeIndex];

  return (
    <div className="mt-8 flex flex-col justify-between gap-5 sm:flex-row sm:items-center">
      <div className="flex flex-wrap items-center gap-2">
        {metrics.map((metric, index) => {
          const Icon = metricIcons[metric.kind] || Activity;
          const isActive = metric.id === activeMetricId;

          return (
            <button
              key={metric.id}
              onClick={() => onChangeMetric(metric.id)}
              title={metric.label}
              aria-label={`Показатель ${index + 1}: ${metric.label}`}
              aria-pressed={isActive}
              className={`relative flex h-12 w-12 flex-col items-center justify-center gap-1 rounded-xl border transition ${
                isActive
                  ? "border-sky-500 bg-sky-950/70 text-sky-300 shadow-lg shadow-sky-950/30"
                  : "border-slate-700 bg-slate-900/80 text-slate-500 hover:border-sky-700 hover:text-slate-200"
              }`}
            >
              <Icon size={18} />
              <span className="text-[9px] font-bold">{index + 1}</span>
              {isActive && (
                <span className="absolute -bottom-px h-0.5 w-5 rounded bg-sky-400" />
              )}
            </button>
          );
        })}
      </div>

      <div className="flex flex-wrap items-end gap-3 sm:justify-end">
        <div>
          <p className="text-[9px] font-bold tracking-widest text-slate-500">
            ВЫБРАННЫЙ ПОКАЗАТЕЛЬ
          </p>
          <p className="mt-1 text-xs font-semibold">{activeMetric?.label}</p>
        </div>
        <span className="text-[10px] text-slate-500">
          {activeIndex + 1} / {metrics.length}
        </span>
      </div>
    </div>
  );
};
