import type { ReactNode } from "react";
import {
  Bell,
  ChevronRight,
  CircleAlert,
  CircleCheck,
  Clock3,
  Command,
  Menu,
  Radio,
  Search,
  ShieldAlert,
  UserRound,
  X,
} from "lucide-react";
import { integration } from "./api";
import { quickViews, views, type NavItem, type ViewId } from "./data";

export type Tone = "ok" | "live" | "warn" | "bad" | "info" | "muted";

export function toneForStatus(status: string): Tone {
  if (/sağlıklı|aktif|başarılı|onaylandı|ödendi|tamamlandı/i.test(status)) return "ok";
  if (/bekliyor|işleniyor|kesildi|planlandı/i.test(status)) return "live";
  if (/risk|yaklaşıyor|kısmi|askıda/i.test(status)) return "warn";
  if (/hata|başarısız|gecikmiş|reddedildi|bağlantı yok/i.test(status)) return "bad";
  return "muted";
}

export function Status({ label, tone = toneForStatus(label) }: { label: string; tone?: Tone }) {
  const Icon = tone === "ok" ? CircleCheck : tone === "bad" ? CircleAlert : tone === "warn" ? ShieldAlert : tone === "live" ? Radio : Clock3;
  return (
    <span className={`status status--${tone}`}>
      <Icon size={13} aria-hidden="true" />
      {label}
    </span>
  );
}

function currentTimeLabel() {
  return new Intl.DateTimeFormat("tr-TR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(new Date());
}

export function Panel({
  title,
  eyebrow,
  action,
  className = "",
  children,
}: {
  title?: string;
  eyebrow?: string;
  action?: ReactNode;
  className?: string;
  children: ReactNode;
}) {
  return (
    <section className={`panel ${className}`}>
      {(title || eyebrow || action) && (
        <header className="panel__header">
          <div>
            {eyebrow && <span className="eyebrow">{eyebrow}</span>}
            {title && <h2>{title}</h2>}
          </div>
          {action}
        </header>
      )}
      {children}
    </section>
  );
}

export function PageHeader({
  view,
  action,
}: {
  view: NavItem;
  action?: ReactNode;
}) {
  return (
    <div className="page-header">
      <div>
        <span className="eyebrow">{view.eyebrow}</span>
        <h1>{view.label}</h1>
        <p>{view.description}</p>
      </div>
      <div className="page-header__actions">
        <span className="freshness">Son güncelleme <b>{currentTimeLabel()}</b></span>
        {action}
      </div>
    </div>
  );
}

export function Button({
  children,
  variant = "secondary",
  disabled,
  title,
  onClick,
  type = "button",
}: {
  children: ReactNode;
  variant?: "primary" | "secondary" | "quiet" | "danger";
  disabled?: boolean;
  title?: string;
  onClick?: () => void;
  type?: "button" | "submit";
}) {
  return (
    <button className={`button button--${variant}`} disabled={disabled} title={title} onClick={onClick} type={type}>
      {children}
    </button>
  );
}

export function IntegrationTag() {
  return integration.live ? (
    <Status label="Canlı servis" tone="ok" />
  ) : (
    <span className="integration-tag" title="Backend API bağlantısı VITE_ENABLE_LIVE_API ile etkinleştirilir">
      <Radio size={12} /> Demo veri
    </span>
  );
}

export function AppShell({
  active,
  onNavigate,
  onOpenCommand,
  children,
}: {
  active: ViewId;
  onNavigate: (view: ViewId) => void;
  onOpenCommand: () => void;
  children: ReactNode;
}) {
  return (
    <div className={`app-shell ${active === "ops" ? "app-shell--ops" : ""}`}>
      <aside className="nav-rail" aria-label="Ana navigasyon">
        <button className="brand" onClick={() => onNavigate("overview")} aria-label="TelcoX ana sayfa">
          <img src="/branding/telcox-signal-atlas-mark.png" alt="" />
          <span><strong>TELCOX</strong><small>SIGNAL ATLAS</small></span>
        </button>
        <nav>
          {views.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              className={active === id ? "nav-item nav-item--active" : "nav-item"}
              onClick={() => onNavigate(id)}
              title={label}
              aria-current={active === id ? "page" : undefined}
            >
              <Icon size={18} /> <span>{label}</span>
            </button>
          ))}
        </nav>
        <button className="nav-profile" title="Oturumu kapat" onClick={() => onNavigate("login")}>
          <UserRound size={18} /><span><b>Tamer Akdeniz</b><small>Platform Admin</small></span>
        </button>
      </aside>
      <div className="workspace">
        <header className="utility-bar">
          <div className="crumb"><Menu size={17} /><span>TelcoX</span><ChevronRight size={14} /><b>{views.find((v) => v.id === active)?.label ?? "Detay"}</b></div>
          <button className="command-trigger" onClick={onOpenCommand}>
            <Search size={16} /><span>Müşteri, MSISDN, fatura, sipariş ara...</span><kbd>⌘ K</kbd>
          </button>
          <div className="utility-actions">
            <IntegrationTag />
            <span className="environment">{integration.environment}</span>
            <button className="icon-button" aria-label="Bildirimler"><Bell size={18} /><i>3</i></button>
            <button className="avatar" aria-label="Profil menüsü">TA</button>
          </div>
        </header>
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}

export function CommandPalette({
  onClose,
  onNavigate,
}: {
  onClose: () => void;
  onNavigate: (view: ViewId) => void;
}) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="command-palette" role="dialog" aria-modal="true" aria-label="Global arama" onMouseDown={(event) => event.stopPropagation()}>
        <div className="command-palette__search"><Command size={18} /><input autoFocus aria-label="Arama" placeholder="Bir görünüm veya kayıt ara..." /><button onClick={onClose} aria-label="Kapat"><X size={17} /></button></div>
        <span className="eyebrow">Hızlı geçiş</span>
        <div className="command-results">
          {[...quickViews, ...views.slice(0, 6)].map(({ id, label, icon: Icon }) => (
            <button key={id} onClick={() => { onNavigate(id); onClose(); }}><Icon size={17} /><span>{label}</span><ChevronRight size={15} /></button>
          ))}
        </div>
        <footer><kbd>↑↓</kbd> gezin <kbd>↵</kbd> aç <kbd>esc</kbd> kapat</footer>
      </section>
    </div>
  );
}

export function Notice({
  kind = "integration",
  children,
}: {
  kind?: "integration" | "warning" | "error";
  children: ReactNode;
}) {
  return <div className={`notice notice--${kind}`}><CircleAlert size={17} />{children}</div>;
}

export function Metric({ label, value, meta, tone = "muted" }: { label: string; value: string; meta: string; tone?: Tone }) {
  return <div className={`metric metric--${tone}`}><span>{label}</span><strong>{value}</strong><small>{meta}</small></div>;
}

export function DataTable({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <div className={`table-wrap ${className}`}><table>{children}</table></div>;
}
