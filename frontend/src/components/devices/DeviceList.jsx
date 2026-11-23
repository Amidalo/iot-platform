import { DeviceListItem } from "./DeviceListItem";

export const DeviceList = ({
  devices,
  totalCount,
  selectedDeviceId,
  onSelectDevice,
}) => (
  <aside className="flex h-full min-h-0 flex-col overflow-hidden border-b border-slate-700 bg-[#101a2b] p-5 lg:border-r lg:border-b-0">
    {/* Шапка сайдбара — не прокручивается */}
    <div className="mb-5 flex shrink-0 items-center justify-between">
      <div>
        <p className="text-[10px] font-bold tracking-[0.18em] text-slate-500">
          МОНИТОРИНГ
        </p>
        <h3 className="mt-2 text-2xl font-bold">Устройства</h3>
      </div>
      <span className="rounded-lg border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm">
        {totalCount}
      </span>
    </div>

    <div className="mb-2 flex shrink-0 justify-between text-[10px] font-bold tracking-widest text-slate-500">
      <span>ВСЕ УСТРОЙСТВА</span>
      <span>{devices.length}</span>
    </div>

    {/* Список — единственная прокручиваемая область */}
    <div className="flex min-h-0 flex-1 flex-col gap-2 overflow-y-auto pr-1 [scrollbar-color:#1e293b_transparent] [scrollbar-width:thin]">
      {devices.map((device) => (
        <DeviceListItem
          key={device.id}
          device={device}
          isSelected={device.id === selectedDeviceId}
          onSelect={() => onSelectDevice(device.id)}
        />
      ))}
      {devices.length === 0 && (
        <p className="p-4 text-sm text-slate-500">Устройства не найдены.</p>
      )}
    </div>

    <div className="mt-4 flex shrink-0 items-center gap-2 border-t border-slate-700 pt-4 text-[10px] text-slate-500">
      <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
      Система работает штатно
    </div>
  </aside>
);
