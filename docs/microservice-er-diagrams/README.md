# Telco CRM - Microservice ER Diagrams

Her `.dbml` dosyasi bir Spring microservice'in kendi veritabani sinirini (database-per-service paterni) temsil eder.

## FK Politikasi (kurumsal standart)

Mikroservis mimarisinde tablolar arasi referanslar **iki farkli kapsamda** ele alinir:

| Kapsam | Kullanim | Notasyon |
|---|---|---|
| **Servis ICI** (ayni `*_db` icindeki tablolar arasi) | Fiziksel `FOREIGN KEY` constraint kullanilir; ACID + cascade + index | `ref: > tablo.id` |
| **Servis ARASI** (farkli mikroservis veritabanlari) | Fiziksel FK **YASAK**; sadece UUID/business key tutulur, tutarliik event-driven saglanir | `note: 'logical ref -> ...'` |
| **Outbox `aggregateId`** | Heterojen aggregate tipleri tutuldugu icin FK uygulanmaz | `note: 'soft ref'` |
| **Processed Event `eventId`** | Diger servisin event id'sidir; bu DB'de hedef yok | `unique` (FK yok) |

**Sebep:** Servisler arasi fiziksel FK; bagimsiz deployment, bagimsiz scaling ve hata izolasyonu prensiplerini kirar, distributed transaction (2PC) ihtiyaci dogurur. Tutarliik **Outbox + Inbox (Processed Event)** kombinasyonu ile saglanir.

## Servis Listesi

| # | Servis | Database | Port | DBML |
|---|---|---|---|---|
| 1 | identity-service | identity_db | 9001 | [identity-service.dbml](identity-service.dbml) |
| 2 | customer-service | customer_db | 9002 | [customer-service.dbml](customer-service.dbml) |
| 3 | product-catalog-service | product_db | 9003 | [product-catalog-service.dbml](product-catalog-service.dbml) |
| 4 | order-service | order_db | 9004 | [order-service.dbml](order-service.dbml) |
| 5 | subscription-service | subscription_db | 9005 | [subscription-service.dbml](subscription-service.dbml) |
| 6 | usage-service | usage_db | 9006 | [usage-service.dbml](usage-service.dbml) |
| 7 | billing-service | billing_db | 9007 | [billing-service.dbml](billing-service.dbml) |
| 8 | payment-service | payment_db | 9008 | [payment-service.dbml](payment-service.dbml) |
| 9 | notification-service | notification_db | 9009 | [notification-service.dbml](notification-service.dbml) |
| 10 | ticket-service | ticket_db | 9010 | [ticket-service.dbml](ticket-service.dbml) |
| 11 | inventory-service | inventory_db | (TBD) | [inventory-service.dbml](inventory-service.dbml) |
| 12 | campaign-service * | campaign_db | (TBD) | [campaign-service.dbml](campaign-service.dbml) |

\* Kampanya servisi MVP **scope-out** kapsaminda; sadece hazirlik amaclidir.

## Konsolide Goruntu

Tum servisleri tek bir diyagramda gormek icin: [services-er.dbml](services-er.dbml)

## Standart Tablolar (Her Servis)

Her servis asagidaki destek tablolarini icerir:

- `*_OUTBOX_EVENT` — Transactional Outbox patterni (event publish atomicity)
- `*_PROCESSED_EVENT` — Idempotent consumer / Inbox patterni
- `*_AUDIT_LOG` — `identity`, `customer`, `subscription`, `payment` servislerinde regulasyon geregi (KVKK/GDPR)

## Onerilen ER Render Aracligi

- [dbdiagram.io](https://dbdiagram.io) — DBML dosyasini import et, gorsel diyagrami otomatik olusur.
- VS Code icin: `vscode-dbml` eklentisi.
