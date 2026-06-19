import { useEffect, useState } from "react";
import { AppShell, CommandPalette } from "./components";
import { type ViewId } from "./data";
import { ScreenRenderer } from "./screens";

const validViews = new Set<ViewId>([
  "login", "overview", "customers", "customer360", "kyc", "catalog",
  "order-new", "order-saga", "subscriptions", "usage", "billing", "invoice",
  "payments", "notifications", "tickets", "admin", "ops", "states",
]);

function viewFromHash(): ViewId {
  const candidate = window.location.hash.replace(/^#\/?/, "") as ViewId;
  return validViews.has(candidate) ? candidate : "overview";
}

export default function App() {
  const [active, setActive] = useState<ViewId>(viewFromHash);
  const [commandOpen, setCommandOpen] = useState(false);

  useEffect(() => {
    const onHashChange = () => setActive(viewFromHash());
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setCommandOpen(true);
      }
      if (event.key === "Escape") setCommandOpen(false);
    };
    window.addEventListener("hashchange", onHashChange);
    window.addEventListener("keydown", onKeyDown);
    return () => {
      window.removeEventListener("hashchange", onHashChange);
      window.removeEventListener("keydown", onKeyDown);
    };
  }, []);

  const navigate = (view: ViewId) => {
    window.location.hash = view;
    setActive(view);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  if (active === "login") {
    return <ScreenRenderer id={active} onNavigate={navigate} />;
  }

  return (
    <AppShell active={active} onNavigate={navigate} onOpenCommand={() => setCommandOpen(true)}>
      <ScreenRenderer id={active} onNavigate={navigate} />
      {commandOpen && <CommandPalette onClose={() => setCommandOpen(false)} onNavigate={navigate} />}
    </AppShell>
  );
}
