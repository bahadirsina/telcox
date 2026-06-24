# TelcoX — Atanan Task'ların Uygulama Notları (iskelet teslimi)

Bu dosya, üretilen iskelet dosyaların nasıl birbirine bağlandığını, nasıl çalıştırılacağını
ve **karar bekleyen** noktaları özetler. Task sahibi: _(senin adın)_.

## Üretilen dosyalar

| Task | Dosya(lar) | Tür |
|---|---|---|
| ARCH-03 | `docs/adr/ADR-0003-async-messaging.md` | Yeni |
| ARCH-04 | `docs/adr/ADR-0004-redis-usage-boundaries.md` | Yeni |
| ARCH-05 | `docs/adr/ADR-0005-projections-read-models.md` | Yeni |
| (index) | `docs/adr/README.md` | Yeni |
| INF-01 | `pom.xml` (root) | Düzenlendi |
| INF-04 | `.../gateway/config/CorsConfig.java`, `.../config/RateLimitConfig.java`, `.../filter/CorrelationIdFilter.java`, `application.yaml` | Yeni + düzenleme |
| SEC-03 | `.../gateway/config/SecurityConfig.java`, `api-gateway/pom.xml`, `application.yaml` | Yeni + düzenleme |
| SEC-04* | `.../gateway/filter/UserContextRelayFilter.java` | Yeni (*komşu task, bkz. aşağı) |
| SEC-01 | `docker-compose.yml`, `.env.example` | Yeni + düzenleme |
| SEC-02 | `infrastructure/keycloak/realm-export/telcox-realm.json` | Yeni |

## Çalıştırma sırası (uçtan uca doğrulama)

```bash
# 1) Ortam değişkenleri
cp .env.example .env                     # değerleri kontrol et

# 2) Keycloak + KC-Postgres'i ayağa kaldır (realm otomatik import)
docker compose up -d keycloak-postgres keycloak

# 3) Realm import oldu mu?  ->  http://localhost:18083  (admin/admin)
#    Realm 'telcox' altında ADMIN/AGENT/CUSTOMER rolleri ve test user'lar görünmeli.

# 4) Token al (direct grant ile hızlı test):
curl -s -X POST http://localhost:18083/realms/telcox/protocol/openid-connect/token \
  -d grant_type=password -d client_id=telcox-frontend \
  -d username=admin -d password=admin123 | jq .access_token

# 5) Gateway'i çalıştır ve korunan route'u dene:
mvn -pl infrastructure/api-gateway spring-boot:run
#    Token'sız  -> 401, Token'lı -> downstream'e X-User-Id/X-User-Roles ile geçer.
```

## Karar bekleyen noktalar (BLOCKER — başka task sahipleriyle netleştir)

1. **ARCH-01 (Boot sürümü):** Repo Boot 4.0.6, task Boot 3.5.x öneriyor. INF-01 pom'u her iki
   hatta hazır ama **karar gelmeden finalize edilmemeli**. Boot 3.5'e dönülürse
   `resilience4j-spring-boot4 -> 3`, Cloud `2025.0.x`, springdoc `2.6.x`.

2. **SEC-05 / token issuer:** Şu an **identity-service kendi JWT'sini üretiyor** (jjwt).
   SEC-03 ile gateway artık **Keycloak imzalı** token bekliyor. İkisi aynı anda doğru olamaz.
   Netleştirilecek: Keycloak tek IdP mı olacak (identity-service token üretmeyi bırakır,
   yalnızca user/profile/audit adapter olur — SEC-05), yoksa geçiş dönemi mi? Bu karar
   verilmeden login akışı uçtan uca çalışmaz.

3. **SEC-04 (header relay):** `UserContextRelayFilter` iskeleti gateway'e eklendi çünkü
   correlation-id (INF-04) ile aynı zincirde. Eğer SEC-04 başka birindeyse, o kişiyle
   sahipliği netleştir; çift implementasyon olmasın.

4. **INF-02 (compose):** Keycloak, Kafka Connect ve Debezium servisleri ana
   `docker-compose.yml` içinde birlikte yönetilir.

## Gateway namespace notu
Mevcut `application.yaml`, route'ları `spring.cloud.gateway.routes` altında tutuyor; aynen
korundu, yalnızca `default-filters` + güvenlik eklendi. Build sırasında route'lar yüklenmezse
Spring Cloud 2025.x `gateway-server-webflux` için prefix'in `spring.cloud.gateway.server.webflux`
olup olmadığını kontrol et (sürüme göre değişebilir).
