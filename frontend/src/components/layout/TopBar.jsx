import {
  Activity,
  Bell,
  CalendarClock,
  Cpu,
  LayoutDashboard,
  Workflow,
} from "lucide-react";

const NAV_ITEMS = [
  { label: "Устройства", Icon: Cpu },
  { label: "Дашборды", Icon: LayoutDashboard },
  { label: "Уведомления", Icon: Bell },
  { label: "Автоматизация", Icon: Workflow },
  { label: "Планировщик", Icon: CalendarClock },
];

export const TopBar = ({ activeSection, onChangeSection }) => (
  <header className="relative z-10 flex min-h-[72px] flex-wrap items-center gap-5 border-b border-slate-200 bg-slate-50 px-6 py-3 text-slate-900">
    <button
      onClick={() => onChangeSection("Дашборды")}
      className="flex items-center gap-2 text-xl font-extrabold tracking-tight"
    >
      <span className="grid h-8 w-8 place-items-center rounded-lg bg-sky-100 text-sky-600">
        <Activity size={19} />
      </span>
      <span>
        My<span className="text-sky-500">.iot</span>
      </span>
    </button>

    <nav className="flex flex-1 flex-wrap items-center gap-1">
      {NAV_ITEMS.map(({ label, Icon }) => (
        <button
          key={label}
          onClick={() => onChangeSection(label)}
          className={`flex items-center gap-2 rounded-lg px-3 py-2.5 text-sm font-semibold transition ${
            activeSection === label
              ? "bg-sky-100 text-sky-700"
              : "text-slate-600 hover:bg-slate-100"
          }`}
        >
          <Icon size={17} />
          {label}
        </button>
      ))}
    </nav>
  </header>
);
