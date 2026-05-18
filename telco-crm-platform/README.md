# Telco CRM Platform

Telekomünikasyon CRM platformu — Spring Boot 3 + Java 21 ile geliştirilen, **microservices** mimarisi ve **database-per-service** paterni uygulayan eğitim projesi.

Detaylı analiz: [`/Users/tamerakdeniz/Personal/telcox/docs/telco-crm-microservices-mvp 2026-05-15 pmt 18.41.50.docx`](../docs/)

ER diyagramları: [`docs/microservice-er-diagrams/`](../docs/microservice-er-diagrams/)

---

## Mimari Özet

### Database-per-Service Pattern
Her mikroservis **kendi PostgreSQL veritabanına** sahiptir. Servisler arası referanslar `FOREIGN KEY` ile değil, **UUID/business key** ile tutulur. Tutarlılık `Outbox + Inbox (Processed Event)` pattern ile event-driven sağlanır.

| Servis | Port | Database |
|---|---|---|
| `api-gateway` | 8080 | — |
| `discovery-server` (Eureka) | 8761 | — |
| `config-server` | 8888 | — |
| `identity-service` | 9001 | `identity_db` |
| `customer-service` | 9002 | `customer_db` |
| `product-catalog-service` | 9003 | `product_db` |
| `order-service` | 9004 | `order_db` |
| `subscription-service` | 9005 | `subscription_db` |
| `usage-service` | 9006 | `usage_db` |
| `billing-service` | 9007 | `billing_db` |
| `payment-service` | 9008 | `payment_db` |
| `notification-service` | 9009 | `notification_db` |
| `ticket-service` | 9010 | `ticket_db` |

### Multi-Module Maven Yapısı
```
telco-crm-platform/                  (parent POM)
├── telco-common/                    (paylaşılan kütüphane: DTO, exception, event)
├── infrastructure/
│   ├── discovery-server/            (Eureka)
│   ├── config-server/               (Spring Cloud Config - native, classpath)
│   └── api-gateway/                 (Spring Cloud Gateway)
└── services/
    ├── identity-service/
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
| Framework | Spring Boot | 3.3.5 |
| Spring Cloud | Gateway, Config, Eureka, OpenFeign | 2023.0.3 |
| Build | Maven Multi-Module | 3.9+ |
| Database | PostgreSQL | 16 |
| Cache / Idempotency | Redis | 7 |
| Broker | Apache Kafka (KRaft) | 7.7.1 |
| Migration | Flyway | (BOM) |
| ORM | Spring Data JPA + Hibernate | (BOM) |
| Mapping | MapStruct | 1.6.3 |
| Auth | Spring Security + JWT (jjwt) | 0.12.6 |
| Docs | Springdoc OpenAPI | 2.6.0 |
| Resilience | Resilience4j | 2.2.0 |
| Observability | Micrometer + Zipkin + OpenTelemetry | (BOM) |
| Test | JUnit 5, Mockito, Testcontainers | 1.20.4 |

---

## Hızlı Başlangıç

### 1. Altyapıyı Ayağa Kaldır

```bash
cd telco-crm-platform
docker compose up -d
```

Bu komut şunları başlatır:
- **PostgreSQL** (5432) — 10 servis için ayrı veritabanları otomatik oluşturulur
- **Redis** (6379)
- **Kafka** (9092) — KRaft mode, Zookeepersız
- **Kafka UI** (http://localhost:8090)
- **Zipkin** (http://localhost:9411)
- **MailHog** (http://localhost:8025) — SMTP test arayüzü
- **pgAdmin** (http://localhost:5050) — `admin@telcox.local` / `admin`

### 2. Tüm Modülleri Derle (ZORUNLU İLK ADIM)

> **Önemli:** Bu adım `telco-common` shared kütüphanesini yerel Maven cache'ine (`~/.m2/repository`) yükler. Bu yapılmadan tek bir servis başlatmayı denersen `Could not find artifact com.telcox:telco-common` hatası alırsın.

Proje **root dizininden** (parent POM olan yerden) çalıştır:

```bash
cd /path/to/telco-crm-platform     # parent pom.xml'in olduğu dizin
mvn clean install -DskipTests
```

Bir kere yaptıktan sonra her servisi bağımsız çalıştırabilirsin. `telco-common`'da değişiklik yaparsan tekrar `install` etmen gerekir.

### 3. Servisleri Sırayla Başlat

Aşağıdaki sırayla 3 ayrı terminalde başlat:

```bash
# Terminal 1 — Discovery (Eureka)
cd infrastructure/discovery-server
mvn spring-boot:run

# Terminal 2 — Config Server
cd infrastructure/config-server
mvn spring-boot:run

# Terminal 3 — API Gateway
cd infrastructure/api-gateway
mvn spring-boot:run
```

Sonra herhangi bir business servisi başlat:

```bash
cd services/identity-service
mvn spring-boot:run
```

> **Alternatif (tek komut):** Root dizinden ayrılmadan, otomatik bağımlılık derlemesiyle:
> ```bash
> mvn spring-boot:run -pl services/identity-service -am
> ```
> `-pl` (projects list) sadece o modülü çalıştırır, `-am` (also-make) bağımlı modülleri de build eder.

### 4. Servisleri Doğrula
- Eureka dashboard: http://localhost:8761
- Gateway routes: http://localhost:8080/actuator/gateway/routes
- Identity Swagger: http://localhost:9001/swagger-ui.html

---

## Konfigürasyon Hiyerarşisi

1. **`infrastructure/config-server/src/main/resources/config/application.yml`** — Tüm servislerin paylaştığı ortak ayarlar (logging, actuator, resilience4j vb.)
2. **`services/<svc>/src/main/resources/application.yml`** — Servise özel ayarlar (port, DB adı, Kafka group-id)
3. **Environment variable override** — `DB_HOST`, `KAFKA_BOOTSTRAP`, `JWT_SECRET` vb.

---

## Servis İletişim Modeli

| Senaryo | Tip | Teknoloji |
|---|---|---|
| Gateway → Servis | Senkron | Spring Cloud Gateway + Eureka (`lb://`) |
| Servis → Servis (data fetch) | Senkron | OpenFeign + Resilience4j |
| Servis → Servis (event) | Asenkron | Kafka + Outbox pattern |
| CDR → Usage Service | Asenkron | Kafka |

---

## Geliştirme Notları

### `ddl-auto: validate`
Her servis Flyway ile schema yönetir. Hibernate sadece **validate** eder, asla tablo oluşturmaz. Yeni tablo eklemek için `services/<svc>/src/main/resources/db/migration/V{N}__<desc>.sql` ekle.

### Outbox Pattern
Her domain değişikliğiyle aynı transaction içinde `*_OUTBOX_EVENT` tablosuna kayıt atılır. Arka planda scheduler bu tabloyu Kafka'ya publish eder. Bkz. analiz dokümanı §9.3.

### Idempotent Consumer
Kafka'dan tüketilen her event'in `eventId`'si `*_PROCESSED_EVENT.event_id` UNIQUE constraint ile kontrol edilir; aynı event tekrar gelirse atlanır.

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
- [ ] Outbox publisher worker (TimerTask / @Scheduled)
- [ ] Kafka producer/consumer + JSON serializer config
- [ ] JWT filter `api-gateway`'de (relay → `X-User-Id`, `X-User-Roles`)
- [ ] Testcontainers ile integration test setup
- [ ] Docker image'lar ve `docker-compose.full.yml`
- [ ] CI/CD pipeline (GitHub Actions)

---

## Lisans
Eğitim projesi. Üretim sistemlerinde kullanılmadan önce güvenlik, performans ve uyumluluk gözden geçirilmelidir.
