# Telco CRM Platform

Telekomünikasyon CRM platformu — Spring Boot 4 + Java 21 ile geliştirilen, **microservices** mimarisi, **database-per-service** ve **multi-module Maven** paternlerini uygulayan eğitim projesi.

Detaylı analiz: [`/Users/tamerakdeniz/Personal/telcox/docs/telco-crm-microservices-mvp 2026-05-15 pmt 18.41.50.docx`](../docs/)

ER diyagramları: [`docs/microservice-er-diagrams/`](../docs/microservice-er-diagrams/)

---

## Mimari Özet

### Database-per-Service Pattern
Her mikroservis **kendi PostgreSQL container'ına** sahiptir. Servisler arası referanslar `FOREIGN KEY` ile değil, **UUID/business key** ile tutulur. Tutarlılık `Outbox + Inbox (Processed Event)` pattern ile event-driven sağlanır.

> **Port stratejisi:** Tüm host portları **1xxxx** aralığına shift edildi (örn. PostgreSQL `5432` → host `15432`) ki yerel makinede çalışan başka projelerle (örn. ayrı bir PostgreSQL, başka bir Spring app) çakışmasın. Container içindeki portlar **standart kalır**; servisler birbirini hâlâ `9001`, `5432`, `8761`, vb. üzerinden bulur.

| Servis | Container Port | PostgreSQL Container | Host Port (PG) | Host Port (App) | Database |
|---|---|---|---|---|---|
| `api-gateway` | 8080 | — | — | **18080** | — |
| `discovery-server` (Eureka) | 8761 | — | — | **18761** | — |
| `config-server` | 8888 | — | — | **18888** | — |
| `identity-service` | 9001 | `identity-postgres` | **15432** | **19001** | `identity_db` |
| `customer-service` | 9002 | `customer-postgres` | **15433** | **19002** | `customer_db` |
| `product-catalog-service` | 9003 | `product-postgres` | **15434** | **19003** | `product_db` |
| `order-service` | 9004 | `order-postgres` | **15435** | **19004** | `order_db` |
| `subscription-service` | 9005 | `subscription-postgres` | **15436** | **19005** | `subscription_db` |
| `usage-service` | 9006 | `usage-postgres` | **15437** | **19006** | `usage_db` |
| `billing-service` | 9007 | `billing-postgres` | **15438** | **19007** | `billing_db` |
| `payment-service` | 9008 | `payment-postgres` | **15439** | **19008** | `payment_db` |
| `notification-service` | 9009 | `notification-postgres` | **15440** | **19009** | `notification_db` |
| `ticket-service` | 9010 | `ticket-postgres` | **15441** | **19010** | `ticket_db` |

### Destek servislerinin host portları

| Servis | Host Port | Container Port |
|---|---|---|
| Redis | **16379** | 6379 |
| Kafka (broker) | **19092** | 9092 |
| Kafka UI | **18090** | 8080 |
| Kafka Connect (Debezium) | **18084** | 8083 |
| Keycloak | **18083** | 8080 |
| Keycloak PostgreSQL | **15442** | 5432 |
| Zipkin | **19411** | 9411 |
| MailHog SMTP | **11025** | 1025 |
| MailHog Web | **18025** | 8025 |
| pgAdmin | **15050** | 80 |

> **Önemli:** Her mikroservis kendine ait **bağımsız bir PostgreSQL Docker container'ı** üzerinde çalışır. Container'lar tek bir Docker network (`telcox-net`) üzerinde haberleşir; servisler birbirinin DB'sine doğrudan erişemez, core akışlarda **Kafka event'leri** ve ihtiyaç halinde açıkça belgelenmiş query API'leri ile haberleşir.

### Multi-Module Maven Yapısı
```
telco-crm-platform/                  (parent POM — BOM ve modül listesi)
├── docker-compose.yml               (tüm sistemi ayağa kaldırır)
├── telco-common/                    (paylaşılan kütüphane: DTO, exception, event)
├── infrastructure/
│   ├── discovery-server/            (Eureka)
│   ├── config-server/               (Spring Cloud Config - native, classpath)
│   └── api-gateway/                 (Spring Cloud Gateway)
└── services/
    ├── identity-service/            (her servis kendi Dockerfile'ı ile)
    ├── customer-service/
    ├── product-catalog-service/
    ├── order-service/
    ├── subscription-service/
    ├── usage-service/
    ├── billing-service/
    ├── payment-service/
    ├── notification-service/
    └── ticket-service/
```

---

## Teknoloji Yığını

| Katman | Teknoloji | Versiyon |
|---|---|---|
| Dil | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.6 |
| Spring Cloud | Gateway, Config, Eureka | 2025.1.1 |
| Build | Maven Multi-Module | 3.9+ |
| Database | PostgreSQL | 16 (her servise ayrı container) |
| Cache / Idempotency | Redis | 7 |
| Broker | Apache Kafka (KRaft) | 7.7.1 |
| Migration | Flyway | (BOM) |
| ORM | Spring Data JPA + Hibernate | (BOM) |
| Mapping | MapStruct | 1.6.3 |
| Auth | Spring Security + JWT (jjwt) | 0.12.6 |
| Docs | Springdoc OpenAPI | 3.0.3 |
| Resilience | Resilience4j | 2.4.0 |
| Observability | Micrometer + Zipkin + OpenTelemetry | (BOM) |
| Test | JUnit 5, Mockito, Testcontainers | 1.20.4 |
| Container | Docker + Docker Compose v2 | — |
| Operasyon UI | React + TypeScript + Vite | Signal Atlas |

---

## Signal Atlas Operasyon Arayüzü

Stitch tasarım sistemi, `frontend/` altında 18 ekranlık gezilebilir bir React
uygulaması olarak projeye entegre edilmiştir. Arayüz müşteri/KYC, katalog,
sipariş saga, abonelik, kullanım, faturalama, ödeme, bildirim, ticket, yönetim
ve platform operasyon akışlarını içerir.

Backend modüllerinde REST controller katmanı henüz uygulanmadığı için arayüz
varsayılan olarak güvenli demo modunda çalışır. Gateway route sözleşmeleri
`frontend/src/api.ts` içinde hazırdır; canlı çağrılar şu değişken ile açılır:

```bash
VITE_ENABLE_LIVE_API=true
```

Lokal frontend geliştirme:

```bash
cd frontend
npm install
npm run dev
```

Adresler:

| Çalışma şekli | Adres |
|---|---|
| Vite development server | http://localhost:5173 |
| Docker Compose | http://localhost:15173 |

Frontend build kontrolü:

```bash
cd frontend
npm run build
```

---

## Hızlı Başlangıç (Tek Komutla Tüm Sistem)

### Önkoşullar
- JDK 21
- Maven 3.9+
- Docker Desktop (veya Docker Engine + Compose v2)
- Host portları **15432-15441** (PG), **16379** (Redis), **19092** (Kafka), **18090** (Kafka UI), **18084** (Kafka Connect), **19411** (Zipkin), **11025/18025** (MailHog), **15050** (pgAdmin), **18761** (Eureka), **18888** (Config), **18080** (Gateway), **19001-19010** (servisler) boş olmalı

### 1. JAR'ları Üret

```bash
cd telco-crm-platform
mvn clean package -DskipTests
```

Bu adım `telco-common`'ı yerel Maven cache'e (`~/.m2/repository`) yazar ve 13 servis için `target/*.jar` üretir. Dockerfile'lar bu jar'ları kullanır.

### 2. Tüm Sistemi Ayağa Kaldır

```bash
docker compose up -d --build
```

Bu komut sistem container'larını paralel ayağa kaldırır:

| Grup | Container'lar |
|---|---|
| **10 ayrı PostgreSQL** | `identity-postgres`, `customer-postgres`, `product-postgres`, `order-postgres`, `subscription-postgres`, `usage-postgres`, `billing-postgres`, `payment-postgres`, `notification-postgres`, `ticket-postgres` |
| **3 altyapı servisi** | `discovery-server` (Eureka), `config-server`, `api-gateway` |
| **10 iş mikroservisi** | `identity-service`, `customer-service`, `product-catalog-service`, `order-service`, `subscription-service`, `usage-service`, `billing-service`, `payment-service`, `notification-service`, `ticket-service` |
| **Destek servisler** | `redis`, `kafka`, `kafka-connect`, `kafka-ui`, `zipkin`, `mailhog`, `pgadmin` |

Her servis kendi PG container'ını `service_healthy` ile bekler; Kafka ve discovery-server hazır olmadan açılmaz.

### 3. Ayağa Kalkış Durumunu İzle

```bash
docker compose ps                           # tum container'lar
docker compose logs -f identity-service     # tek servisin log'u
docker compose logs --tail=50 -f            # tum sistem (uzun olur)
```

### 4. Sağlık Kontrolü

| Endpoint | Adres |
|---|---|
| Eureka dashboard | http://localhost:18761 |
| API Gateway routes | http://localhost:18080/actuator/gateway/routes |
| Identity Swagger | http://localhost:19001/swagger-ui.html |
| Kafka UI | http://localhost:18090 |
| Kafka Connect REST API | http://localhost:18084/connectors |
| Zipkin | http://localhost:19411 |
| MailHog (SMTP UI) | http://localhost:18025 |
| pgAdmin | http://localhost:15050 (admin@telcox.com / admin) |

pgAdmin açılınca **10 PostgreSQL sunucusu** "Telcox CRM" grubunda otomatik tanımlı gelir; her birinin şifresi `telcox`.

### 5. Sistemi Durdur / Sıfırla

```bash
docker compose down                # container'lari durdurur (veriler kalir)
docker compose down -v             # +veri volume'lerini de siler (temiz baslangic)
```

---

## Lokal Geliştirme (mvn spring-boot:run)

Tüm container'ları başlatmadan, sadece **PostgreSQL** + **Kafka** + **Redis** ayağa kaldırıp tek bir servisi IDE/terminal'den çalıştırabilirsin.

```bash
# Sadece altyapi + tum PG'leri baslat
docker compose up -d \
  identity-postgres customer-postgres product-postgres order-postgres \
  subscription-postgres usage-postgres billing-postgres payment-postgres \
  notification-postgres ticket-postgres \
  redis kafka mailhog

# Discovery + Config + Gateway'i lokal calistir
mvn -pl infrastructure/discovery-server spring-boot:run     # Terminal 1
mvn -pl infrastructure/config-server    spring-boot:run     # Terminal 2
mvn -pl infrastructure/api-gateway      spring-boot:run     # Terminal 3

# Identity service'i lokal calistir
mvn -pl services/identity-service spring-boot:run           # Terminal 4
```

`application.yml` default değerleri her servisin doğru host port'una bağlanır (`identity` → `localhost:15432`, `customer` → `localhost:15433`, vb.).

> `.env.example` dosyasını `.env` olarak kopyalayıp override edebilirsin.

---

## Konfigürasyon Hiyerarşisi

1. **`infrastructure/config-server/src/main/resources/config/application.yml`** — Tüm servislerin paylaştığı ortak ayarlar (logging, actuator, resilience4j vb.)
2. **`services/<svc>/src/main/resources/application.yml`** — Servise özel ayarlar (port, DB adı, Kafka group-id)
3. **Docker compose `environment:`** — Container içinde `DB_HOST`, `KAFKA_BOOTSTRAP`, `EUREKA_URL` vb. override edilir
4. **Process env / `.env`** — Lokal `mvn spring-boot:run` çalıştırırken override için

---

## Servis İletişim Modeli

| Senaryo | Tip | Teknoloji |
|---|---|---|
| Gateway → Servis | Senkron | Spring Cloud Gateway + Eureka (`lb://`) |
| Servis → Servis (query) | Senkron | Belgelenmiş query API + Resilience4j |
| Servis → Servis (event) | Asenkron | Kafka + Outbox pattern |
| CDR → Usage Service | Asenkron | Kafka |

**Database-per-service** prensibi gereği bir mikroservis **başka bir servisin DB'sine doğrudan erişemez**. Veri ihtiyacı sadece API çağrısı veya event ile karşılanır.

Mimari kararlar:

- [ADR-0001: Spring Boot / Spring Cloud version baseline](docs/adr/ADR-0001-spring-boot-cloud-baseline.md)
- [ADR-0002: Remove OpenFeign from core service communication](docs/adr/ADR-0002-remove-openfeign-from-core-service-communication.md)
- [Event Backbone Standard](docs/event-backbone.md)

---

## Geliştirme Notları

### `ddl-auto: validate`
Her servis Flyway ile schema yönetir. Hibernate sadece **validate** eder, asla tablo oluşturmaz. Yeni tablo eklemek için `services/<svc>/src/main/resources/db/migration/V{N}__<desc>.sql` ekle.

### Outbox Pattern
Her domain değişikliğiyle aynı transaction içinde `*_OUTBOX_EVENT` tablosuna kayıt atılır. Arka planda scheduler bu tabloyu Kafka'ya publish eder. Bkz. analiz dokümanı §9.3.

### Idempotent Consumer
Kafka'dan tüketilen her event'in `eventId`'si `*_PROCESSED_EVENT.event_id` UNIQUE constraint ile kontrol edilir; aynı event tekrar gelirse atlanır.

### Event Envelope / Topic Standardi
Kafka domain event'leri `telco-common` icindeki `EventEnvelope<T>` sozlesmesini kullanir. Zorunlu alanlar: `eventId`, `type`, `aggregateId`, `correlationId`, `schemaVersion`.

Topic formati PR #4 Debezium connector ciktilariyla aynidir: `telcox.events.<aggregate_type>`.

Retry ve DLQ formati: `<topic>.retry.0`, `<topic>.retry.1`, `<topic>.dlq`. Detaylar `docs/event-backbone.md` dosyasindadir.

### `telco-common` Değişikliği
`telco-common` modülünde değişiklik yaptıktan sonra:
```bash
mvn -pl telco-common -am clean install -DskipTests
```
sonrasında ilgili servisleri `docker compose up -d --build <service>` ile yeniden build et.

---

## Profiller

| Profile | Açıklama |
|---|---|
| `default` | Lokal geliştirme (localhost altyapı) |
| `docker` | Container içinden çalıştırma (service-discovery: container adları) |
| `prod` | Production (env var'larla yönetilen secret'lar) |

---

## Sıradaki Adımlar (Ödev Yol Haritası)

- [ ] Her servis için domain entity'leri (`@Entity`) ve repository'leri ekle
- [ ] ER diagramına göre tüm tabloları içeren `V2__...sql` migration'larını yaz
- [ ] REST controller + DTO + MapStruct mapper katmanları
- [ ] OpenAPI dokümanı her servis için tamamla
- [ ] Outbox publisher worker (`@Scheduled` + transactional outbox)
- [ ] Kafka producer/consumer + JSON serializer config
- [ ] JWT filter `api-gateway`'de (relay → `X-User-Id`, `X-User-Roles`)
- [ ] Testcontainers ile integration test setup
- [ ] CI/CD pipeline (GitHub Actions)

---

## Lisans
Eğitim projesi. Üretim sistemlerinde kullanılmadan önce güvenlik, performans ve uyumluluk gözden geçirilmelidir.
