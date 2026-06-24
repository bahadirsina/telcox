# Architecture Decision Records (ADR)

Bu klasör TelcoX mimari kararlarını tutar. Her karar numaralı, değişmez bir kayıttır;
bir karar değişirse yeni ADR yazılır ve eskisi "Superseded" işaretlenir.

| ID | Başlık | Durum | Epic/Task |
|----|--------|-------|-----------|
| [ADR-0001](./ADR-0001-spring-boot-cloud-baseline.md) | Spring Boot / Spring Cloud versiyon baseline | Accepted | ARCH-01 |
| [ADR-0002](./ADR-0002-remove-openfeign-from-core-service-communication.md) | OpenFeign'i core servis iletişiminden çıkarma | Accepted | ARCH-02 |
| [ADR-0003](./ADR-0003-async-messaging.md) | Asenkron iletişim standardı (event/command/reply-projection) | Proposed | ARCH-03 |
| [ADR-0004](./ADR-0004-redis-usage-boundaries.md) | Redis kullanım sınırları | Proposed | ARCH-04 |
| [ADR-0005](./ADR-0005-projections-read-models.md) | Servis bazlı projection/read-model sorumlulukları | Proposed | ARCH-05 |

**Durum değerleri:** Proposed → Accepted → (Deprecated | Superseded)
