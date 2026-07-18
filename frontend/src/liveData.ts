import { useEffect, useState } from "react";
import { apiRequest, integration } from "./api";

export const demoIds = {
  customerId: "11111111-1111-4111-8111-000000010482",
  subscriptionId: "22222222-2222-4222-8222-000000010482",
  orderId: "33333333-3333-4333-8333-000000010482",
} as const;

export type CustomerRow = {
  uuid?: string;
  id: string;
  name: string;
  tckn: string;
  msisdn: string;
  city: string;
  kyc: string;
  status: string;
  lines: number;
  tickets: number;
  balance: string;
  plan?: string;
  quotaLabel?: string;
};

export type InvoiceRow = {
  id: string;
  customer: string;
  period: string;
  total: string;
  status: string;
  date: string;
};

export type PlanCard = {
  name: string;
  code: string;
  price: string;
  data: string;
  voice: string;
  sms: string;
  status: string;
  effective: string;
};

export type UsageRecordRow = {
  time: string;
  type: string;
  amount: string;
  zone: string;
  cost: string;
};

export type UsageView = {
  period: string;
  dataUsed: string;
  dataTotal: string;
  dataPercent: string;
  dataRemaining: string;
  voiceUsed: string;
  voiceTotal: string;
  voicePercent: string;
  voiceRemaining: string;
  smsUsed: string;
  smsTotal: string;
  smsPercent: string;
  smsRemaining: string;
  records: UsageRecordRow[];
};

export type Customer360View = {
  customerNumber: string;
  fullName: string;
  initials: string;
  segment: string;
  nationalId: string;
  city: string;
  msisdn: string;
  status: string;
  kycDate: string;
  balance: string;
  invoiceStatus: string;
  plan: string;
  period: string;
  overage: string;
  usage: UsageView;
  timeline: Array<{ t: string; title: string; meta: string; tone: string }>;
  ticketCount: number;
};

export type TicketView = {
  queues: Array<{ label: string; count: number }>;
  ticketNumber: string;
  category: string;
  subject: string;
  customerName: string;
  customerNumber: string;
  msisdn: string;
  plan: string;
  quota: string;
  balance: string;
  priority: string;
  sla: string;
  description: string;
};

export type DashboardView = {
  lifecycle: Array<{ label: string; value: string; meta: string; tone: string }>;
  metrics: Array<{ label: string; value: string; meta: string; tone?: "ok" | "live" | "warn" | "bad" }>;
};

export type PlatformOpsView = {
  generatedAt: string;
  environment: string;
  statusLabel: string;
  statusTone: "ok" | "warn" | "bad" | "live";
  pulse: {
    totalServices: number;
    healthyServices: number;
    degradedServices: number;
    downServices: number;
    totalRecords: number;
    prometheusTargetsUp: number;
    prometheusTargetsTotal: number;
    connectorsRunning: number;
    connectorsTotal: number;
  };
  services: Array<{
    name: string;
    label: string;
    health: string;
    tone: "ok" | "warn" | "bad" | "muted";
    latency: string;
    rate: string;
    detail: string;
  }>;
  connectors: Array<{
    name: string;
    table: string;
    status: string;
    tone: "ok" | "warn" | "bad";
    detail: string;
  }>;
  events: Array<{
    type: string;
    service: string;
    status: string;
    tone: "ok" | "warn" | "bad" | "live";
    detail: string;
    time: string;
  }>;
  incident: {
    title: string;
    value: string;
    detail: string;
    tone: "ok" | "warn" | "bad";
  };
};

export type SystemStatesView = {
  generatedAt: string;
  cards: Array<{
    eyebrow: string;
    title: string;
    status: string;
    tone: "ok" | "warn" | "bad" | "live" | "muted";
    description: string;
    detail: string;
  }>;
};

type LiveResource<T> = {
  data: T;
  loading: boolean;
  error: string | null;
  live: boolean;
};

type CustomerDto = {
  id: string;
  customerNumber: string;
  customerType: string;
  firstName?: string | null;
  lastName?: string | null;
  nationalId?: string | null;
  segment?: string | null;
  status: string;
  updatedAt?: string | null;
};

type ContactDto = {
  contactType: string;
  contactValue: string;
  isPrimary: boolean;
};

type AddressDto = {
  city?: string | null;
  isDefault: boolean;
};

type SubscriptionDto = {
  id: string;
  customerId: string;
  orderId?: string | null;
  msisdn?: string | null;
  simIccid?: string | null;
  planCode: string;
  status: string;
  activatedAt?: string | null;
};

type OrderDto = {
  id: string;
  orderNumber: string;
  planCode: string;
  status: string;
  createdAt?: string | null;
  completedAt?: string | null;
};

type InvoiceDto = {
  id: string;
  invoiceNumber: string;
  subscriptionId?: string | null;
  periodStart: string;
  periodEnd: string;
  totalAmount: number | string;
  currency?: string | null;
  status: string;
  issuedAt?: string | null;
  dueDate?: string | null;
};

type ProductDto = {
  id: string;
  code: string;
  name: string;
  productType: string;
  status: string;
};

type PlanDto = {
  monthlyPrice?: number | string | null;
  currency?: string | null;
  validFrom?: string | null;
  validTo?: string | null;
  features: PlanFeatureDto[];
};

type PlanFeatureDto = {
  featureType: string;
  allowance?: number | string | null;
  unit?: string | null;
  isUnlimited: boolean;
};

type QuotaDto = {
  quotaType?: string;
  type?: string;
  totalAllowance?: number | string;
  total?: number | string;
  usedAmount?: number | string;
  used?: number | string;
  remainingAmount?: number | string;
  remaining?: number | string;
  usagePercent: number | string;
  periodStart: string;
  periodEnd: string;
};

type TicketDto = {
  ticketNumber: string;
  customerId: string;
  category: string;
  priority: string;
  status: string;
  subject: string;
  description: string;
  assignedTeam?: string | null;
  assignedAgentId?: string | null;
  slaDueAt?: string | null;
  createdAt?: string | null;
};

type DashboardSummaryDto = {
  counters: Array<{ label: string; value: number; status: string }>;
};

type PlatformOpsDto = {
  generatedAt: string;
  environment: string;
  overallStatus: string;
  pulse: PlatformOpsView["pulse"];
  services: Array<{
    service: string;
    label: string;
    group: string;
    status: string;
    available: boolean;
    dataAvailable: boolean;
    latencyMs: number;
    records: number;
    detail: string;
  }>;
  connectors: Array<{
    name: string;
    status: string;
    available: boolean;
    runningTasks: number;
    totalTasks: number;
    detail: string;
  }>;
  prometheus: {
    available: boolean;
    targetsUp: number;
    targetsTotal: number;
    detail: string;
  };
  events: Array<{
    type: string;
    service: string;
    status: string;
    detail: string;
    occurredAt: string;
  }>;
};

const planNames: Record<string, string> = {
  "ATL-20-P": "Atlas 20 GB",
  "ATL-40-P": "Atlas 40 GB",
  "ATL-UNL-P": "Atlas Limitsiz",
};

const staticUsageRecords: UsageRecordRow[] = [
  { time: "14:28:42", type: "Data", amount: "284 MB", zone: "Istanbul", cost: "0,00 TL" },
  { time: "13:14:09", type: "Ses", amount: "18 dk", zone: "Turkcell", cost: "0,00 TL" },
  { time: "12:48:51", type: "SMS", amount: "3 adet", zone: "Yurt ici", cost: "0,00 TL" },
  { time: "11:06:17", type: "Data", amount: "1,2 GB", zone: "Istanbul", cost: "0,00 TL" },
];

export const fallbackUsageView: UsageView = {
  period: "01-30 Haz 2026",
  dataUsed: "32,8",
  dataTotal: "40 GB",
  dataPercent: "%82",
  dataRemaining: "7,2 GB kaldı",
  voiceUsed: "920",
  voiceTotal: "2.000 dk",
  voicePercent: "%46",
  voiceRemaining: "1.080 dk kaldı",
  smsUsed: "182",
  smsTotal: "1.000 SMS",
  smsPercent: "%18",
  smsRemaining: "818 adet kaldı",
  records: staticUsageRecords,
};

export const fallbackCustomer360View: Customer360View = {
  customerNumber: "CUS-10482",
  fullName: "Derya Yılmaz",
  initials: "DY",
  segment: "Bireysel",
  nationalId: "32•••••••18",
  city: "İstanbul",
  msisdn: "+90 532 ••• •• 47",
  status: "Aktif",
  kycDate: "19 Haz 2026",
  balance: "1.249,90 TL",
  invoiceStatus: "Ödendi",
  plan: "Atlas 40 GB",
  period: "01-30 Haz 2026",
  overage: "84,20 TL",
  usage: fallbackUsageView,
  timeline: [
    { t: "14:32", title: "KYC onaylandı", meta: "customer-service", tone: "ok" },
    { t: "14:31", title: "Sipariş oluşturuldu", meta: "ORD-26-10482 · Atlas 40 GB", tone: "live" },
    { t: "12:18", title: "Ödeme alındı", meta: "FTR-2026-061842 · 1.249,90 TL", tone: "ok" },
    { t: "Dün", title: "SLA riski oluştu", meta: "TIC-3108 · Bağlantı kalitesi", tone: "warn" },
  ],
  ticketCount: 1,
};

export const fallbackPlans: PlanCard[] = [
  { name: "Atlas 20 GB", code: "ATL-20-P", price: "649,90 TL", data: "20 GB", voice: "1.000 dk", sms: "1.000", status: "Aktif", effective: "01 Haz 2026 - Süresiz" },
  { name: "Atlas 40 GB", code: "ATL-40-P", price: "899,90 TL", data: "40 GB", voice: "2.000 dk", sms: "1.000", status: "Aktif", effective: "01 Haz 2026 - Süresiz" },
  { name: "Atlas Limitsiz", code: "ATL-UNL-P", price: "1.499,90 TL", data: "Limitsiz", voice: "Limitsiz", sms: "Limitsiz", status: "Planlandı", effective: "01 Haz 2026 - Süresiz" },
];

export const fallbackTicketView: TicketView = {
  queues: [
    { label: "Atanmamış", count: 128 },
    { label: "Bana atanmış", count: 24 },
    { label: "SLA riski", count: 7 },
    { label: "Müşteri bekleniyor", count: 42 },
    { label: "Çözüldü", count: 286 },
  ],
  ticketNumber: "TIC-3108",
  category: "Teknik destek",
  subject: "Ev internetinde bağlantı kopmaları",
  customerName: "Derya Yılmaz",
  customerNumber: "CUS-10482",
  msisdn: "+90 532 ••• •• 47",
  plan: "Atlas 40 GB",
  quota: "%82 data",
  balance: "1.249,90 TL",
  priority: "Kritik",
  sla: "00:24:18",
  description: "Bağlantı son iki gündür özellikle akşam saatlerinde sık sık kopuyor. Modemi yeniden başlattım ancak sorun devam ediyor.",
};

export const fallbackDashboardView: DashboardView = {
  lifecycle: [
    { label: "Başvuru", value: "1.284", meta: "+%8,4", tone: "ok" },
    { label: "KYC", value: "1.096", meta: "42 bekliyor", tone: "warn" },
    { label: "Ödeme", value: "984", meta: "18 başarısız", tone: "bad" },
    { label: "MSISDN", value: "947", meta: "6 bekliyor", tone: "live" },
    { label: "Aktivasyon", value: "921", meta: "%71,7", tone: "ok" },
  ],
  metrics: [
    { label: "Aktif müşteri", value: "284.910", meta: "+1.284 bugün", tone: "ok" },
    { label: "Açık sipariş", value: "1.462", meta: "6 blokaj", tone: "live" },
    { label: "Kesilen fatura", value: "218.604", meta: "196,2 Mn TL" },
    { label: "Açık talep", value: "842", meta: "7 SLA riski", tone: "warn" },
  ],
};

export const fallbackPlatformOpsView: PlatformOpsView = {
  generatedAt: "Snapshot",
  environment: integration.environment,
  statusLabel: "Snapshot",
  statusTone: "warn",
  pulse: {
    totalServices: 0,
    healthyServices: 0,
    degradedServices: 0,
    downServices: 0,
    totalRecords: 0,
    prometheusTargetsUp: 0,
    prometheusTargetsTotal: 0,
    connectorsRunning: 0,
    connectorsTotal: 0,
  },
  services: [],
  connectors: [],
  events: [],
  incident: {
    title: "Canlı platform snapshot alınamadı",
    value: "-",
    detail: "BFF platform endpoint'i yanıt vermedi.",
    tone: "warn",
  },
};

export const fallbackSystemStatesView: SystemStatesView = {
  generatedAt: "Snapshot",
  cards: [
    {
      eyebrow: "Offline",
      title: "Canlı sistem durumu alınamadı",
      status: "Snapshot",
      tone: "warn",
      description: "BFF platform endpoint'i yanıt verene kadar son bilinen durum desenleri gösterilir.",
      detail: "bff-service /platform/ops",
    },
  ],
};

export function useLiveResource<T>(fallback: T, loader: () => Promise<T>, deps: readonly unknown[] = []): LiveResource<T> {
  const [state, setState] = useState<LiveResource<T>>({
    data: fallback,
    loading: integration.live,
    error: null,
    live: false,
  });

  useEffect(() => {
    let active = true;
    setState((current) => ({ ...current, data: fallback, loading: integration.live, error: null }));

    if (!integration.live) {
      return () => {
        active = false;
      };
    }

    loader()
      .then((data) => {
        if (active) {
          setState({ data, loading: false, error: null, live: true });
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setState({ data: fallback, loading: false, error: errorMessage(error), live: false });
        }
      });

    return () => {
      active = false;
    };
  }, deps);

  return state;
}

export async function loadDashboardView(): Promise<DashboardView> {
  const summary = await apiRequest<DashboardSummaryDto>("bff", "/dashboard/summary");
  const count = (label: string) => summary.counters.find((counter) => counter.label === label)?.value ?? 0;
  const orders = count("orders");
  const subscriptions = count("subscriptions");
  const invoices = count("invoices");
  const tickets = count("tickets");

  return {
    lifecycle: [
      { label: "Başvuru", value: formatCount(orders), meta: "order-service", tone: "ok" },
      { label: "KYC", value: formatCount(subscriptions + 1), meta: "customer-service", tone: "warn" },
      { label: "Ödeme", value: formatCount(invoices), meta: "billing-service", tone: "live" },
      { label: "Aktivasyon", value: formatCount(subscriptions), meta: "subscription-service", tone: "ok" },
    ],
    metrics: [
      { label: "Aktif müşteri", value: formatCount(subscriptions + 1), meta: "Seed DB", tone: "ok" },
      { label: "Açık sipariş", value: formatCount(orders), meta: "order-service", tone: "live" },
      { label: "Kesilen fatura", value: formatCount(invoices), meta: "billing-service" },
      { label: "Açık talep", value: formatCount(tickets), meta: "ticket-service", tone: tickets > 0 ? "warn" : "ok" },
    ],
  };
}

export async function loadPlatformOpsView(): Promise<PlatformOpsView> {
  const snapshot = await apiRequest<PlatformOpsDto>("bff", "/platform/ops");
  return platformOpsFromDto(snapshot);
}

export async function loadSystemStatesView(): Promise<SystemStatesView> {
  return systemStatesFromOps(await loadPlatformOpsView());
}

export async function loadCustomerRows(): Promise<CustomerRow[]> {
  const customers = await apiRequest<CustomerDto[]>("customers");
  const rows = await Promise.all(customers.map(async (customer) => {
    const [contacts, addresses, subscriptions, invoices, tickets] = await Promise.all([
      safe(() => apiRequest<ContactDto[]>("customers", `/${customer.id}/contacts`), []),
      safe(() => apiRequest<AddressDto[]>("customers", `/${customer.id}/addresses`), []),
      safe(() => apiRequest<SubscriptionDto[]>("subscriptions", `?customerId=${customer.id}`), []),
      safe(() => apiRequest<InvoiceDto[]>("billing", `?customerId=${customer.id}`), []),
      safe(() => apiRequest<TicketDto[]>("tickets", `?customerId=${customer.id}`), []),
    ]);
    const primarySubscription = subscriptions[0];
    const primaryContact = primaryPhone(contacts) ?? primarySubscription?.msisdn ?? "-";
    const totalAmount = invoices.reduce((sum, invoice) => sum + toNumber(invoice.totalAmount), 0);
    const city = addresses.find((address) => address.isDefault)?.city ?? addresses[0]?.city ?? "-";

    return {
      uuid: customer.id,
      id: customer.customerNumber,
      name: fullName(customer),
      tckn: maskNationalId(customer.nationalId),
      msisdn: formatMsisdn(primaryContact),
      city,
      kyc: kycLabel(customer.status),
      status: customerStatusLabel(customer.status),
      lines: subscriptions.filter((subscription) => subscription.status === "ACTIVE").length,
      tickets: tickets.filter((ticket) => ticket.status === "OPEN" || ticket.status === "IN_PROGRESS").length,
      balance: formatTry(totalAmount),
      plan: primarySubscription ? planLabel(primarySubscription.planCode) : "-",
    };
  }));
  return rows.sort((left, right) => left.id.localeCompare(right.id)).reverse();
}

export async function loadCustomer360View(): Promise<Customer360View> {
  const [profile, contacts, addresses] = await Promise.all([
    apiRequest<{
      customer: CustomerDto;
      subscriptions: SubscriptionDto[];
      orders: OrderDto[];
      invoices: InvoiceDto[];
      tickets: TicketDto[];
    }>("bff", `/customers/${demoIds.customerId}/360`),
    safe(() => apiRequest<ContactDto[]>("customers", `/${demoIds.customerId}/contacts`), []),
    safe(() => apiRequest<AddressDto[]>("customers", `/${demoIds.customerId}/addresses`), []),
  ]);

  const customer = profile.customer;
  const subscription = profile.subscriptions[0];
  const invoice = profile.invoices[0];
  const quotas = subscription
    ? await safe(() => apiRequest<QuotaDto[]>("usage", `/subscriptions/${subscription.id}/quotas`), [])
    : [];
  const usage = usageFromQuotas(quotas);
  const ticket = profile.tickets[0];
  const order = profile.orders[0];
  const city = addresses.find((address) => address.isDefault)?.city ?? addresses[0]?.city ?? "-";
  const msisdn = primaryPhone(contacts) ?? subscription?.msisdn ?? "-";

  return {
    customerNumber: customer.customerNumber,
    fullName: fullName(customer),
    initials: initials(fullName(customer)),
    segment: segmentLabel(customer.segment, customer.customerType),
    nationalId: maskNationalId(customer.nationalId),
    city,
    msisdn: formatMsisdn(msisdn),
    status: customerStatusLabel(customer.status),
    kycDate: formatDate(customer.updatedAt),
    balance: formatTry(invoice?.totalAmount),
    invoiceStatus: invoiceStatusLabel(invoice?.status),
    plan: subscription ? planLabel(subscription.planCode) : "-",
    period: usage.period,
    overage: "84,20 TL",
    usage,
    timeline: buildTimeline(customer, order, invoice, ticket),
    ticketCount: profile.tickets.filter((item) => item.status === "OPEN" || item.status === "IN_PROGRESS").length,
  };
}

export async function loadCatalogPlans(): Promise<PlanCard[]> {
  const products = await apiRequest<ProductDto[]>("products");
  const planProducts = products.filter((product) => product.productType === "PLAN");
  const plans = await Promise.all(planProducts.map(async (product) => {
    const [plan, price] = await Promise.all([
      safe<PlanDto | null>(() => apiRequest<PlanDto>("products", `/${product.id}/plan`), null),
      safe<{ price: number | string; currency?: string | null } | null>(() => apiRequest<{ price: number | string; currency?: string | null }>("products", `/${product.id}/prices/effective`), null),
    ]);
    const features = plan?.features ?? [];
    const monthlyPrice = plan?.monthlyPrice ?? price?.price ?? 0;

    return {
      name: product.name,
      code: product.code,
      price: formatTry(monthlyPrice),
      data: featureLabel(features, "DATA_MB", "GB"),
      voice: featureLabel(features, "VOICE_MIN", "dk"),
      sms: featureLabel(features, "SMS_COUNT", ""),
      status: productStatusLabel(product.status),
      effective: `${formatDate(plan?.validFrom)} - ${plan?.validTo ? formatDate(plan.validTo) : "Süresiz"}`,
    };
  }));
  return plans.sort((left, right) => left.price.localeCompare(right.price, "tr"));
}

export async function loadInvoices(): Promise<InvoiceRow[]> {
  const [invoices, subscriptions, customers] = await Promise.all([
    apiRequest<InvoiceDto[]>("billing"),
    safe(() => apiRequest<SubscriptionDto[]>("subscriptions"), []),
    safe(() => apiRequest<CustomerDto[]>("customers"), []),
  ]);
  const customerById = new Map(customers.map((customer) => [customer.id, fullName(customer)]));
  const customerIdBySubscription = new Map(subscriptions.map((subscription) => [subscription.id, subscription.customerId]));

  return invoices.map((invoice) => {
    const customerId = invoice.subscriptionId ? customerIdBySubscription.get(invoice.subscriptionId) : undefined;
    return {
      id: invoice.invoiceNumber,
      customer: customerId ? customerById.get(customerId) ?? customerId : "Musteri bilgisi yok",
      period: formatPeriod(invoice.periodStart, invoice.periodEnd),
      total: formatTry(invoice.totalAmount),
      status: invoiceStatusLabel(invoice.status),
      date: formatDate(invoice.issuedAt ?? invoice.dueDate),
    };
  });
}

export async function loadUsageView(): Promise<UsageView> {
  const quotas = await apiRequest<QuotaDto[]>("usage", `/subscriptions/${demoIds.subscriptionId}/quotas`);
  return usageFromQuotas(quotas);
}

export async function loadTicketView(): Promise<TicketView> {
  const tickets = await apiRequest<TicketDto[]>("tickets");
  const selected = tickets.find((ticket) => ticket.ticketNumber === "TIC-3108") ?? tickets[0];
  if (!selected) {
    return fallbackTicketView;
  }

  const [customer, subscriptions, invoices, quotas] = await Promise.all([
    safe<CustomerDto | null>(() => apiRequest<CustomerDto>("customers", `/${selected.customerId}`), null),
    safe(() => apiRequest<SubscriptionDto[]>("subscriptions", `?customerId=${selected.customerId}`), []),
    safe(() => apiRequest<InvoiceDto[]>("billing", `?customerId=${selected.customerId}`), []),
    safe(() => apiRequest<QuotaDto[]>("usage", `/subscriptions/${demoIds.subscriptionId}/quotas`), []),
  ]);
  const subscription = subscriptions[0];
  const invoice = invoices[0];
  const usage = usageFromQuotas(quotas);

  return {
    queues: [
      { label: "Atanmamış", count: tickets.filter((ticket) => !ticket.assignedTeam).length },
      { label: "Bana atanmış", count: tickets.filter((ticket) => Boolean(ticket.assignedAgentId)).length },
      { label: "SLA riski", count: tickets.filter((ticket) => ticket.status !== "RESOLVED" && ticket.status !== "CLOSED").length },
      { label: "Müşteri bekleniyor", count: tickets.filter((ticket) => ticket.category === "Tahsilat").length },
      { label: "Çözüldü", count: tickets.filter((ticket) => ticket.status === "RESOLVED" || ticket.status === "CLOSED").length },
    ],
    ticketNumber: selected.ticketNumber,
    category: selected.category,
    subject: selected.subject,
    customerName: customer ? fullName(customer) : selected.customerId,
    customerNumber: customer?.customerNumber ?? selected.customerId,
    msisdn: formatMsisdn(subscription?.msisdn ?? "-"),
    plan: subscription ? planLabel(subscription.planCode) : "-",
    quota: `${usage.dataPercent} data`,
    balance: formatTry(invoice?.totalAmount),
    priority: priorityLabel(selected.priority),
    sla: slaLabel(selected.slaDueAt),
    description: selected.description,
  };
}

function usageFromQuotas(quotas: QuotaDto[]): UsageView {
  const data = quotas.find((quota) => quotaKind(quota) === "DATA_MB");
  const voice = quotas.find((quota) => quotaKind(quota) === "VOICE_MIN");
  const sms = quotas.find((quota) => quotaKind(quota) === "SMS_COUNT");

  return {
    period: data ? formatPeriod(data.periodStart, data.periodEnd) : fallbackUsageView.period,
    dataUsed: data ? formatGigabytes(quotaUsed(data)) : fallbackUsageView.dataUsed,
    dataTotal: data ? `${formatGigabytes(quotaTotal(data))} GB` : fallbackUsageView.dataTotal,
    dataPercent: data ? percentLabel(data.usagePercent) : fallbackUsageView.dataPercent,
    dataRemaining: data ? `${formatGigabytes(quotaRemaining(data))} GB kaldı` : fallbackUsageView.dataRemaining,
    voiceUsed: voice ? formatInteger(quotaUsed(voice)) : fallbackUsageView.voiceUsed,
    voiceTotal: voice ? `${formatInteger(quotaTotal(voice))} dk` : fallbackUsageView.voiceTotal,
    voicePercent: voice ? percentLabel(voice.usagePercent) : fallbackUsageView.voicePercent,
    voiceRemaining: voice ? `${formatInteger(quotaRemaining(voice))} dk kaldı` : fallbackUsageView.voiceRemaining,
    smsUsed: sms ? formatInteger(quotaUsed(sms)) : fallbackUsageView.smsUsed,
    smsTotal: sms ? `${formatInteger(quotaTotal(sms))} SMS` : fallbackUsageView.smsTotal,
    smsPercent: sms ? percentLabel(sms.usagePercent) : fallbackUsageView.smsPercent,
    smsRemaining: sms ? `${formatInteger(quotaRemaining(sms))} adet kaldı` : fallbackUsageView.smsRemaining,
    records: staticUsageRecords,
  };
}

function platformOpsFromDto(snapshot: PlatformOpsDto): PlatformOpsView {
  const services = snapshot.services.map((service) => ({
    name: service.service,
    label: service.label,
    health: platformStatusLabel(service.status),
    tone: platformStatusTone(service.status),
    latency: `${Math.round(service.latencyMs)} ms`,
    rate: service.records > 0 ? `${formatCount(service.records)} kayıt` : service.dataAvailable ? "health only" : "veri yok",
    detail: service.detail,
  }));
  const connectors = snapshot.connectors.map((connector) => ({
    name: connector.name,
    table: connector.totalTasks > 0 ? `${connector.runningTasks}/${connector.totalTasks} task` : "worker",
    status: connector.available ? "Sağlıklı" : platformStatusLabel(connector.status),
    tone: connector.available ? "ok" as const : platformStatusTone(connector.status) === "bad" ? "bad" as const : "warn" as const,
    detail: connector.detail,
  }));
  const events = snapshot.events.map((event) => ({
    type: event.type,
    service: event.service,
    status: event.status,
    tone: platformEventTone(event.status),
    detail: event.detail,
    time: formatTime(event.occurredAt),
  }));
  const incident = platformIncident(snapshot);

  return {
    generatedAt: formatTime(snapshot.generatedAt),
    environment: snapshot.environment,
    statusLabel: platformStatusLabel(snapshot.overallStatus),
    statusTone: platformEventTone(snapshot.overallStatus),
    pulse: snapshot.pulse,
    services,
    connectors,
    events,
    incident,
  };
}

function systemStatesFromOps(ops: PlatformOpsView): SystemStatesView {
  const pulse = ops.pulse;
  const unavailableServices = ops.services.filter((service) => service.tone === "bad");
  const degradedServices = ops.services.filter((service) => service.tone === "warn");
  return {
    generatedAt: ops.generatedAt,
    cards: [
      {
        eyebrow: "Control plane",
        title: "Genel platform durumu",
        status: ops.statusLabel,
        tone: ops.statusTone,
        description: `${pulse.healthyServices}/${pulse.totalServices} servis sağlıklı.`,
        detail: `Son kontrol ${ops.generatedAt}`,
      },
      {
        eyebrow: "Service health",
        title: unavailableServices.length > 0 ? "Kritik servis kesintisi" : "Servis erişilebilirliği",
        status: unavailableServices.length > 0 ? `${unavailableServices.length} down` : "UP",
        tone: unavailableServices.length > 0 ? "bad" : degradedServices.length > 0 ? "warn" : "ok",
        description: unavailableServices.length > 0
          ? unavailableServices.map((service) => service.name).join(", ")
          : degradedServices.length > 0
            ? `${degradedServices.length} servis veri katmanında riskli.`
            : "Tüm probed servisler actuator health üzerinden erişilebilir.",
        detail: `Degraded ${pulse.degradedServices} · Down ${pulse.downServices}`,
      },
      {
        eyebrow: "Data freshness",
        title: "Canlı kayıt kapsamı",
        status: `${formatCount(pulse.totalRecords)} kayıt`,
        tone: pulse.totalRecords > 0 ? "live" : "warn",
        description: "BFF ana domain endpointlerinden canlı kayıt sayımı aldı.",
        detail: "customer, product, order, subscription, usage, billing, ticket",
      },
      {
        eyebrow: "Kafka Connect",
        title: "CDC worker durumu",
        status: `${pulse.connectorsRunning}/${pulse.connectorsTotal}`,
        tone: pulse.connectorsTotal === 0 ? "warn" : pulse.connectorsRunning === pulse.connectorsTotal ? "ok" : "warn",
        description: ops.connectors.map((connector) => `${connector.name}: ${connector.detail}`).join(" · ") || "Connector bilgisi alınamadı.",
        detail: "kafka-connect REST",
      },
      {
        eyebrow: "Prometheus",
        title: "Scrape hedefleri",
        status: `${pulse.prometheusTargetsUp}/${pulse.prometheusTargetsTotal}`,
        tone: pulse.prometheusTargetsTotal === 0 ? "warn" : pulse.prometheusTargetsUp === pulse.prometheusTargetsTotal ? "ok" : "warn",
        description: "Prometheus up query sonucu canlı scrape hedefleri.",
        detail: "prometheus /api/v1/query?query=up",
      },
      {
        eyebrow: "UI integration",
        title: "Signal Atlas canlı bağlantı",
        status: integration.live ? "Live API" : "Snapshot",
        tone: integration.live ? "ok" : "warn",
        description: integration.live ? "Frontend BFF platform endpoint'inden veri okuyor." : "VITE_ENABLE_LIVE_API=false.",
        detail: `Ortam ${integration.environment}`,
      },
    ],
  };
}

function platformIncident(snapshot: PlatformOpsDto): PlatformOpsView["incident"] {
  const down = snapshot.services.find((service) => service.status === "DOWN");
  if (down) {
    return {
      title: `${down.service} erişilemiyor`,
      value: "DOWN",
      detail: down.detail,
      tone: "bad",
    };
  }
  const degraded = snapshot.services.find((service) => service.status === "DEGRADED");
  if (degraded) {
    return {
      title: `${degraded.service} veri katmanı riskli`,
      value: "DEGRADED",
      detail: degraded.detail,
      tone: "warn",
    };
  }
  const targets = snapshot.prometheus.targetsTotal > 0
    ? `${snapshot.prometheus.targetsUp}/${snapshot.prometheus.targetsTotal}`
    : "-";
  return {
    title: "Aktif kritik incident yok",
    value: targets,
    detail: "Servis health, connector worker ve Prometheus snapshot temiz.",
    tone: "ok",
  };
}

function platformStatusLabel(status: string): string {
  return {
    UP: "Sağlıklı",
    DEGRADED: "Riskli",
    DOWN: "Hata",
  }[status] ?? status;
}

function platformStatusTone(status: string): "ok" | "warn" | "bad" | "muted" {
  if (status === "UP") {
    return "ok";
  }
  return status === "DOWN" ? "bad" : "warn";
}

function platformEventTone(status: string): "ok" | "warn" | "bad" | "live" {
  if (status === "UP") {
    return "ok";
  }
  return status === "DOWN" ? "bad" : "warn";
}

function quotaKind(quota: QuotaDto): string | undefined {
  return quota.quotaType ?? quota.type;
}

function quotaTotal(quota: QuotaDto): number | string | undefined {
  return quota.totalAllowance ?? quota.total;
}

function quotaUsed(quota: QuotaDto): number | string | undefined {
  return quota.usedAmount ?? quota.used;
}

function quotaRemaining(quota: QuotaDto): number | string | undefined {
  return quota.remainingAmount ?? quota.remaining;
}

async function safe<T>(loader: () => Promise<T>, fallback: T): Promise<T> {
  try {
    return await loader();
  } catch {
    return fallback;
  }
}

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Canlı veri alınamadı.";
}

function fullName(customer: CustomerDto): string {
  return [customer.firstName, customer.lastName].filter(Boolean).join(" ") || customer.customerNumber;
}

function initials(name: string): string {
  return name.split(/\s+/).slice(0, 2).map((part) => part[0]).join("").toUpperCase() || "MX";
}

function primaryPhone(contacts: ContactDto[]): string | undefined {
  return contacts.find((contact) => contact.isPrimary && isPhone(contact))?.contactValue
    ?? contacts.find(isPhone)?.contactValue;
}

function isPhone(contact: ContactDto): boolean {
  return contact.contactType === "PHONE" || contact.contactType === "MOBILE";
}

function maskNationalId(value?: string | null): string {
  if (!value) {
    return "-";
  }
  return `${value.slice(0, 2)}•••••••${value.slice(-2)}`;
}

function formatMsisdn(value?: string | null): string {
  if (!value || value === "-") {
    return "-";
  }
  const compact = value.replace(/\D/g, "");
  if (compact.startsWith("90") && compact.length === 12) {
    return `+90 ${compact.slice(2, 5)} ${compact.slice(5, 8)} ${compact.slice(8, 10)} ${compact.slice(10)}`;
  }
  if (compact.startsWith("0") && compact.length === 11) {
    return `${compact.slice(0, 4)} ${compact.slice(4, 7)} ${compact.slice(7, 9)} ${compact.slice(9)}`;
  }
  return value;
}

function formatTry(value?: number | string | null): string {
  return `${toNumber(value).toLocaleString("tr-TR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} TL`;
}

function toNumber(value?: number | string | null): number {
  if (value == null) {
    return 0;
  }
  return typeof value === "number" ? value : Number.parseFloat(value);
}

function formatCount(value: number): string {
  return value.toLocaleString("tr-TR");
}

function formatInteger(value?: number | string | null): string {
  return Math.round(toNumber(value)).toLocaleString("tr-TR");
}

function formatGigabytes(value?: number | string | null): string {
  return (toNumber(value) / 1024).toLocaleString("tr-TR", { maximumFractionDigits: 1 });
}

function percentLabel(value?: number | string | null): string {
  return `%${Math.round(toNumber(value))}`;
}

function formatDate(value?: string | null): string {
  if (!value) {
    return "-";
  }
  return new Intl.DateTimeFormat("tr-TR", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(value));
}

function formatTime(value?: string | null): string {
  if (!value) {
    return "-";
  }
  return new Intl.DateTimeFormat("tr-TR", { hour: "2-digit", minute: "2-digit" }).format(new Date(value));
}

function formatPeriod(start?: string | null, end?: string | null): string {
  if (!start || !end) {
    return "-";
  }
  const startDate = new Date(start);
  const endDate = new Date(end);
  const sameMonth = startDate.getMonth() === endDate.getMonth() && startDate.getFullYear() === endDate.getFullYear();
  if (sameMonth) {
    return `${startDate.toLocaleDateString("tr-TR", { day: "2-digit" })}-${endDate.toLocaleDateString("tr-TR", { day: "2-digit", month: "short", year: "numeric" })}`;
  }
  return `${formatDate(start)} - ${formatDate(end)}`;
}

function kycLabel(status: string): string {
  if (status === "ACTIVE" || status === "SUSPENDED") {
    return "Onaylandı";
  }
  if (status === "CLOSED") {
    return "Reddedildi";
  }
  return "Bekliyor";
}

function customerStatusLabel(status: string): string {
  return {
    ACTIVE: "Aktif",
    PROSPECT: "Bekliyor",
    SUSPENDED: "Askıda",
    CLOSED: "Kapalı",
  }[status] ?? status;
}

function invoiceStatusLabel(status?: string | null): string {
  if (!status) {
    return "-";
  }
  return {
    DRAFT: "Taslak",
    ISSUED: "Kesildi",
    PARTIALLY_PAID: "Kısmi ödendi",
    PAID: "Ödendi",
    OVERDUE: "Gecikmiş",
    CANCELLED: "İptal",
  }[status] ?? status;
}

function productStatusLabel(status: string): string {
  return {
    DRAFT: "Planlandı",
    ACTIVE: "Aktif",
    RETIRED: "Arşiv",
  }[status] ?? status;
}

function priorityLabel(priority: string): string {
  return {
    LOW: "Düşük",
    MEDIUM: "Orta",
    HIGH: "Yüksek",
    CRITICAL: "Kritik",
  }[priority] ?? priority;
}

function segmentLabel(segment?: string | null, customerType?: string | null): string {
  if (customerType === "CORPORATE") {
    return "Kurumsal";
  }
  return segment === "VIP" ? "Bireysel VIP" : "Bireysel";
}

function planLabel(code: string): string {
  return planNames[code] ?? code;
}

function featureLabel(features: PlanFeatureDto[], type: string, unit: string): string {
  const feature = features.find((item) => item.featureType === type);
  if (!feature) {
    return "-";
  }
  if (feature.isUnlimited) {
    return "Limitsiz";
  }
  if (type === "DATA_MB") {
    return `${formatGigabytes(feature.allowance)} GB`;
  }
  return `${formatInteger(feature.allowance)}${unit ? ` ${unit}` : ""}`;
}

function buildTimeline(customer: CustomerDto, order?: OrderDto, invoice?: InvoiceDto, ticket?: TicketDto): Customer360View["timeline"] {
  return [
    { t: formatTime(customer.updatedAt), title: "KYC onaylandı", meta: "customer-service", tone: "ok" },
    order ? { t: formatTime(order.createdAt), title: "Sipariş oluşturuldu", meta: `${order.orderNumber} · ${planLabel(order.planCode)}`, tone: "live" } : null,
    invoice ? { t: formatTime(invoice.issuedAt), title: "Fatura kesildi", meta: `${invoice.invoiceNumber} · ${formatTry(invoice.totalAmount)}`, tone: "ok" } : null,
    ticket ? { t: formatTime(ticket.createdAt), title: "SLA riski oluştu", meta: `${ticket.ticketNumber} · ${ticket.category}`, tone: "warn" } : null,
  ].filter((item): item is Customer360View["timeline"][number] => item !== null);
}

function slaLabel(value?: string | null): string {
  if (!value) {
    return "-";
  }
  const diff = new Date(value).getTime() - Date.now();
  if (diff <= 0) {
    return "SLA geçti";
  }
  const totalMinutes = Math.floor(diff / 60000);
  const hours = Math.floor(totalMinutes / 60).toString().padStart(2, "0");
  const minutes = (totalMinutes % 60).toString().padStart(2, "0");
  return `${hours}:${minutes}:00`;
}
