# ADR-0007 — Fatura Kalemlerinde Tarife/Kota Fiyat Kaynağı

- **Durum:** Önerilen (Proposed)
- **Tarih:** 2026-07-08
- **Sahip:** _(senin adın)_ · İlgili: **ADR-0005** (projection sorumlulukları)
- **İlgili kod:** `services/billing-service` (`api/BasePlanChargeRequest.java`,
  `api/AddonLineRequest.java`, `api/OverageRequest.java`,
  `service/InvoiceLineService.java`)

---

## 1. Bağlam

BILL-02 (FR-22), fatura kalemlerinin (base plan, addon, VAS, overage) doğru
tutarlarla hesaplanmasını istiyor. Ancak bu tutarların "source of truth"u
`product-catalog-service`'te (`ProductPrice`, `Plan.monthlyPrice`,
`PlanFeature.allowance`) — billing-service'in kendi veritabanında bu veri
**yok**. ADR-0002 gereği senkron servisler-arası çağrı (Feign vb.)
kullanılamıyor; ADR-0005 gereği bu tür yabancı veri yerel bir **projection**
ile event'ten beslenmeli.

Şu an billing-service'te `subscription_projection` ve `usage_projection`
var (subscription-service ve usage-service event'lerinden beslenen), ama
product-catalog-service'ten gelen bir **tariff-price projection** henüz yok.

## 2. Karar

BILL-02 kapsamında, eksik olan projection'ı beklemek yerine, fatura kalemi
ekleme uç noktaları (`POST /invoices/{id}/items/base-plan`, `/addon`,
`/overage`) **tutarı/kotayı dışarıdan parametre olarak alacak şekilde**
tasarlandı (`BasePlanChargeRequest.monthlyPrice`, `OverageRequest.allowance`
gibi). Bu sayede:

- Kalem oluşturma, vergi hesaplama ve toplam güncelleme mantığı (asıl iş
  kuralı) doğru ve eksiksiz çalışır durumda.
- Fiyat/kota verisinin *nereden* geleceği (manuel girdi mi, yoksa gerçek bir
  `TariffPriceProjection` mi) ayrı ve izole bir sorun olarak bırakıldı.

## 3. Sonuçlar

**Artılar:** BILL-02 teslim edilebilir durumda; hesaplama mantığı
(`InvoiceLineService`) ADR-0002/0005 ile çelişmiyor, hiçbir senkron
servis-arası çağrı eklenmedi.

**Eksiler:** Bugünkü haliyle bill-run otomasyonu (BILL-01), base plan/overage
kalemlerini **otomatik ekleyemez** — bu bilgiyi çağıran taraf (bir operatör,
ya da ileride yazılacak bir orkestrasyon katmanı) sağlamalıdır. Yani BILL-01
ile BILL-02 arasında hâlâ manuel bir köprü var.

## 4. Takip işleri

- `product-catalog-service` içinde `ProductPriceChanged`/`ProductActivated`
  event'lerini yayınlayan outbox zaten var (bkz. CAT-01). billing-service'e
  bunları tüketen bir `TariffPriceProjectionEventListener` +
  `tariff_price_projection` tablosu eklenmeli (ADR-0005'teki
  `SubscriptionProjectionEventListener` ile aynı desen).
- Aynı şekilde `PlanFeature` (kota/allowance) için de bir projection
  gerekiyor; bu hazır olunca `OverageRequest.allowance` parametresi
  kaldırılıp otomatik okunabilir.
- Bu projeksiyonlar hazır olunca, `BillRunService.runForPeriod()` içine
  `InvoiceLineService` çağrıları otomatik olarak eklenip BILL-01/02 arasındaki
  manuel köprü kapatılmalı.
