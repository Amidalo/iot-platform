export const DeviceInfo = ({ device }) => (
  <div className="pt-3">
    <p className="flex items-center gap-2 text-[10px] font-bold tracking-widest text-slate-400">
      <span className="h-1.5 w-1.5 rounded-full bg-sky-400" />
      {device.id.toUpperCase()}
      <span className="text-slate-600">/</span>
      ДАТЧИКИ
    </p>

    <h3 className="mt-5 text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
      {device.name}
    </h3>

    <p className="mt-3 text-xs text-slate-400">{device.location}</p>

    <div
      className={`mt-6 inline-flex items-center gap-2 rounded-lg border px-3 py-2 text-[10px] ${
        device.status === "online"
          ? "border-emerald-900 bg-emerald-950/40 text-emerald-300"
          : "border-red-900 bg-red-950/30 text-red-300"
      }`}
    >
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {device.status === "online" ? "Устройство в сети" : "Нет соединения"}
    </div>
  </div>
);
