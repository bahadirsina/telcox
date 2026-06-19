#!/bin/sh
set -eu

BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVER:-kafka:29092}"
REPLICATION_FACTOR="${KAFKA_REPLICATION_FACTOR:-1}"
PARTITIONS="${KAFKA_PARTITIONS:-3}"

create_topic() {
  topic="$1"
  partitions="${2:-$PARTITIONS}"
  retention_ms="${3:-604800000}"

  kafka-topics \
    --bootstrap-server "$BOOTSTRAP_SERVER" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$partitions" \
    --replication-factor "$REPLICATION_FACTOR" \
    --config retention.ms="$retention_ms"
}

create_topic telcox.connect.configs 1
create_topic telcox.connect.offsets 25
create_topic telcox.connect.statuses 5

create_topic telcox.identity.user-registered.v1
create_topic telcox.customer.customer-created.v1
create_topic telcox.product-catalog.product-changed.v1
create_topic telcox.order.order-created.v1
create_topic telcox.subscription.subscription-activated.v1
create_topic telcox.usage.usage-recorded.v1
create_topic telcox.billing.invoice-issued.v1
create_topic telcox.payment.payment-received.v1
create_topic telcox.notification.notification-requested.v1
create_topic telcox.ticket.ticket-opened.v1

for topic in \
  telcox.identity.user-registered.v1 \
  telcox.customer.customer-created.v1 \
  telcox.order.order-created.v1 \
  telcox.subscription.subscription-activated.v1 \
  telcox.billing.invoice-issued.v1 \
  telcox.payment.payment-received.v1
do
  create_topic "$topic.retry.0" "$PARTITIONS" 86400000
  create_topic "$topic.retry.1" "$PARTITIONS" 259200000
  create_topic "$topic.dlq" "$PARTITIONS" 1209600000
done
