# ADR-0003 — Asenkron Servis İletişim Standardı (Event / Command / Reply-Projection)

- **Durum:** Önerilen (Proposed)
- **Tarih:** 2026-06-19
- **Sahip:** _(senin adın)_ · İlgili: ARCH-02 (Feign çıkışı), EVT-01..EVT-08
- **İlgili kod:** `telco-common/src/main/java/com/telcox/common/event/DomainEvent.java`

---

## 1. Bağlam

TelcoX **database-per-service** prensibiyle çalışır: bir servis başka bir servisin
veritabanına erişemez (FK yok, yalnızca UUID/business key ile logical ref). Bugün
servisler arası senkron çağrı için OpenFeign + Eureka kullanımı README'de geçiyor,
ancak ARCH-02 ile Feign'in **core servis iletişiminden çıkarılması** kararlaştırılıyor.

Bu durumda servisler arası **veri tutarlılığı** ve **iş akışı koordinasyonu** için
net bir asenkron mesajlaşma standardına ihtiyaç var. Bu ADR bu standardı tanımlar.

## 2. Karar

Servisler arası iletişim **üç mesaj türüne** ayrılır. Her türün anlamı, yönü ve
sahipliği farklıdır:

### 2.1 Event (olay) — "olmuş bir olgu"

- **Anlam:** Geçmiş zamanlı, değişmez bir gerçek. (`CustomerRegistered`, `OrderConfirmed`, `InvoiceIssued`.)
- **Yön:** Üreten servis → ilgilenen herkese (fan-out, publish/subscribe).
- **Sahiplik:** Yayınlayan servis olayın sahibidir; tüketiciyi tanımaz/umursamaz.
- **Teslim:** Outbox pattern ile **at-least-once**. Tüketici **idempotent** olmak zorunda (EVT-06).
- **İsimlendirme:** `<Aggregate><PastTenseVerb>` → `SubscriptionActivated`.

### 2.2 Command (komut) — "şunu yap"

- **Anlam:** Belirli bir servisten **tek bir** alıcı servise talimat. (`ReserveMsisdn`, `ChargePayment`.)
- **Yön:** Gönderen → tek hedef (point-to-point, tek consumer group).
- **Sahiplik:** **Alıcı** servis komutun sözleşmesinin sahibidir (kabul ettiği komutu o tanımlar).
- **Kullanım:** Saga adımlarında (örn. `ORDER_SERVICE_SAGA_STATE`) bir sonraki adımı tetiklemek için.
- **İsimlendirme:** `<ImperativeVerb><Aggregate>` → `ReserveMsisdn`.

### 2.3 Reply / Projection (yanıt / projeksiyon) — "sonucu bildir / read-model güncelle"

- **Reply:** Bir komutun sonucudur (`MsisdnReserved` / `MsisdnReservationFailed`). Saga
  bu reply'ları dinleyip ilerler veya kompanzasyon (compensation) yapar.
- **Projection:** Bir event tüketildiğinde, tüketen servisin **kendi lokal read-model'ini**
  güncellemesidir. Detay: **ADR-0005**. (Örn. order-service, `CustomerRegistered`'dan
  kendi customer projeksiyonunu besler.)

### 2.4 Senkron iletişim nerede serbest?

| Senaryo | İzin | Teknoloji |
|---|---|---|
| Gateway → Servis | ✅ Serbest | Spring Cloud Gateway (`lb://`) |
| Servis → Servis (veri/komut) | ❌ Yasak (Feign çıkıyor) | Kafka event/command/reply |
| Servis → Servis (zorunlu senkron okuma) | ⚠️ Yalnızca ADR ile istisna | (gerekçeli, geçici) |

**Kural:** Bir servis ihtiyacı olan veriyi ya event'lerden kendi read-model'ine
projekte eder ya da komut/reply ile alır; **canlı senkron çağrı varsayılan olarak yasaktır.**

## 3. Event Envelope (zarf) standardı

Tüm mesajlar `telco-common`'daki `DomainEvent` sözleşmesini taşır. Minimum alanlar
(EVT-01 ile birebir uyumlu):

| Alan | Tip | Açıklama |
|---|---|---|
| `eventId` | UUID | Global benzersiz; idempotency anahtarı (PROCESSED_EVENT.event_id) |
| `eventType` | String | `CustomerRegistered` vb. |
| `sourceService` | String | Üreten servis |
| `aggregateId` | UUID | Olayın ait olduğu aggregate |
| `aggregateType` | String | `CUSTOMER`, `ORDER`, ... |
| `occurredAt` | OffsetDateTime (UTC) | Oluşma zamanı |
| `correlationId` | String | Gateway tarafından enjekte (INF-04 ile uyumlu) |
| `schemaVersion` | int | Event schema major versiyonu; `DomainEvent` varsayılanı `1` |

## 4. Sonuçlar

**Artılar:** Servisler gevşek bağlı; database-per-service ihlal edilmez; saga'lar
reply/compensation ile yönetilebilir; idempotency standardı net.

**Eksiler:** Eventual consistency'ye alışmak gerekir; read-model'ler güncel-olmayabilir
(stale) pencereler içerir; debugging için correlationId + tracing zorunlu.

## 5. Takip işleri (bu ADR'ı uygulayan task'lar)

- Event envelope ve `schemaVersion()` kuralları EVT-01/EVT-08 kapsamında `docs/event-backbone.md` içinde detaylandırılır.
- Topic naming standardı EVT-02'de tanımlanacak; bu ADR'a referans verecek.
- Outbox tablo kolonları Debezium Outbox SMT'ye uyumlanacak (EVT-03).
- Read-model sorumlulukları **ADR-0005**'te listelenir.
