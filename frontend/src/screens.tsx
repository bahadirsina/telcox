import { useEffect, useState, type KeyboardEvent, type ReactNode } from "react";
import {
  Activity,
  ArrowDownToLine,
  ArrowRight,
  BadgeCheck,
  BellRing,
  Boxes,
  Check,
  ChevronRight,
  CircleDashed,
  Clock3,
  CloudOff,
  Code2,
  CreditCard,
  Database,
  FileCheck2,
  FileText,
  Filter,
  Fingerprint,
  Gauge,
  GitBranch,
  KeyRound,
  LockKeyhole,
  Mail,
  MessageSquareText,
  MoreHorizontal,
  Network,
  PackageCheck,
  PauseCircle,
  Play,
  Plus,
  Radio,
  ReceiptText,
  RefreshCw,
  Search,
  Send,
  Server,
  ShieldCheck,
  ShoppingCart,
  Signal,
  Smartphone,
  TriangleAlert,
  UploadCloud,
  UserRoundCheck,
  Users,
  WifiOff,
  XCircle,
  Zap,
} from "lucide-react";
import { integration } from "./api";
import { Button, DataTable, Metric, Notice, PageHeader, Panel, Status, toneForStatus, type Tone } from "./components";
import { customers as fallbackCustomers, events, invoices as fallbackInvoices, views, type NavItem, type ViewId } from "./data";
import {
  fallbackCustomer360View,
  fallbackDashboardView,
  fallbackPlans,
  fallbackPlatformOpsView,
  fallbackSystemStatesView,
  fallbackTicketView,
  fallbackUsageView,
  loadCatalogPlans,
  loadCustomer360View,
  loadCustomerRows,
  loadDashboardView,
  loadInvoices,
  loadPlatformOpsView,
  loadSystemStatesView,
  loadTicketView,
  loadUsageView,
  useLiveResource,
  type CustomerRow,
} from "./liveData";

type ScreenProps = {
  onNavigate: (view: ViewId) => void;
};

const detailViews: Record<Exclude<ViewId, (typeof views)[number]["id"] | "login">, NavItem> = {
  customer360: { id: "customer360", label: "Müşteri 360", eyebrow: "Customer service / CUS-10482", description: "Müşteri, abonelik ve finansal bağlamı tek görünümde inceleyin.", icon: Users },
  kyc: { id: "kyc", label: "KYC inceleme", eyebrow: "Customer service / Kimlik doğrulama", description: "Girilen bilgiler ile belge verisini kontrollü biçimde karşılaştırın.", icon: ShieldCheck },
  "order-saga": { id: "order-saga", label: "Sipariş saga izleyici", eyebrow: "Order service / ORD-26-10482", description: "Servisler arası ilerlemeyi, retry ve kompansasyonları izleyin.", icon: GitBranch },
  invoice: { id: "invoice", label: "Fatura detayı", eyebrow: "Billing service / FTR-2026-061842", description: "Değiştirilemez fatura verisi, PDF ve tahsilat geçmişi.", icon: ReceiptText },
};

const disabledTitle = "Backend işlevi hazır olduğunda VITE_ENABLE_LIVE_API=true ile etkinleştirilecek";

function activateRow(event: KeyboardEvent<HTMLTableRowElement>, action: () => void) {
  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    action();
  }
}

function LiveAction({ children, variant = "primary" }: { children: ReactNode; variant?: "primary" | "danger" | "secondary" }) {
  return <Button variant={variant} disabled={!integration.live} title={!integration.live ? disabledTitle : undefined}>{children}</Button>;
}

function ScreenHeader({ id, action }: { id: ViewId; action?: ReactNode }) {
  const view = views.find((item) => item.id === id) ?? detailViews[id as keyof typeof detailViews];
  return <PageHeader view={view} action={action} />;
}

function EmptyLink({ children, onClick }: { children: ReactNode; onClick: () => void }) {
  return <button className="text-link" onClick={onClick}>{children}<ChevronRight size={14} /></button>;
}

function useRefreshTick(intervalMs = 15000) {
  const [tick, setTick] = useState(0);
  useEffect(() => {
    const timer = window.setInterval(() => setTick((value) => value + 1), intervalMs);
    return () => window.clearInterval(timer);
  }, [intervalMs]);
  return [tick, () => setTick((value) => value + 1)] as const;
}

export function LoginScreen({ onNavigate }: ScreenProps) {
  return (
    <div className="login-screen">
      <section className="login-form">
        <div className="login-brand"><img src="/branding/telcox-signal-atlas-mark.png" alt="TelcoX Signal Atlas" /><div><strong>TELCOX</strong><span>SIGNAL ATLAS</span></div></div>
        <span className="eyebrow">Güvenli operasyon erişimi</span>
        <h1>Ağın bütün sinyalleri, tek operasyon haritasında.</h1>
        <p>TelcoX çalışan kimliğinizle giriş yapın. Hassas veri erişimleri ve tüm yönetim işlemleri audit günlüğüne kaydedilir.</p>
        <div className="login-field"><label>Kurumsal e-posta</label><input value="tamer.akdeniz@telcox.local" readOnly /></div>
        <Button variant="primary" onClick={() => onNavigate("overview")}><KeyRound size={17} /> Demo oturumu ile devam et</Button>
        <LiveAction variant="secondary"><ShieldCheck size={17} /> Keycloak / SSO ile giriş</LiveAction>
        <div className="login-security"><LockKeyhole size={16} /><span>Oturumlar 15 dakika hareketsizlikten sonra sonlandırılır. Ortam: <b>{integration.environment}</b></span></div>
      </section>
      <section className="signal-field" aria-label="TelcoX ağ haritası görseli">
        <div className="signal-field__orb orb-a" /><div className="signal-field__orb orb-b" /><div className="signal-field__orb orb-c" />
        <div className="signal-route route-a"><i /><i /><i /><i /></div>
        <div className="signal-field__copy"><span className="eyebrow">19 Haz 2026 · 14:32</span><strong>10 servis çevrimiçi</strong><p>Kafka throughput 4.821 msg/s<br />Açık SLA riski 7</p></div>
      </section>
    </div>
  );
}

function PulseRibbon() {
  return (
    <section className="pulse-ribbon">
      <div className="pulse-ribbon__lead"><Radio size={17} /><span><b>Network Pulse</b><small>Son 60 saniye</small></span></div>
      <div><span>Servis sağlığı</span><b>9 / 10</b><Status label="1 riskli" tone="warn" /></div>
      <div><span>Kafka throughput</span><b>4.821 <small>msg/s</small></b><svg viewBox="0 0 110 24"><path d="M0 18 L12 12 L21 16 L35 6 L47 13 L60 8 L72 17 L86 4 L99 9 L110 3" /></svg></div>
      <div><span>Açık SLA riski</span><b>7</b><small>2 kritik · 5 yakın</small></div>
      <div><span>Bill-run</span><b>Haziran</b><Status label="%72 tamamlandı" tone="live" /></div>
    </section>
  );
}

export function OverviewScreen({ onNavigate }: ScreenProps) {
  const liveDashboard = useLiveResource(fallbackDashboardView, loadDashboardView);
  const dashboard = liveDashboard.data;

  return (
    <>
      <ScreenHeader id="overview" action={<div className="action-row"><Button onClick={() => onNavigate("kyc")}><UserRoundCheck size={16} /> KYC kuyruğu</Button><Button variant="primary" onClick={() => onNavigate("order-new")}><Plus size={16} /> Yeni hat siparişi</Button></div>} />
      {liveDashboard.error && <Notice kind="warning">Canlı dashboard özeti alınamadı; son bilinen operasyon snapshot'ı gösteriliyor.</Notice>}
      <PulseRibbon />
      <div className="overview-grid">
        <Panel eyebrow="Bugünün akışı" title="Başvurudan aktivasyona" className="lifecycle-panel" action={<span className="mono muted">{liveDashboard.live ? "Canlı BFF" : "Snapshot"}</span>}>
          <div className="lifecycle-flow">
            {dashboard.lifecycle.map((step, index) => (
              <div className="lifecycle-step" key={step.label}><div className={`signal-node signal-node--${step.tone}`}><span>{index + 1}</span></div><b>{step.label}</b><strong>{step.value}</strong><small>{step.meta}</small></div>
            ))}
          </div>
          <div className="flow-summary"><span>Medyan aktivasyon</span><b>04:18</b><span>Hedef altında</span><Status label="Başarılı" tone="ok" /></div>
        </Panel>
        <Panel eyebrow="Canlı akış" title="Domain eventleri" className="event-panel" action={<Radio size={15} className="live-icon" />}>
          <div className="event-stream">{events.map((event) => <button key={event.id}><i className={`dot dot--${event.tone}`} /><span><b>{event.type}</b><small className="mono">{event.id}</small></span><time>{event.time}</time></button>)}</div>
        </Panel>
        <Panel eyebrow="Exceptions first" title="Operasyon müdahalesi gerekenler" className="exception-panel">
          <div className="exception-list">
            <button onClick={() => onNavigate("payments")}><span className="exception-icon bad"><CreditCard /></span><span><b>18 başarısız ödeme</b><small>4 kayıt son retry penceresinde</small></span><strong>128.450 TL</strong><ChevronRight /></button>
            <button onClick={() => onNavigate("tickets")}><span className="exception-icon warn"><Clock3 /></span><span><b>7 SLA riski</b><small>2 talep 30 dakikanın altında</small></span><strong>00:24:18</strong><ChevronRight /></button>
            <button onClick={() => onNavigate("order-saga")}><span className="exception-icon live"><GitBranch /></span><span><b>6 saga bekliyor</b><small>order-service ödeme onayı bekliyor</small></span><strong>En eski 12 dk</strong><ChevronRight /></button>
            <button onClick={() => onNavigate("kyc")}><span className="exception-icon info"><Fingerprint /></span><span><b>42 KYC incelemede</b><small>11 belge otomatik eşleşmedi</small></span><strong>+8 bugün</strong><ChevronRight /></button>
          </div>
        </Panel>
        <Panel eyebrow="Kanal özeti" title="Aktif operasyon hacmi" className="metric-band">
          <div className="metric-grid">{dashboard.metrics.map((metric) => <Metric key={metric.label} label={metric.label} value={metric.value} meta={metric.meta} tone={metric.tone} />)}</div>
        </Panel>
      </div>
    </>
  );
}

export function CustomersScreen({ onNavigate }: ScreenProps) {
  const liveCustomers = useLiveResource<CustomerRow[]>(fallbackCustomers, loadCustomerRows);
  const customerRows = liveCustomers.data;
  const selected = customerRows[0] ?? fallbackCustomers[0];
  const selectedInitials = selected.name.split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toUpperCase();

  return (
    <>
      <ScreenHeader id="customers" action={<Button variant="primary" onClick={() => onNavigate("kyc")}><Plus size={16} /> Yeni müşteri</Button>} />
      {liveCustomers.error && <Notice kind="warning">Canlı müşteri verisi alınamadı; son bilinen kayıt gösteriliyor.</Notice>}
      <div className="query-bar"><div className="search-box"><Search size={17} /><input placeholder="Ad, müşteri ID, TCKN veya MSISDN ara" /></div><Button><Filter size={16} /> Filtreler <span className="count">3</span></Button><div className="saved-views"><button>KYC bekleyenler</button><button>Borç riski</button><button>Yeni aktivasyonlar</button></div></div>
      <div className="split-list">
        <Panel className="table-panel" title="Müşteri kayıtları" eyebrow={`${customerRows.length} sonuç`} action={<span className="mono muted">{liveCustomers.loading ? "Yükleniyor" : liveCustomers.live ? "Canlı DB" : "Snapshot"}</span>}>
          <DataTable><thead><tr><th>Müşteri</th><th>KYC</th><th>Abonelik</th><th>Şehir</th><th>Hat</th><th>Açık talep</th><th>Bakiye</th><th /></tr></thead><tbody>{customerRows.map((customer) => <tr key={customer.id} tabIndex={0} onClick={() => onNavigate("customer360")} onKeyDown={(event) => activateRow(event, () => onNavigate("customer360"))}><td><b>{customer.name}</b><small className="mono">{customer.id} · {customer.msisdn}</small></td><td><Status label={customer.kyc} /></td><td><Status label={customer.status} /></td><td>{customer.city}</td><td className="mono">{customer.lines}</td><td className="mono">{customer.tickets}</td><td className="mono"><b>{customer.balance}</b></td><td><MoreHorizontal size={16} /></td></tr>)}</tbody></DataTable>
        </Panel>
        <aside className="inspector customer-preview">
          <span className="eyebrow">Seçili müşteri</span><div className="inspector__title"><div className="initials">{selectedInitials}</div><div><h2>{selected.name}</h2><span className="mono">{selected.id}</span></div><Status label={selected.status} /></div>
          <dl className="detail-list"><div><dt>Kimlik</dt><dd>{selected.tckn}</dd></div><div><dt>Birincil hat</dt><dd className="mono">{selected.msisdn}</dd></div><div><dt>Tarife</dt><dd>{selected.plan ?? "Atlas 40 GB"}</dd></div><div><dt>Açık bakiye</dt><dd>{selected.balance}</dd></div></dl>
          <div className="inspector-signal"><i /><span><b>{selected.quotaLabel ?? "Kota %82"}</b><small>Canlı kota detayı Usage ekranında</small></span></div>
          <Button variant="primary" onClick={() => onNavigate("customer360")}>Müşteri 360'ı aç <ArrowRight size={16} /></Button>
        </aside>
      </div>
    </>
  );
}

export function Customer360Screen({ onNavigate }: ScreenProps) {
  const liveProfile = useLiveResource(fallbackCustomer360View, loadCustomer360View);
  const profile = liveProfile.data;

  return (
    <>
      <ScreenHeader id="customer360" action={<div className="action-row"><LiveAction>Hassas veriyi göster</LiveAction><Button variant="primary" onClick={() => onNavigate("order-new")}><Plus size={16} /> Yeni hat</Button></div>} />
      {liveProfile.error && <Notice kind="warning">Canlı 360 verisi alınamadı; son bilinen müşteri görünümü gösteriliyor.</Notice>}
      <section className="customer-signal-header"><div className="signal-customer"><span className="initials">{profile.initials}</span><div><span className="eyebrow">{profile.customerNumber} · {profile.segment}</span><h2>{profile.fullName}</h2><p>TCKN {profile.nationalId} · {profile.city}</p></div></div><div className="signal-fact"><span>Birincil MSISDN</span><b className="mono">{profile.msisdn}</b><Status label={profile.status} /></div><div className="signal-fact"><span>KYC durumu</span><b>{profile.kycDate}</b><Status label="Onaylandı" /></div><div className="signal-fact"><span>Toplam bakiye</span><b className="mono">{profile.balance}</b><Status label={profile.invoiceStatus} /></div></section>
      <div className="tabs">{["Özet", "Abonelikler", "Kullanım", "Faturalar", "Ödemeler", "Talepler", "Belgeler", "Audit"].map((tab, index) => <button className={index === 0 ? "active" : ""} key={tab}>{tab}</button>)}</div>
      <div className="customer-grid"><Panel eyebrow="Anlık durum" title="Abonelik sinyali" className="quota-summary"><div className="compact-orbits"><div className="orbit orbit--coral"><span><b>{profile.usage.dataPercent}</b><small>Data</small></span></div><div className="orbit orbit--violet"><span><b>{profile.usage.voicePercent}</b><small>Ses</small></span></div><div className="orbit orbit--lime"><span><b>{profile.usage.smsPercent}</b><small>SMS</small></span></div></div><dl className="detail-list"><div><dt>Tarife</dt><dd>{profile.plan}</dd></div><div><dt>Dönem</dt><dd>{profile.period}</dd></div><div><dt>Aşım tahmini</dt><dd>{profile.overage}</dd></div></dl><EmptyLink onClick={() => onNavigate("usage")}>Kullanım detayına git</EmptyLink></Panel><Panel eyebrow="Unified lifecycle" title="Müşteri zaman çizgisi" className="timeline-panel"><div className="pulse-spine">{profile.timeline.map((item) => <div className={`spine-item spine-item--${item.tone}`} key={`${item.title}-${item.t}`}><time>{item.t}</time><i /><span><b>{item.title}</b><small className="mono">{item.meta}</small></span></div>)}</div></Panel><Panel eyebrow="Risk ve aksiyon" title="Operasyon özeti"><div className="risk-row"><Status label={`${profile.usage.dataPercent} data kullanımı`} tone="warn" /><span>{profile.usage.dataRemaining}</span></div><div className="risk-row"><Status label={`${profile.ticketCount} açık talep`} tone="live" /><span>Ticket service</span></div><div className="risk-row"><Status label={profile.invoiceStatus} tone="ok" /><span>{profile.balance}</span></div><Button onClick={() => onNavigate("tickets")}>Talep çalışma alanını aç</Button></Panel></div>
    </>
  );
}

export function KycScreen({ onNavigate }: ScreenProps) {
  return (
    <>
      <ScreenHeader id="kyc" action={<div className="action-row"><LiveAction variant="danger"><XCircle size={16} /> Reddet</LiveAction><LiveAction><BadgeCheck size={16} /> Onayla</LiveAction></div>} />
      {!integration.live && <Notice>Onay ve red aksiyonları tasarlandı; customer-service KYC endpoint'i hazır olana kadar devre dışıdır.</Notice>}
      <div className="kyc-grid"><Panel eyebrow="Başvuru verisi" title="Kimlik ve iletişim"><div className="form-grid"><label>Ad<input value="Derya" readOnly /></label><label>Soyad<input value="Yılmaz" readOnly /></label><label>TCKN<input value="321••••••18" readOnly /></label><label>Doğum tarihi<input value="12.08.1992" readOnly /></label><label className="span-2">Adres<textarea value="Kozyatağı Mah. Defne Sk. No: 8/12 Kadıköy, İstanbul" readOnly /></label><label>E-posta<input value="derya.yilmaz@example.com" readOnly /></label><label>Telefon<input value="+90 532 000 00 47" readOnly /></label></div></Panel><Panel eyebrow="Belge inceleme" title="T.C. kimlik kartı" className="document-review"><div className="document-mock"><div className="document-photo"><UserRoundCheck /></div><div><b>Türkiye Cumhuriyeti</b><span>Derya Yılmaz</span><span>321••••••18</span><span>12.08.1992</span></div><FileCheck2 /></div><div className="match-list"><div><Check /> Ad / soyad <b>%100</b></div><div><Check /> TCKN <b>%100</b></div><div><Check /> Doğum tarihi <b>%100</b></div><div className="warning"><TriangleAlert /> Belge parlama skoru <b>%74</b></div></div><Button><UploadCloud size={16} /> Belgeyi yeniden yükle</Button></Panel><Panel eyebrow="Decision history" title="KYC karar geçmişi" className="kyc-history"><div className="pulse-spine"><div className="spine-item spine-item--live"><time>14:32</time><i /><span><b>Manuel inceleme</b><small>Tamer Akdeniz · Platform Admin</small></span></div><div className="spine-item spine-item--ok"><time>14:26</time><i /><span><b>OCR tamamlandı</b><small>4/4 alan eşleşti</small></span></div><div className="spine-item spine-item--warn"><time>14:25</time><i /><span><b>Kalite kontrolü</b><small>Parlama skoru eşik altında</small></span></div><div className="spine-item spine-item--ok"><time>14:24</time><i /><span><b>Belge yüklendi</b><small className="mono">doc-7729...c21</small></span></div></div><Button onClick={() => onNavigate("customer360")}>Müşteri 360'a dön</Button></Panel></div>
    </>
  );
}

export function CatalogScreen({ onNavigate }: ScreenProps) {
  const livePlans = useLiveResource(fallbackPlans, loadCatalogPlans);
  const plans = livePlans.data;

  return <><ScreenHeader id="catalog" action={<LiveAction><Plus size={16} /> Yeni ürün</LiveAction>} />{(!integration.live || livePlans.error) && <Notice kind={livePlans.error ? "warning" : "integration"}>{livePlans.error ? "Canlı katalog verisi alınamadı; demo katalog snapshot'ı gösteriliyor." : "Katalog mutasyonları product-catalog-service REST katmanı tamamlandığında etkinleşecektir. Karşılaştırma ve önizleme demo veriyle kullanılabilir."}</Notice>}<div className="catalog-layout"><Panel eyebrow="Katalog ağacı" title="Ürün aileleri" className="catalog-tree"><button className="active"><Signal /> Tarifeler <b>{plans.length}</b></button><button><PackageCheck /> Ek paketler <b>3</b></button><button><Zap /> VAS <b>1</b></button><button><CircleDashed /> Arşiv <b>0</b></button></Panel><Panel eyebrow="Karşılaştırma tuvali" title="Postpaid tarifeler" className="catalog-canvas" action={<Button><Filter size={16} /> Aktif ürünler</Button>}><div className="plan-grid">{plans.map((plan, index) => <article className={index === 1 ? "plan-card plan-card--selected" : "plan-card"} key={plan.code}><header><span className="eyebrow mono">{plan.code}</span><Status label={plan.status} /></header><h3>{plan.name}</h3><strong>{plan.price}<small>/ay</small></strong><dl><div><dt>Data</dt><dd>{plan.data}</dd></div><div><dt>Ses</dt><dd>{plan.voice}</dd></div><div><dt>SMS</dt><dd>{plan.sms}</dd></div></dl><span className="effective">{plan.effective}</span><Button onClick={() => onNavigate("order-new")}>Siparişte önizle</Button></article>)}</div></Panel><Panel eyebrow="Sürüm haritası" title="Atlas 40 GB v3.2" className="version-panel"><div className="version-line"><i className="current" /><span><b>v3.2</b><small>899,90 TL · 01 Tem 2026</small></span><Status label="Planlandı" /></div><div className="version-line"><i /><span><b>v3.1</b><small>849,90 TL · 01 Mar 2026</small></span><Status label="Aktif" /></div><div className="version-line"><i /><span><b>v2.4</b><small>749,90 TL · 01 Kas 2025</small></span><Status label="Arşiv" /></div><label className="toggle-row"><input type="checkbox" defaultChecked /> Mevcut aboneleri koru</label></Panel></div></>;
}

export function OrderNewScreen({ onNavigate }: ScreenProps) {
  return <><ScreenHeader id="order-new" action={<span className="mono muted">Taslak ORD-DRAFT-8821</span>} /><div className="wizard-steps">{["Müşteri", "Tarife", "Ek paketler", "Ödeme", "Onay"].map((step, index) => <div className={index < 2 ? "done" : index === 2 ? "current" : ""} key={step}><i>{index < 2 ? <Check size={14} /> : index + 1}</i><span>{step}</span></div>)}</div><div className="order-layout"><Panel eyebrow="Adım 3 / 5" title="Ek paketleri seçin" className="order-main"><div className="selected-customer"><div className="initials">DY</div><span><b>Derya Yılmaz</b><small className="mono">CUS-10482 · +90 532 ••• •• 47</small></span><Status label="KYC onaylandı" /></div><h3>Atlas 40 GB ile uyumlu paketler</h3><div className="addon-list">{[{ name: "Gece 20 GB", price: "129,90 TL", note: "00:00-06:00 arasında" }, { name: "Yurt Dışı 5 GB", price: "249,90 TL", note: "30 ülkede geçerli" }, { name: "Müzik Premium", price: "69,90 TL", note: "Data kotasından düşmez" }].map((addon, index) => <label key={addon.name}><input type="checkbox" defaultChecked={index === 0} /><span><b>{addon.name}</b><small>{addon.note}</small></span><strong>{addon.price}<small>/ay</small></strong></label>)}</div><div className="wizard-actions"><Button>Geri</Button><Button variant="primary">Ödemeye devam et <ArrowRight size={16} /></Button></div></Panel><aside className="order-summary"><span className="eyebrow">Sipariş özeti</span><h2>Yeni postpaid hat</h2><dl><div><dt>Atlas 40 GB</dt><dd>899,90 TL</dd></div><div><dt>Gece 20 GB</dt><dd>129,90 TL</dd></div><div><dt>SIM aktivasyon</dt><dd>120,00 TL</dd></div></dl><div className="order-total"><span>Aylık toplam</span><b>1.029,80 TL</b><small>Vergiler dahil</small></div><div className="order-total once"><span>Bir kerelik</span><b>120,00 TL</b></div><Notice kind="warning">Onaydan sonra OrderCreated eventi ile saga başlayacak.</Notice><Button onClick={() => onNavigate("order-saga")}>Saga önizlemesini aç</Button></aside></div></>;
}

export function OrderSagaScreen({ onNavigate }: ScreenProps) {
  const steps = [{ name: "Sipariş oluşturuldu", owner: "order-service", time: "14:31:52", tone: "ok" }, { name: "Ödeme", owner: "payment-service", time: "14:31:54", tone: "ok" }, { name: "MSISDN ayırma", owner: "subscription-service", time: "14:31:58", tone: "live" }, { name: "Abonelik aktivasyonu", owner: "subscription-service", time: "Bekliyor", tone: "muted" }, { name: "Bildirim", owner: "notification-service", time: "Bekliyor", tone: "muted" }];
  return <><ScreenHeader id="order-saga" action={<LiveAction variant="danger">Siparişi iptal et</LiveAction>} /><div className="saga-summary"><span><small>Durum</small><Status label="İşleniyor" tone="live" /></span><span><small>Geçen süre</small><b className="mono">00:04:18</b></span><span><small>Korelasyon</small><b className="mono">9f3c1b7a...e21d</b></span><span><small>Müşteri</small><b>Derya Yılmaz</b></span></div><Panel eyebrow="Lifecycle map" title="ORD-26-10482" className="saga-panel"><div className="saga-rail">{steps.map((step, index) => <button className={`saga-step saga-step--${step.tone}`} key={step.name}><i>{step.tone === "ok" ? <Check /> : index + 1}</i><span><b>{step.name}</b><small>{step.owner}</small><time>{step.time}</time></span></button>)}</div></Panel><div className="saga-grid"><Panel eyebrow="Event log" title="Servis hareketleri"><DataTable><thead><tr><th>Zaman</th><th>Event</th><th>Servis</th><th>Durum</th><th>Korelasyon</th></tr></thead><tbody>{[{ t: "14:31:58.920", e: "MSISDNAllocationRequested", s: "subscription-service", d: "İşleniyor" }, { t: "14:31:56.114", e: "PaymentReceived", s: "payment-service", d: "Başarılı" }, { t: "14:31:54.051", e: "PaymentRequested", s: "order-service", d: "Başarılı" }, { t: "14:31:52.005", e: "OrderCreated", s: "order-service", d: "Başarılı" }].map((row) => <tr key={row.t}><td className="mono">{row.t}</td><td><b>{row.e}</b></td><td>{row.s}</td><td><Status label={row.d} /></td><td className="mono">9f3c...e21d</td></tr>)}</tbody></DataTable></Panel><aside className="event-payload"><span className="eyebrow">Event inspector</span><h3>MSISDNAllocationRequested</h3><dl className="detail-list"><div><dt>Event ID</dt><dd className="mono">evt-7721...ca91</dd></div><div><dt>Attempt</dt><dd>1 / 3</dd></div><div><dt>Elapsed</dt><dd className="mono">00:00:22</dd></div></dl><pre>{`{\n  "orderId": "ORD-26-10482",\n  "customerId": "CUS-10482",\n  "planCode": "ATL-40-P",\n  "region": "TR-34"\n}`}</pre><Button onClick={() => onNavigate("subscriptions")}>Abonelik bağlamını aç</Button></aside></div></>;
}

export function SubscriptionsScreen({ onNavigate }: ScreenProps) {
  return <><ScreenHeader id="subscriptions" action={<LiveAction>Durum değiştir</LiveAction>} /><div className="query-bar"><div className="search-box"><Search /><input placeholder="Müşteri, MSISDN, ICCID, IMSI veya tarife ara" /></div><Button><Filter size={16} /> Durum: Tümü</Button></div><div className="subscription-grid"><Panel eyebrow="Abonelik detayı" title="+90 532 ••• •• 47" className="subscription-detail"><div className="subscription-hero"><Status label="Aktif" /><span className="eyebrow mono">SUB-2026-88214</span><h3>Atlas 40 GB</h3><p>Derya Yılmaz · CUS-10482</p></div><dl className="detail-grid"><div><dt>MSISDN</dt><dd className="mono">+90 532 ••• •• 47</dd></div><div><dt>ICCID</dt><dd className="mono">8990 0100 •••• 4821</dd></div><div><dt>IMSI</dt><dd className="mono">28601 ••••• 4812</dd></div><div><dt>Aktivasyon</dt><dd>19 Haz 2026, 14:34</dd></div><div><dt>Tarife snapshot</dt><dd>ATL-40-P v3.1</dd></div><div><dt>Fatura dönemi</dt><dd>Ayın 1'i</dd></div></dl><div className="related-links"><button onClick={() => onNavigate("order-saga")}>ORD-26-10482</button><button onClick={() => onNavigate("usage")}>Kullanım</button><button onClick={() => onNavigate("invoice")}>Fatura</button></div></Panel><Panel eyebrow="State machine" title="Abonelik yaşam döngüsü" className="state-machine"><div className="state-map"><div className="state-node done">Oluşturuldu<small>14:31</small></div><ArrowRight /><div className="state-node active">Aktif<small>14:34</small></div><ArrowRight /><div className="state-node">Askıda</div><ArrowRight /><div className="state-node">Yeniden aktif</div><ArrowRight /><div className="state-node">Sonlandırıldı</div></div><Notice>Askıya alma ve sonlandırma işlemleri gerekçe, etki tarihi ve yetki doğrulaması gerektirir.</Notice><div className="action-row"><LiveAction variant="secondary"><PauseCircle size={16} /> Askıya al</LiveAction><LiveAction variant="danger">Sonlandır</LiveAction></div></Panel><Panel eyebrow="Audit" title="Son durum hareketleri"><div className="event-stream"><button><i className="dot dot--ok" /><span><b>SubscriptionActivated</b><small>subscription-service</small></span><time>14:34</time></button><button><i className="dot dot--live" /><span><b>MSISDNAllocated</b><small className="mono">+90 532 ••• •• 47</small></span><time>14:33</time></button><button><i className="dot dot--ok" /><span><b>SimCardReserved</b><small className="mono">899001...4821</small></span><time>14:32</time></button></div></Panel></div></>;
}

export function UsageScreen() {
  const liveUsage = useLiveResource(fallbackUsageView, loadUsageView);
  const usage = liveUsage.data;
  const records = usage.records;

  return <><ScreenHeader id="usage" action={<Button><RefreshCw size={16} /> Veriyi yenile</Button>} /><Notice kind={liveUsage.error ? "warning" : "integration"}>{liveUsage.error ? "Canlı kota verisi alınamadı; son bilinen kullanım snapshot'ı gösteriliyor." : "Kullanım verisi usage-service quota endpoint'inden okunur. CDR liste endpoint'i eklendiğinde akış da canlıya alınacak."}</Notice><div className="usage-grid"><Panel eyebrow={usage.period} title="Kota orbitleri" className="quota-panel"><div className="large-orbits"><div className="orbit-large data"><span><b>{usage.dataUsed}</b><small>/ {usage.dataTotal}</small></span></div><div className="orbit-large voice"><span><b>{usage.voiceUsed}</b><small>/ {usage.voiceTotal}</small></span></div><div className="orbit-large sms"><span><b>{usage.smsUsed}</b><small>/ {usage.smsTotal}</small></span></div></div><div className="quota-legend"><div><i className="coral" />Data <b>{usage.dataPercent}</b><span>{usage.dataRemaining}</span></div><div><i className="violet" />Ses <b>{usage.voicePercent}</b><span>{usage.voiceRemaining}</span></div><div><i className="lime" />SMS <b>{usage.smsPercent}</b><span>{usage.smsRemaining}</span></div></div></Panel><Panel eyebrow="Trend" title="Günlük data kullanımı" className="usage-chart"><div className="chart-summary"><b>2,84 GB</b><span>Bugün · günlük ortalama 1,62 GB</span></div><svg viewBox="0 0 640 220" role="img" aria-label="Haziran ayı günlük data kullanımı çizgi grafiği"><defs><linearGradient id="usageFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor="#ff5a43" stopOpacity=".28" /><stop offset="1" stopColor="#ff5a43" stopOpacity="0" /></linearGradient></defs><path className="gridline" d="M30 40 H620 M30 90 H620 M30 140 H620 M30 190 H620" /><path className="area" d="M30 170 C70 145 85 150 120 122 S180 154 220 120 S285 80 330 112 S410 72 450 92 S510 60 550 74 S590 45 620 42 L620 200 L30 200 Z" /><path className="line" d="M30 170 C70 145 85 150 120 122 S180 154 220 120 S285 80 330 112 S410 72 450 92 S510 60 550 74 S590 45 620 42" /><line className="threshold" x1="520" y1="20" x2="520" y2="200" /><text x="528" y="34">%80 eşiği</text></svg><p>Özet: Data kullanımı 17 Haziran'dan itibaren günlük ortalamanın %42 üzerine çıktı.</p></Panel><Panel eyebrow="Aşım projeksiyonu" title="Dönem sonu tahmini"><Metric label="Tahmini kullanım" value="44,2 GB" meta="4,2 GB aşım" tone="warn" /><Metric label="Billing'e aktarılacak" value="84,20 TL" meta="Tahmini, vergi hariç" /><Status label={`${usage.dataPercent} eşiği gönderildi`} tone="ok" /></Panel><Panel eyebrow="CDR akışı" title="Son kullanım kayıtları" className="cdr-panel"><DataTable><thead><tr><th>Zaman</th><th>Tip</th><th>Miktar</th><th>Bölge</th><th>Ücret</th></tr></thead><tbody>{records.map((row) => <tr key={row.time}><td className="mono">{row.time}</td><td>{row.type}</td><td><b>{row.amount}</b></td><td>{row.zone}</td><td className="mono">{row.cost}</td></tr>)}</tbody></DataTable></Panel></div></>;
}

export function BillingScreen({ onNavigate }: ScreenProps) {
  const liveInvoices = useLiveResource(fallbackInvoices, loadInvoices);
  const invoiceRows = liveInvoices.data;

  return <><ScreenHeader id="billing" action={<LiveAction><Play size={16} /> Bill-run başlat</LiveAction>} />{(!integration.live || liveInvoices.error) && <Notice kind={liveInvoices.error ? "warning" : "integration"}>{liveInvoices.error ? "Canlı fatura ledger'ı alınamadı; son bilinen fatura snapshot'ı gösteriliyor." : "Bill-run komutu billing-service job endpoint'i uygulanana kadar devre dışıdır. İlerleme ve fatura ekranları demo veriyle tasarlanmıştır."}</Notice>}<section className="billing-band"><div><span className="eyebrow">Haziran 2026 bill-run</span><h2>{invoiceRows.length} fatura hazır</h2><div className="progress"><i style={{ width: "72%" }} /></div><p><Status label="İşleniyor" tone="live" /> MVP seed faturaları billing-service DB'den okunuyor</p></div><div className="billing-stage"><Check /> Kapsam hazırlandı <small>Demo müşteriler</small></div><div className="billing-stage"><Check /> Kullanım toplandı <small>Usage projection</small></div><div className="billing-stage active"><RefreshCw /> Faturalar kesiliyor <small>{invoiceRows.length} kayıt</small></div><div className="billing-stage"><CircleDashed /> PDF & bildirim <small>Bekliyor</small></div></section><div className="metric-grid billing-metrics"><Metric label="Toplam gelir" value="196,2 Mn TL" meta="+%4,8 geçen aya göre" tone="ok" /><Metric label="Vergi" value="38,4 Mn TL" meta="Hesaplanan toplam" /><Metric label="Gecikmiş bakiye" value="8,7 Mn TL" meta="3.142 fatura" tone="bad" /><Metric label="Tahsilat oranı" value="%94,2" meta="Hedef %95" tone="warn" /></div><Panel eyebrow={liveInvoices.live ? "Billing DB" : "Invoice ledger"} title="Faturalar" className="table-panel" action={<div className="action-row"><Button><Filter size={16} /> İstisnalar</Button><Button><ArrowDownToLine size={16} /> Mutabakat</Button></div>}><DataTable><thead><tr><th>Fatura</th><th>Müşteri</th><th>Dönem</th><th>Tarih</th><th>Tutar</th><th>Durum</th><th /></tr></thead><tbody>{invoiceRows.map((invoice) => <tr key={invoice.id} onClick={() => onNavigate("invoice")} onKeyDown={(event) => activateRow(event, () => onNavigate("invoice"))} tabIndex={0}><td><b className="mono">{invoice.id}</b></td><td>{invoice.customer}</td><td>{invoice.period}</td><td>{invoice.date}</td><td><b className="mono">{invoice.total}</b></td><td><Status label={invoice.status} /></td><td><ChevronRight /></td></tr>)}</tbody></DataTable></Panel></>;
}

export function InvoiceScreen({ onNavigate }: ScreenProps) {
  return <><ScreenHeader id="invoice" action={<div className="action-row"><Button><Mail size={16} /> Yeniden gönder</Button><Button variant="primary"><ArrowDownToLine size={16} /> PDF indir</Button></div>} /><div className="invoice-grid"><Panel eyebrow="Immutable invoice" title="FTR-2026-061842" className="invoice-data"><div className="invoice-heading"><div><span>Müşteri</span><b>Derya Yılmaz</b><small>CUS-10482 · +90 532 ••• •• 47</small></div><Status label="Ödendi" tone="ok" /></div><dl className="detail-grid"><div><dt>Fatura tarihi</dt><dd>19 Haz 2026</dd></div><div><dt>Son ödeme</dt><dd>29 Haz 2026</dd></div><div><dt>Dönem</dt><dd>01-30 Haz 2026</dd></div><div><dt>Vergi no</dt><dd>TelcoX 8130••••</dd></div></dl><DataTable><thead><tr><th>Kalem</th><th>Miktar</th><th>Tutar</th></tr></thead><tbody><tr><td>Atlas 40 GB aylık ücret</td><td>1 ay</td><td>899,90 TL</td></tr><tr><td>Gece 20 GB ek paket</td><td>1 ay</td><td>129,90 TL</td></tr><tr><td>Data aşımı</td><td>4,2 GB</td><td>84,20 TL</td></tr><tr><td>ÖİV + KDV</td><td>-</td><td>135,90 TL</td></tr></tbody><tfoot><tr><td colSpan={2}>Genel toplam</td><td>1.249,90 TL</td></tr></tfoot></DataTable><div className="payment-history"><Status label="Ödeme alındı" tone="ok" /><span>19 Haz 2026, 12:18 · Visa •••• 1842</span><b className="mono">PAY-88214</b></div><Button onClick={() => onNavigate("payments")}>Ödeme detayını aç</Button></Panel><Panel eyebrow="PDF preview" title="Fatura belgesi" className="pdf-preview"><div className="pdf-page"><div className="pdf-logo"><img src="/branding/telcox-signal-atlas-mark.png" alt="" /><b>TELCOX</b></div><span>FATURA</span><h3>FTR-2026-061842</h3><div className="pdf-rule" /><b>Derya Yılmaz</b><p>Kozyatağı Mah. Defne Sk.<br />Kadıköy / İstanbul</p><div className="pdf-lines"><i /><i /><i /><i /></div><strong>1.249,90 TL</strong><small>Bu belgenin görsel önizlemesidir.</small></div></Panel></div></>;
}

export function PaymentsScreen() {
  return <><ScreenHeader id="payments" action={<LiveAction><CreditCard size={16} /> Yeni ödeme</LiveAction>} />{!integration.live && <Notice>PSP ve payment-service bağlantıları hazır olana kadar ödeme, iade ve retry aksiyonları devre dışıdır.</Notice>}<div className="payments-grid"><Panel eyebrow="Ödeme detayı" title="PAY-88214" className="payment-card"><div className="payment-amount"><span>Tutar</span><b>1.249,90 TL</b><Status label="Başarılı" tone="ok" /></div><dl className="detail-grid"><div><dt>Fatura</dt><dd className="mono">FTR-2026-061842</dd></div><div><dt>Yöntem</dt><dd>Visa •••• 1842</dd></div><div><dt>Dış referans</dt><dd className="mono">psp_8839101</dd></div><div><dt>Idempotency key</dt><dd className="mono">idem-9f3c...e21d</dd></div></dl><div className="secure-note"><LockKeyhole /> Kart verisi TelcoX sistemlerinde saklanmaz.</div><LiveAction variant="danger">İade başlat</LiveAction></Panel><Panel eyebrow="Attempt timeline" title="Ödeme denemeleri ve retry" className="retry-panel"><div className="pulse-spine"><div className="spine-item spine-item--bad"><time>17 Haz</time><i /><span><b>1. deneme başarısız</b><small>PSP_DECLINED · Yetersiz bakiye</small></span></div><div className="spine-item spine-item--bad"><time>18 Haz</time><i /><span><b>24 saat retry başarısız</b><small>Attempt 2 / 4</small></span></div><div className="spine-item spine-item--ok"><time>19 Haz</time><i /><span><b>72 saat penceresi: Başarılı</b><small>12:18:04 · 1.249,90 TL</small></span></div><div className="spine-item spine-item--muted"><time>24 Haz</time><i /><span><b>168 saat retry</b><small>Ödeme başarılı olduğu için iptal</small></span></div></div></Panel><Panel eyebrow="Planlanan retry" title="Bekleyen ödemeler"><div className="retry-countdown"><span><b>PAY-88104</b><small>Selin Arslan · 3.840,15 TL</small></span><div><small>Sonraki deneme</small><b className="mono">04:18:22</b></div><Status label="72 saat" tone="warn" /></div><div className="retry-countdown"><span><b>PAY-88092</b><small>Can Korkmaz · 786,40 TL</small></span><div><small>Sonraki deneme</small><b className="mono">22:42:08</b></div><Status label="24 saat" tone="live" /></div></Panel></div></>;
}

export function NotificationsScreen() {
  return <><ScreenHeader id="notifications" action={<LiveAction><Plus size={16} /> Yeni şablon</LiveAction>} /><div className="tabs"><button className="active">Şablonlar</button><button>Gönderim geçmişi</button><button>Tercihler</button></div><div className="notification-layout"><Panel eyebrow="Template editor" title="Fatura kesildi / SMS" className="template-editor"><label>Şablon adı<input value="invoice-issued-sms-v3" readOnly /></label><label>Mesaj<textarea value="Merhaba {{customer.firstName}}, {{invoice.id}} numaralı {{invoice.total}} tutarındaki faturanız oluşturuldu. Son ödeme: {{invoice.dueDate}}." readOnly /></label><div className="variables"><span>{"{{customer.firstName}}"}</span><span>{"{{invoice.id}}"}</span><span>{"{{invoice.total}}"}</span><span>{"{{invoice.dueDate}}"}</span></div><div className="action-row"><LiveAction variant="secondary">Taslağı kaydet</LiveAction><LiveAction>Yayınla</LiveAction></div></Panel><Panel eyebrow="Live preview" title="SMS önizleme" className="phone-preview"><div className="phone"><div className="phone-bar" /><span className="sms-sender">TELCOX</span><div className="sms-bubble">Merhaba Derya, FTR-2026-061842 numaralı 1.249,90 TL tutarındaki faturanız oluşturuldu. Son ödeme: 29 Haz 2026.</div><small>14:32 · 132 karakter</small></div></Panel><Panel eyebrow="Delivery stream" title="Son gönderimler" className="delivery-stream"><div className="event-stream"><button><i className="dot dot--ok" /><span><b>Fatura kesildi</b><small>Derya Yılmaz · SMS</small></span><Status label="Teslim edildi" tone="ok" /></button><button><i className="dot dot--bad" /><span><b>Ödeme hatırlatma</b><small>Selin Arslan · E-posta</small></span><Status label="Başarısız" tone="bad" /></button><button><i className="dot dot--live" /><span><b>Kota %80</b><small>Can Korkmaz · SMS</small></span><Status label="İşleniyor" tone="live" /></button></div></Panel><Panel eyebrow="Consent" title="İletişim tercihleri"><div className="consent-row"><span><Smartphone /> SMS</span><Status label="İzinli" tone="ok" /></div><div className="consent-row"><span><Mail /> E-posta</span><Status label="İzinli" tone="ok" /></div><div className="consent-row"><span><BellRing /> Push</span><Status label="İzin yok" tone="muted" /></div><Notice>İzin durumu sessizce geçersiz kılınamaz; her değişiklik audit edilir.</Notice></Panel></div></>;
}

export function TicketsScreen({ onNavigate }: ScreenProps) {
  const liveTicket = useLiveResource(fallbackTicketView, loadTicketView);
  const view = liveTicket.data;
  const queues = view.queues;

  return <><ScreenHeader id="tickets" action={<LiveAction><Plus size={16} /> Yeni talep</LiveAction>} />{liveTicket.error && <Notice kind="warning">Canlı ticket verisi alınamadı; son bilinen destek workspace'i gösteriliyor.</Notice>}<div className="ticket-workspace"><aside className="ticket-queues"><span className="eyebrow">Kuyruklar</span>{queues.map((queue) => <button className={queue.label === "SLA riski" ? "active" : ""} key={queue.label}><span>{queue.label}</span><b>{queue.count}</b></button>)}</aside><section className="ticket-conversation"><header><div><span className="eyebrow mono">{view.ticketNumber} · {view.category}</span><h2>{view.subject}</h2><p>{view.customerName} · Son yanıt 8 dk önce</p></div><div className="sla-clock"><span>SLA kalan</span><b>{view.sla}</b><Status label={view.priority} tone={view.priority === "Kritik" ? "bad" : "warn"} /></div></header><div className="messages"><article><div className="message-author">{view.customerName.split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toUpperCase()}</div><div><b>{view.customerName} <time>13:48</time></b><p>{view.description}</p></div></article><article className="agent"><div className="message-author">TA</div><div><b>Tamer Akdeniz <time>14:02</time></b><p>Hat sinyalinizi ve bölgesel durumu kontrol ediyorum. Son 24 saatte üç kısa kesinti görüyorum.</p><span className="internal-note">İç not · NOC kontrolü istendi</span></div></article></div><div className="reply-box"><textarea placeholder="Müşteriye yanıt yazın..." /><div><button>İç not</button><LiveAction><Send size={16} /> Gönder</LiveAction></div></div></section><aside className="ticket-context"><span className="eyebrow">Müşteri bağlamı</span><h3>{view.customerName}</h3><span className="mono">{view.customerNumber}</span><dl className="detail-list"><div><dt>MSISDN</dt><dd>{view.msisdn}</dd></div><div><dt>Tarife</dt><dd>{view.plan}</dd></div><div><dt>Kota</dt><dd>{view.quota}</dd></div><div><dt>Açık bakiye</dt><dd>{view.balance}</dd></div></dl><div className="context-signal"><Signal /><span><b>Sinyal normal</b><small>Son ölçüm -74 dBm</small></span></div><Button onClick={() => onNavigate("customer360")}>Müşteri 360'ı aç</Button><LiveAction variant="secondary">Talebi çöz</LiveAction></aside></div></>;
}

export function AdminScreen() {
  const audit = [{ time: "14:32:01", actor: "admin@telcox.local", action: "ROLE_UPDATE", entity: "User:1042", id: "evt-9f8a...4d5e" }, { time: "14:28:15", actor: "system_core", action: "QUOTA_ADJUSTMENT", entity: "Account:8891A", id: "evt-7x8y...1b2c" }, { time: "13:55:04", actor: "tamer@telcox.local", action: "MSISDN_REVEAL", entity: "Customer:10482", id: "evt-1a2b...5e6f" }, { time: "13:10:45", actor: "order-service", action: "SAGA_RETRY", entity: "Order:10482", id: "evt-0z9y...6v5u" }];
  return <><ScreenHeader id="admin" action={<LiveAction><Plus size={16} /> Yeni rol</LiveAction>} /><div className="admin-grid"><Panel eyebrow="Access control" title="Yetki matrisi" className="permission-panel"><DataTable><thead><tr><th>Rol / Yetki</th><th>Kullanıcı</th><th>Fatura & Kota</th><th>Sistem ayarı</th><th>Audit oku</th></tr></thead><tbody>{[["Platform Admin", 1, 1, 1, 1], ["Ops Engineer", 0, 1, 1, 1], ["Support Agent", 0, 1, 0, 0], ["Read-only Auditor", 0, 0, 0, 1]].map((row) => <tr key={String(row[0])}>{row.map((cell, index) => <td key={index}>{index === 0 ? <b>{cell}</b> : cell ? <Check className="check" /> : <XCircle className="cross" />}</td>)}</tr>)}</tbody></DataTable></Panel><Panel eyebrow="Immutable log" title="Denetim günlüğü" className="audit-panel" action={<Button><Filter size={16} /> Filtrele</Button>}><DataTable><thead><tr><th>Zaman</th><th>Aktör</th><th>İşlem</th><th>Varlık</th><th>Korelasyon</th></tr></thead><tbody>{audit.map((row) => <tr key={row.id}><td className="mono">{row.time}</td><td>{row.actor}</td><td><b>{row.action}</b></td><td>{row.entity}</td><td className="mono">{row.id}</td></tr>)}</tbody></DataTable></Panel><aside className="audit-inspector"><span className="eyebrow">Audit event payload</span><h3>ROLE_UPDATE</h3><dl className="detail-list"><div><dt>Korelasyon</dt><dd className="mono">evt-9f8a...4d5e</dd></div><div><dt>Aktör IP</dt><dd className="mono">192.168.1.104</dd></div><div><dt>Yetki seviyesi</dt><dd>L3 Platform Admin</dd></div></dl><h4>Mutation snapshot</h4><pre>{`- "billing.refund": false\n+ "billing.refund": true\n  "audit.read": true`}</pre><Notice kind="warning">Yüksek yetki değişikliği. İkinci onay zinciri gerekli.</Notice></aside></div></>;
}

export function OpsScreen() {
  const [tick, refresh] = useRefreshTick();
  const liveOps = useLiveResource(fallbackPlatformOpsView, loadPlatformOpsView, [tick]);
  const ops = liveOps.data;
  const gateway = ops.services.find((service) => service.name === "api-gateway");
  const topologyServices = ops.services.filter((service) => service.name !== "api-gateway").slice(0, 5);
  const availability = ops.pulse.totalServices > 0
    ? `%${Math.round((ops.pulse.healthyServices / ops.pulse.totalServices) * 100)}`
    : "-";

  return <><ScreenHeader id="ops" action={<Button onClick={refresh}><RefreshCw size={16} /> Yenile</Button>} />{liveOps.error && <Notice kind="warning">Canlı platform snapshot alınamadı; son bilinen ops görünümü gösteriliyor.</Notice>}<section className="ops-pulse"><Status label={`${ops.environment}: ${ops.statusLabel}`} tone={ops.statusTone} /><span>Gateway <b>{gateway?.latency ?? "-"}</b></span><span>Kafka Connect <b>{ops.pulse.connectorsRunning}/{ops.pulse.connectorsTotal}</b></span><span>Servis sağlığı <b>{availability}</b></span><span>Son kontrol <b>{ops.generatedAt}</b></span></section><div className="ops-grid"><Panel eyebrow="Service topology" title="TelcoX servis haritası" className="topology-panel" action={<span className="mono muted">{liveOps.live ? "Canlı BFF" : "Snapshot"}</span>}><div className="topology"><div className={`topology-service gateway ${gateway?.tone === "bad" ? "failed" : gateway?.tone === "warn" ? "warning" : ""}`}><Server /><b>{gateway?.name ?? "api-gateway"}</b><small>{gateway?.latency ?? "-"} · {gateway?.health ?? "-"}</small></div><div className="topology-bus"><Network /><span>Kafka event backbone</span></div>{topologyServices.map((service, index) => <div className={`topology-service service-${index} ${service.tone === "bad" ? "failed" : service.tone === "warn" ? "warning" : ""}`} key={service.name}><Database /><b>{service.name}</b><small>{service.latency} · {service.rate}</small></div>)}</div></Panel><Panel eyebrow="CDC health" title="Kafka Connect" className="connector-panel">{ops.connectors.map((connector) => <div className="connector" key={connector.name}><span><Database /><b>{connector.name}</b><small>{connector.table} · {connector.detail}</small></span><Status label={connector.status} tone={connector.tone} /></div>)}<LiveAction variant="secondary"><RefreshCw size={16} /> Connector restart</LiveAction></Panel><Panel eyebrow="Live event stream" title="Platform olayları" className="kafka-panel"><div className="event-stream">{ops.events.map((event) => <button key={`${event.type}-${event.service}-${event.time}`}><i className={`dot dot--${event.tone}`} /><span><b>{event.type}</b><small className="mono">{event.service} · {event.detail}</small></span><time>{event.time}</time></button>)}</div></Panel><Panel eyebrow="Incident" title={ops.incident.title} className="incident-panel"><div className="incident-number"><b>{ops.incident.value}</b><span>{ops.incident.detail}</span></div><svg viewBox="0 0 300 80"><path d="M0 64 L35 61 L60 66 L90 58 L120 60 L150 48 L175 52 L200 22 L230 28 L260 14 L300 19" /></svg><div className="action-row"><Button><Activity size={16} /> Trace'ler</Button><Button><Code2 size={16} /> Loglar</Button></div></Panel></div></>;
}

export function StatesScreen() {
  const [tick, refresh] = useRefreshTick();
  const liveStates = useLiveResource(fallbackSystemStatesView, loadSystemStatesView, [tick]);
  const view = liveStates.data;
  const iconFor = (tone: string) => tone === "bad" ? <CloudOff /> : tone === "warn" ? <TriangleAlert /> : tone === "live" ? <Radio /> : <Check />;

  return <><ScreenHeader id="states" action={<Button onClick={refresh}><RefreshCw size={16} /> Yenile</Button>} />{liveStates.error && <Notice kind="warning">Canlı sistem durumu alınamadı; son bilinen snapshot gösteriliyor.</Notice>}<div className="states-grid">{view.cards.map((card) => <Panel eyebrow={card.eyebrow} title={card.title} className={`state-example ${card.tone === "bad" ? "error" : card.tone === "warn" ? "warning" : ""}`} key={`${card.eyebrow}-${card.title}`}>{iconFor(card.tone)}<h3>{card.status}</h3><p>{card.description}</p><Status label={card.detail} tone={card.tone} /></Panel>)}<Panel eyebrow="Snapshot" title="Canlılık bilgisi" className="state-example"><Gauge /><h3>{liveStates.live ? "BFF live" : "Snapshot"}</h3><p>Bu ekran BFF platform ops endpoint'inden periyodik veri çeker.</p><Status label={`Son kontrol ${view.generatedAt}`} tone={liveStates.live ? "ok" : "warn"} /></Panel></div></>;
}

export function ScreenRenderer({ id, onNavigate }: { id: ViewId; onNavigate: (view: ViewId) => void }) {
  const props = { onNavigate };
  switch (id) {
    case "login": return <LoginScreen {...props} />;
    case "overview": return <OverviewScreen {...props} />;
    case "customers": return <CustomersScreen {...props} />;
    case "customer360": return <Customer360Screen {...props} />;
    case "kyc": return <KycScreen {...props} />;
    case "catalog": return <CatalogScreen {...props} />;
    case "order-new": return <OrderNewScreen {...props} />;
    case "order-saga": return <OrderSagaScreen {...props} />;
    case "subscriptions": return <SubscriptionsScreen {...props} />;
    case "usage": return <UsageScreen />;
    case "billing": return <BillingScreen {...props} />;
    case "invoice": return <InvoiceScreen {...props} />;
    case "payments": return <PaymentsScreen />;
    case "notifications": return <NotificationsScreen />;
    case "tickets": return <TicketsScreen {...props} />;
    case "admin": return <AdminScreen />;
    case "ops": return <OpsScreen />;
    case "states": return <StatesScreen />;
  }
}
