# Architecture Decision Records (ADR)

Bu klasör TelcoX mimari kararlarını tutar. Her karar numaralı, değişmez bir kayıttır;
bir karar değişirse yeni ADR yazılır ve eskisi "Superseded" işaretlenir.

| ID | Başlık | Durum | Epic/Task |
|----|--------|-------|-----------|
| [ADR-0003](./ADR-0003-async-messaging.md) | Asenkron iletişim standardı (event/command/reply-projection) | Proposed | ARCH-03 |
| [ADR-0004](./ADR-0004-redis-usage-boundaries.md) | Redis kullanım sınırları | Proposed | ARCH-04 |
| [ADR-0005](./ADR-0005-projections-read-models.md) | Servis bazlı projection/read-model sorumlulukları | Proposed | ARCH-05 |

> Henüz yazılmayanlar (başka task sahipleri): ADR-0001 Boot/Cloud versiyon kararı (ARCH-01),
> ADR-0002 Feign çıkışı (ARCH-02). Bu ikisi yazılınca INF-01 ve SEC-03 finalize edilebilir.

**Durum değerleri:** Proposed → Accepted → (Deprecated | Superseded)
