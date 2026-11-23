import { useState } from "react";
import { TopBar } from "./components/layout/TopBar";
import DashboardPage from "./pages/DashboardPage";
import DevicesPage from "./pages/DevicesPage";

export default function App() {
  const [activeSection, setActiveSection] = useState("Дашборды");

  const page =
    activeSection === "Устройства" ? (
      <DevicesPage />
    ) : (
      <DashboardPage activeSection={activeSection} />
    );

  return (
    <div className="min-h-screen bg-[#0b1220] text-slate-100">
      <TopBar
        activeSection={activeSection}
        onChangeSection={setActiveSection}
      />
      {page}
    </div>
  );
}
