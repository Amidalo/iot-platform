import { ChevronRight, RadioTower, Wifi, WifiOff } from "lucide-react";

export const DeviceListItem = ({ device, isSelected, onSelect }) => {
  const isOnline = device.status === "online";

  return (
    <button
      onClick={onSelect}
      className={`flex w-full items-start gap-3 rounded-xl border p-3 text-left transition ${
        isSelected
          ? "border-sky-800 bg-[#172b43] shadow-[inset_3px_0_#2798f5]"
          : "border-transparent hover:bg-slate-800"
      }`}
    >
      <span
        className={`grid h-9 w-9 shrink-0 place-items-center rounded-lg ${
          isOnline ? "bg-sky-950 text-sky-400" : "bg-slate-800 text-slate-500"
        }`}
      >
        <RadioTower size={19} />
      </span>

      <span className="min-w-0 flex-1">
        <span className="block text-xs font-bold text-slate-100">
          {device.name}
        </span>
        <span className="mt-1 block text-[10px] text-slate-500">
          {device.location}
        </span>
        <span
          className={`mt-2 flex items-center gap-1.5 text-[10px] ${
            isOnline ? "text-emerald-400" : "text-slate-500"
          }`}
        >
          {isOnline ? <Wifi size={12} /> : <WifiOff size={12} />}
          {isOnline ? "В сети" : "Не в сети"}
          <span className="h-1 w-1 rounded-full bg-current" />
        </span>
      </span>

      <span className="flex items-center gap-1 text-slate-500">
        <span className="grid h-5 min-w-5 place-items-center rounded bg-slate-700 px-1 text-[10px]">
          {device.metrics.length}
        </span>
        <ChevronRight size={15} />
      </span>
    </button>
  );
};
