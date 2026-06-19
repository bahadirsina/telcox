import type { LucideIcon } from "lucide-react";
import {
  Activity,
  BellRing,
  Boxes,
  CircleDollarSign,
  ClipboardList,
  CreditCard,
  Gauge,
  LayoutDashboard,
  PackageSearch,
  RadioTower,
  ReceiptText,
  Settings2,
  ShieldCheck,
  ShoppingCart,
  Users,
} from "lucide-react";

export type ViewId =
  | "login"
  | "overview"
  | "customers"
  | "customer360"
  | "kyc"
  | "catalog"
  | "order-new"
  | "order-saga"
  | "subscriptions"
  | "usage"
  | "billing"
  | "invoice"
  | "payments"
  | "notifications"
  | "tickets"
  | "admin"
  | "ops"
  | "states";

export type NavItem = {
  id: ViewId;
  label: string;
  eyebrow: string;
  description: string;
  icon: LucideIcon;
};

export const views: NavItem[] = [
  { id: "overview", label: "Genel Bakış", eyebrow: "Operasyon kokpiti", description: "Abone yaşam döngüsünü istisnalar öncelikli izleyin.", icon: LayoutDashboard },
  { id: "customers", label: "Müşteriler", eyebrow: "Customer service", description: "Müşteri, KYC ve abonelik bağlamında arama yapın.", icon: Users },
  { id: "catalog", label: "Katalog", eyebrow: "Product catalog", description: "Tarife, ek paket ve VAS sürümlerini yönetin.", icon: PackageSearch },
  { id: "order-new", label: "Siparişler", eyebrow: "Order service", description: "Yeni hat siparişini kontrollü bir akışla başlatın.", icon: ShoppingCart },
  { id: "subscriptions", label: "Abonelikler", eyebrow: "Subscription service", description: "Hat, SIM ve tarife durumlarını yönetin.", icon: Boxes },
  { id: "usage", label: "Kullanım", eyebrow: "Usage service", description: "Kota, eşik ve CDR hareketlerini inceleyin.", icon: Gauge },
  { id: "billing", label: "Faturalama", eyebrow: "Billing service", description: "Bill-run süreçlerini ve tahsilat görünümünü izleyin.", icon: ReceiptText },
  { id: "payments", label: "Ödemeler", eyebrow: "Payment service", description: "Ödeme denemeleri, idempotency ve retry akışları.", icon: CreditCard },
  { id: "notifications", label: "Bildirimler", eyebrow: "Notification service", description: "Şablon, izin ve gönderim durumlarını izleyin.", icon: BellRing },
  { id: "tickets", label: "Talepler", eyebrow: "Ticket service", description: "SLA odaklı müşteri destek çalışma alanı.", icon: ClipboardList },
  { id: "admin", label: "Yönetim", eyebrow: "Identity service", description: "Roller, yetkiler ve değiştirilemez audit kayıtları.", icon: ShieldCheck },
  { id: "ops", label: "Platform Ops", eyebrow: "Network room", description: "Servis topolojisi, Kafka ve connector sağlığı.", icon: RadioTower },
  { id: "states", label: "Sistem Durumları", eyebrow: "Pattern sheet", description: "Erişilebilir yükleme, hata ve boş durum desenleri.", icon: Settings2 },
];

export const customers = [
  { id: "CUS-10482", name: "Derya Yılmaz", tckn: "32•••••••18", msisdn: "+90 532 ••• •• 47", city: "İstanbul", kyc: "Onaylandı", status: "Aktif", lines: 2, tickets: 1, balance: "1.249,90 TL" },
  { id: "CUS-10479", name: "Mert Aksoy", tckn: "45•••••••06", msisdn: "+90 535 ••• •• 12", city: "Ankara", kyc: "Bekliyor", status: "Bekliyor", lines: 0, tickets: 0, balance: "0,00 TL" },
  { id: "CUS-10461", name: "Selin Arslan", tckn: "27•••••••42", msisdn: "+90 542 ••• •• 83", city: "İzmir", kyc: "Onaylandı", status: "Riskli", lines: 1, tickets: 2, balance: "3.840,15 TL" },
  { id: "CUS-10398", name: "Can Korkmaz", tckn: "19•••••••61", msisdn: "+90 505 ••• •• 29", city: "Bursa", kyc: "Reddedildi", status: "Askıda", lines: 1, tickets: 1, balance: "786,40 TL" },
];

export const events = [
  { type: "CustomerKYCApproved", time: "14:32:08", id: "9f3c1b7a...e21d", tone: "ok" },
  { type: "OrderCreated", time: "14:31:52", id: "ord-10482...a901", tone: "live" },
  { type: "PaymentFailed", time: "14:29:16", id: "pay-88214...b717", tone: "bad" },
  { type: "QuotaThresholdReached", time: "14:27:44", id: "usage-744...c20", tone: "warn" },
  { type: "TicketAssigned", time: "14:24:03", id: "tic-3108...f09", tone: "info" },
];

export const invoices = [
  { id: "FTR-2026-061842", customer: "Derya Yılmaz", period: "Haziran 2026", total: "1.249,90 TL", status: "Ödendi", date: "19 Haz 2026" },
  { id: "FTR-2026-061839", customer: "Selin Arslan", period: "Haziran 2026", total: "3.840,15 TL", status: "Gecikmiş", date: "18 Haz 2026" },
  { id: "FTR-2026-061827", customer: "Can Korkmaz", period: "Haziran 2026", total: "786,40 TL", status: "Kısmi ödendi", date: "18 Haz 2026" },
  { id: "FTR-2026-061801", customer: "Ece Güngör", period: "Haziran 2026", total: "934,75 TL", status: "Kesildi", date: "17 Haz 2026" },
];

export const services = [
  { name: "api-gateway", health: "Sağlıklı", latency: "42 ms", rate: "1.204 req/s" },
  { name: "identity-service", health: "Sağlıklı", latency: "18 ms", rate: "86 req/s" },
  { name: "customer-service", health: "Sağlıklı", latency: "31 ms", rate: "342 req/s" },
  { name: "order-service", health: "Riskli", latency: "214 ms", rate: "119 req/s" },
  { name: "billing-service", health: "Hata", latency: "850 ms", rate: "45 req/s" },
  { name: "payment-service", health: "Sağlıklı", latency: "96 ms", rate: "132 req/s" },
];

export const quickViews = [
  { id: "customer360" as ViewId, label: "Müşteri 360", icon: Activity },
  { id: "order-saga" as ViewId, label: "Saga izleyici", icon: CircleDollarSign },
  { id: "invoice" as ViewId, label: "Fatura detayı", icon: ReceiptText },
  { id: "kyc" as ViewId, label: "KYC inceleme", icon: ShieldCheck },
];
