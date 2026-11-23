// src/pages/HomePage.jsx

function HomePage() {
  const navLinks = [
    { href: "/", label: "Главная" },
    { href: "/devices", label: "Устройства" },
    { href: "/dashboard", label: "Дашборд" },
  ];

  return (
    <div style={{ padding: "40px 16px", maxWidth: 1200, margin: "0 auto" }}>
      <nav className="mb-8 flex gap-4 text-sm font-medium">
        {navLinks.map((link) => (
          <a
            key={link.href}
            href={link.href}
            className="text-sky-400 hover:text-sky-300 underline"
          >
            {link.label}
          </a>
        ))}
      </nav>

      <h1>Добро пожаловать 👋</h1>
      <p className="mt-4 text-slate-300">Это главная страница IoT-платформы.</p>

      <div className="mt-8 p-6 rounded-2xl border border-slate-700 bg-[#111c2e]/95">
        <h2 className="mb-4 text-xl font-bold text-slate-100">Быстрый старт</h2>
        <ul className="list-disc list-inside text-slate-300 space-y-2">
          <li>Перейдите в раздел <b>Устройства</b> для управления датчиками.</li>
          <li>Перейдите в раздел <b>Дашборды</b> для просмотра телеметрии.</li>
        </ul>
      </div>
    </div>
  );
}

export default HomePage;
