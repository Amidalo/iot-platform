export const PlaceholderSection = ({ title, description }) => (
  <main className="grid min-h-[calc(100vh-72px)] place-items-center p-6">
    <div className="w-full max-w-lg rounded-2xl border border-slate-700 bg-slate-900 p-10 text-center">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-500">
        NEXUS IOT / РАЗДЕЛ
      </p>
      <h1 className="mt-4 text-3xl font-bold">{title}</h1>
      <p className="mt-4 text-sm leading-7 text-slate-400">{description}</p>
      <p className="mt-6 text-xs text-slate-500">
        Этот раздел пока представлен демонстрационной страницей.
      </p>
    </div>
  </main>
);
