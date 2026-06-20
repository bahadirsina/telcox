# Idempotent Kafka Consumer Standard

## Amaç

Kafka, aynı kaydı bir consumer'a birden fazla kez teslim edebilir. Bir servis bir
olayı tekrar aldığında domain değişikliğini ikinci kez uygulamamalıdır. Her servis
bu amaçla kendi `*_processed_event` tablosunu kullanır. Tablodaki `event_id`
alanının `UNIQUE` olması idempotency'nin veritabanı seviyesindeki garantisidir.

Bu standart, Debezium outbox ile yayımlanan her domain event için geçerlidir.

## Zorunlu işleme akışı

1. Consumer, mesajın benzersiz olay kimliğini (`event_id`) okur. Bu değer outbox
   eventinin `id` alanıdır; Kafka offset'i veya rastgele üretilmiş yeni bir UUID
   kullanılmaz.
2. Consumer, kendi `*_processed_event` tablosuna `event_id`, `event_type`,
   `source_service`, `aggregate_id` ve `status = PROCESSING` ile kayıt eklemeyi
   dener.
3. Ekleme `event_id` unique constraint'i nedeniyle başarısızsa olay daha önce
   alınmıştır. Mesaj başarıyla acknowledge edilir; domain işlemi tekrar
   çalıştırılmaz ve hata/retry üretilmez.
4. Kayıt eklenmişse domain değişikliği **aynı veritabanı transaction'ı içinde**
   uygulanır. Başarılı işlem sonunda kayıt `status = PROCESSED` ve
   `processed_at = CURRENT_TIMESTAMP` olacak şekilde güncellenir.
5. Transaction başarısızsa hem domain değişikliği hem de `PROCESSING` kaydı
   rollback olur. Mesaj retry politikasına bırakılır. Kalıcı olarak işlenemeyen
   olaylar için hata bilgisi `error_message` alanına yazılır ve ilgili hata/DLT
   akışı kullanılır.
6. Kafka offset'i yalnızca transaction commit edildikten sonra acknowledge
   edilir. Böylece servis çökerse aynı mesaj yeniden geldiğinde veritabanındaki
   `event_id` koruması devreye girer.

## Uygulama iskeleti

```java
@Transactional
public void consume(DomainEvent event) {
    try {
        processedEventRepository.insertProcessing(event);
    } catch (DuplicateKeyException duplicate) {
        return; // Olay daha önce commit edilmiştir; güvenle acknowledge edilir.
    }

    applyDomainChange(event);
    processedEventRepository.markProcessed(event.eventId());
}
```

`DuplicateKeyException` yalnızca `event_id` unique constraint'inden kaynaklanan
tekrar için yutulmalıdır. Başka veritabanı hataları normal hata akışına
bırakılmalıdır.

## Kabul kriterleri

- Yeni her Kafka consumer bu akışı kullanır.
- Aynı `event_id` iki kez geldiğinde domain kaydı yalnızca bir kez değişir.
- `*_processed_event.event_id` unique constraint'i kaldırılmaz veya uygulama
  katmanındaki bir kontrolle ikame edilmez.
- Consumer testi; ilk teslim, aynı event'in tekrar teslimi ve transaction
  rollback senaryolarını kapsar.

## Mevcut durum

Tüm servis başlangıç migration'larında `*_processed_event` tablosu ve benzersiz
`event_id` tanımlıdır. Henüz uygulamada `@KafkaListener` bulunmadığından bu
standart ilk consumer taskları (örneğin usage projection veya subscription
workflow) eklenirken bu sınıflara uygulanacaktır.
