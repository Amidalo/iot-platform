import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  ArrowDownRight,
  ArrowUpRight,
  Clock3,
  ShieldCheck,
} from "lucide-react";

export const TelemetryCard = ({ device, metric, metricIndex }) => (
  <div className="min-w-0 rounded-2xl border border-slate-700 bg-[#111c2e]/95 p-4 shadow-2xl sm:p-6">
    <div className="flex items-start justify-between border-b border-slate-700 pb-4">
      <div>
        <p className="text-[9px] font-bold tracking-[0.16em] text-slate-500">
          ТЕКУЩИЙ ПОКАЗАТЕЛЬ
        </p>
        <h4 className="mt-2 text-base font-bold">{metric.label}</h4>
      </div>
      <span className="rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-[10px] text-slate-400">
        #{String(metricIndex + 1).padStart(2, "0")}
      </span>
    </div>

      <div className="flex flex-wrap items-end justify-between gap-3 py-5">
      <div className="text-5xl font-bold tracking-tight">
        {metric.value != null
          ? metric.value.toLocaleString("ru-RU", { maximumFractionDigits: 2 })
          : "—"}
        <span className="ml-2 text-base font-normal text-slate-400">
          {metric.unit}
        </span>
      </div>

      <span
        className={`flex items-center gap-1 rounded-md border px-2 py-1.5 text-[10px] ${
          metric.change?.startsWith("-")
            ? "border-orange-900 bg-orange-950/30 text-orange-300"
            : "border-sky-900 bg-sky-950/50 text-sky-300"
        }`}
      >
        {metric.change?.startsWith("-") ? (
          <ArrowDownRight size={14} />
        ) : (
          <ArrowUpRight size={14} />
        )}
        {metric.change ?? "—"}
      </span>
    </div>

    <div className="mb-2 flex flex-wrap justify-between gap-2 text-[10px] text-slate-400">
      <span>История показаний</span>
      <span className="flex items-center gap-1.5">
        <Clock3 size={12} />
        Последние 6 часов
      </span>
    </div>

    <div className="h-[210px] w-full">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart
          data={metric.history}
          margin={{ top: 12, right: 8, left: -18, bottom: 0 }}
        >
          <defs>
            <linearGradient id="metricFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#36a3ff" stopOpacity={0.3} />
              <stop offset="95%" stopColor="#36a3ff" stopOpacity={0} />
            </linearGradient>
          </defs>

          <CartesianGrid
            stroke="rgba(148,163,184,.12)"
            vertical={false}
            strokeDasharray="3 5"
          />
          <XAxis
            dataKey="time"
            tick={{ fill: "#7f8da5", fontSize: 10 }}
            tickLine={false}
            axisLine={false}
            minTickGap={24}
          />
          <YAxis
            tick={{ fill: "#7f8da5", fontSize: 10 }}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip
            contentStyle={{
              background: "#101a2c",
              border: "1px solid #2b3a51",
              borderRadius: 10,
              color: "#e8eef8",
              fontSize: 12,
            }}
            formatter={(value) => [`${value} ${metric.unit}`, metric.label]}
          />
          <Area
            type="monotone"
            dataKey="value"
            stroke="#36a3ff"
            strokeWidth={2.5}
            fill="url(#metricFill)"
            activeDot={{ r: 4, strokeWidth: 0, fill: "#36a3ff" }}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>

    <div className="mt-3 flex flex-wrap justify-between gap-4 border-t border-slate-700 pt-4">
      <div>
        <p className="text-[9px] font-bold tracking-widest text-slate-500">
          СОСТОЯНИЕ
        </p>
        <p className="mt-2 flex items-center gap-1.5 text-[10px] text-slate-300">
          <ShieldCheck size={14} className="text-emerald-400" />
          {device.status === "online" ? "Норма" : "Нет данных"}
        </p>
      </div>
      <div>
        <p className="text-[9px] font-bold tracking-widest text-slate-500">
          ОБНОВЛЕНИЕ
        </p>
        <p className="mt-2 text-[10px] text-slate-300">{device.lastSeen}</p>
      </div>
    </div>
  </div>
);
