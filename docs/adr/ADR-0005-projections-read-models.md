# ADR-0005 — Servis Bazlı Projection / Read-Model Sorumlulukları

- **Durum:** Önerilen (Proposed)
- **Tarih:** 2026-06-19
- **Sahip:** _(senin adın)_ · Dayanak: **ADR-0003** (event/command/reply), `db.sql` logical ref'leri
- **İlgili:** ARCH-02 (Feign çıkışı sonrası bu projeksiyonlar zorunlu hale gelir)

---

## 1. Bağlam

Database-per-service nedeniyle hiçbir servis başka servisin tablosuna FK ile bağlanamaz.
`db.sql`'deki tüm cross-service alanlar `-- logical ref -> X-service.Y.id` yorumuyla
işaretli. Feign çıkınca (ARCH-02) bu veriler **canlı çağrıyla** alınamaz; her servis
ihtiyaç duyduğu yabancı veriyi **event'lerden besleyeceği lokal bir read-model'e**
(projeksiyon) yazmak zorundadır.

Bu ADR, hangi servisin hangi event'ten hangi minimal read-model'i tutacağını sabitler.

## 2. İlke

- Projeksiyon **minimaldir**: sadece o servisin iş kuralı/görüntüleme için gereken alanlar
  (örn. ad, durum, plan adı), tüm aggregate değil.
- Projeksiyon **türetilmiştir**: source-of-truth kaynak serviste kalır; projeksiyon eventual
  consistent'tir ve stale olabilir.
- Projeksiyon tablosu `<service>_<source>_projection` adıyla, kaynağın UUID'siyle anahtarlanır.
- Projeksiyonu besleyen consumer **idempotent**'tir (EVT-06, `*_PROCESSED_EVENT`).

## 3. Read-model haritası (db.sql logical ref'lerinden türetildi)

| Tüketen servis | Tutması gereken read-model | Kaynak servis | Besleyen event(ler) (öneri) |
|---|---|---|---|
| **customer-service** | user projection (id, email, displayName) | identity-service | `UserRegistered`, `UserUpdated` |
| **order-service** | customer projection (id, ad, durum), product projection (id, ad, fiyat) | customer, product-catalog | `CustomerRegistered/Updated`, `ProductPublished/Updated` |
| **subscription-service** | customer, plan/product, msisdn durumu | customer, product-catalog, (inventory) | `CustomerRegistered`, `PlanPublished`, `MsisdnReserved/Released` |
| **billing-service** | customer (billing account için), subscription (fatura için) | customer, subscription | `CustomerRegistered`, `SubscriptionActivated/Changed` |
| **payment-service** | customer, invoice projection (id, tutar, durum) | customer, billing | `CustomerRegistered`, `InvoiceIssued/Updated` |
| **usage-service** | subscription projection (id, plan, kota) | subscription | `SubscriptionActivated/PlanChanged` |
| **notification-service** | customer iletişim tercihi/profili | customer | `CustomerRegistered/Updated`, `ConsentChanged` |
| **ticket-service** | customer projection, user (atanan/oluşturan) projection | customer, identity | `CustomerRegistered`, `UserRegistered/Updated` |

> **identity-service.USER.id** referansı neredeyse her serviste geçiyor (actorUserId,
> changedByUserId, authorUserId...). Çoğu yerde bu sadece **denetim/görüntüleme** içindir;
> bu durumda tam projeksiyon yerine sadece `userId + displayName` cache'lemek yeterli (ADR-0004 cache).

## 4. Modül olmayan kaynaklar (dikkat)

`db.sql`'de `CAMPAIGN_SERVICE_*` ve `INVENTORY_SERVICE_*` tabloları var ama bunlar
`pom.xml` modül listesinde **yok** (henüz servis değil). subscription-service'in
`msisdn` referansı bir **inventory-service** varsayar. Karar: inventory/campaign ayrı
servis mi, yoksa mevcut bir servisin parçası mı? → Bu **ARCH** kapsamında netleştirilmeli;
bu ADR şimdilik onları "ileride servisleşecek" olarak işaretler.

## 5. Sonuçlar

**Artılar:** Her servis kendi kendine yeter; senkron bağımlılık (ve Feign) kalkar;
okuma hızlı (lokal tablo).

**Eksiler:** Veri çoğaltma (duplication); projeksiyon drift'i için reconcile/replay
stratejisi gerekebilir; ilk yükleme (bootstrap) için event replay veya snapshot gerekir.

## 6. Takip işleri

- Her serviste projeksiyon tabloları için `V2__projections.sql` migration'ları (ayrı task).
- Bootstrap/replay stratejisi (EVT kapsamı).
- inventory/campaign servisleşme kararı (ARCH).
