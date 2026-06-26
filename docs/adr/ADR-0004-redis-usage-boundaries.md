# ADR-0004 — Redis Kullanım Sınırları (cache / idempotency / rate-limit; source-of-truth değil)

- **Durum:** Önerilen (Proposed)
- **Tarih:** 2026-06-19
- **Sahip:** _(senin adın)_
- **İlgili kod:** `infrastructure/api-gateway` (redis-reactive), `services/identity-service` (redis), `docker-compose.yml` (redis, host `16379`)

---

## 1. Bağlam

Projede tek bir Redis instance'ı (host `16379`) var ve şu an iki yerde bağımlılık
eklenmiş durumda: api-gateway (reactive) ve identity-service. Redis'in **kalıcı veri
deposu gibi** kullanılması, database-per-service ve event-driven tutarlılık modelini
bozar. Bu ADR Redis'in **izinli** ve **yasak** kullanımlarını sabitler.

## 2. Karar

Redis **yalnızca kaybı tolere edilebilir, türetilebilir (derived) veriler** için kullanılır.
**Asla** bir verinin tek doğru kaynağı (source-of-truth) olamaz. Tek doğru kaynak her
zaman ilgili servisin PostgreSQL'idir.

### 2.1 İzinli kullanımlar

| Kullanım | Açıklama | TTL zorunlu? |
|---|---|---|
| **Cache** | Postgres/event read-model'inden türetilmiş, yeniden üretilebilir veri | ✅ Evet |
| **Idempotency yardımcısı** | Kısa süreli "bu istek/işlem görüldü mü" işareti (asıl kayıt yine `*_PROCESSED_EVENT` tablosunda) | ✅ Evet |
| **Rate-limit sayaçları** | Gateway'de istek sayacı (INF-04 RequestRateLimiter) | ✅ (pencere) |
| **Token denylist / session helper** | Çıkış yapılmış/iptal token'ların kısa ömürlü kara listesi (SEC-06) | ✅ (token exp'i kadar) |
| **Dağıtık kilit (gerekirse)** | Kısa kritik bölüm; yalnızca best-effort | ✅ Evet |

### 2.2 Yasak kullanımlar

- ❌ Kalıcı iş verisi (müşteri, sipariş, fatura, abonelik) tutmak.
- ❌ Servisler arası **paylaşılan** veri deposu olarak kullanmak (her servis kendi
  prefix'iyle ayrı mantıksal alan kullanır; ortak okuma/yazma yok).
- ❌ TTL'siz, süresiz veri yazmak.
- ❌ Redis'i tek tutanak kabul edip Postgres'e yazmamak (write-behind tek başına yasak).

### 2.3 Anahtar (key) konvansiyonu

```
<service>:<amac>:<id>
örn:  gateway:ratelimit:ip:1.2.3.4
      gateway:denylist:jti:<jti>
      customer:cache:profile:<customerId>
      payment:idempotency:<sha256>
```

Her servis **yalnızca kendi `<service>:` prefix'ine** yazar.

## 3. Sonuçlar

**Artılar:** Redis kaybı (restart/flush) sistemi bozmaz; yalnızca cache/sayaç soğur,
veriler Postgres'ten yeniden ısınır. Sorumluluk sınırları net.

**Eksiler:** Geliştiriciler "kolay olduğu için" Redis'e kalıcı veri yazma eğilimine
karşı disiplinli olmalı; PR review'da bu ADR'a referansla denetlenir.

## 4. Takip işleri

- SEC-06 (token denylist) bu ADR'ın §2.1 sınırlarına uyacak.
- CACHE-04 payment guard atomik `SET NX` + TTL kullanır; yalnızca başarısız
  işlemlerde lease token'ı doğrulanarak key serbest bırakılır.
- Cache eklenen her serviste TTL zorunluluğu code review checklist'ine girer.
