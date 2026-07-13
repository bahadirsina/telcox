export const integration = {
  baseUrl: import.meta.env.VITE_API_BASE_URL ?? "/api/v1",
  live: import.meta.env.VITE_ENABLE_LIVE_API === "true",
  environment: import.meta.env.VITE_ENVIRONMENT ?? "UAT",
} as const;

export type ServiceKey =
  | "identity"
  | "bff"
  | "customers"
  | "products"
  | "orders"
  | "subscriptions"
  | "usage"
  | "billing"
  | "payments"
  | "notifications"
  | "tickets";

export const serviceRoutes: Record<ServiceKey, string> = {
  identity: "/auth",
  bff: "/bff",
  customers: "/customers",
  products: "/products",
  orders: "/orders",
  subscriptions: "/subscriptions",
  usage: "/usage",
  billing: "/billing",
  payments: "/payments",
  notifications: "/notifications",
  tickets: "/tickets",
};

export class IntegrationDisabledError extends Error {
  constructor() {
    super("Canlı servis bağlantısı henüz etkin değil.");
    this.name = "IntegrationDisabledError";
  }
}

export async function apiRequest<T>(service: ServiceKey, path = "", init?: RequestInit): Promise<T> {
  if (!integration.live) {
    throw new IntegrationDisabledError();
  }

  const response = await fetch(`${integration.baseUrl}${serviceRoutes[service]}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      "X-TelcoX-Client": "signal-atlas",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    const correlationId = response.headers.get("x-correlation-id") ?? "n/a";
    throw new Error(`İstek başarısız (${response.status}). Korelasyon: ${correlationId}`);
  }

  return response.json() as Promise<T>;
}
