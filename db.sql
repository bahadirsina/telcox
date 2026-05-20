CREATE SCHEMA IF NOT EXISTS "public";

CREATE TABLE "public"."ORDER_SERVICE_FULFILLMENT" (
    "id" uuid NOT NULL,
    "orderId" uuid NOT NULL,
    -- SIM_PROVISION | NUMBER_PORT | DEVICE_SHIPMENT
    "fulfillmentType" varchar(500),
    -- PENDING | IN_PROGRESS | COMPLETED | FAILED
    "status" varchar(500),
    "msisdn" varchar(500),
    "iccid" varchar(500),
    "scheduledAt" timestamp,
    "completedAt" timestamp,
    "errorMessage" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."ORDER_SERVICE_FULFILLMENT"."fulfillmentType" IS 'SIM_PROVISION | NUMBER_PORT | DEVICE_SHIPMENT';
COMMENT ON COLUMN "public"."ORDER_SERVICE_FULFILLMENT"."status" IS 'PENDING | IN_PROGRESS | COMPLETED | FAILED';

CREATE TABLE "public"."PAYMENT_SERVICE_AUDIT_LOG" (
    "id" uuid NOT NULL,
    -- logical ref -> identity-service.USER.id
    "actorUserId" uuid,
    "action" varchar(500),
    "entityType" varchar(500),
    "entityId" uuid,
    "oldValueJson" jsonb,
    "newValueJson" jsonb,
    "correlationId" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_AUDIT_LOG"."actorUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."CAMPAIGN_SERVICE_ELIGIBILITY" (
    "id" uuid NOT NULL,
    "campaignId" uuid NOT NULL,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    -- ELIGIBLE | REDEEMED | EXPIRED
    "status" varchar(500),
    "eligibleAt" timestamp,
    "redeemedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_ELIGIBILITY"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_ELIGIBILITY"."status" IS 'ELIGIBLE | REDEEMED | EXPIRED';

CREATE TABLE "public"."CAMPAIGN_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."ORDER_SERVICE_SAGA_STATE" (
    "id" uuid NOT NULL,
    "orderId" uuid NOT NULL UNIQUE,
    "currentStep" varchar(500),
    -- STARTED | COMPLETED | COMPENSATED | FAILED
    "stepStatus" varchar(500),
    "payloadJson" jsonb,
    "lastUpdatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."ORDER_SERVICE_SAGA_STATE"."stepStatus" IS 'STARTED | COMPLETED | COMPENSATED | FAILED';

CREATE TABLE "public"."INVENTORY_SERVICE_STOCK_LOCATION" (
    "id" uuid NOT NULL,
    "code" varchar(500) UNIQUE,
    "name" varchar(500),
    -- WAREHOUSE | STORE | DEALER
    "locationType" varchar(500),
    "address" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_STOCK_LOCATION"."locationType" IS 'WAREHOUSE | STORE | DEALER';

CREATE TABLE "public"."NOTIFICATION_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."CUSTOMER_SERVICE_CONSENT" (
    "id" uuid NOT NULL,
    "customerId" uuid NOT NULL,
    -- KVKK | MARKETING | DATA_SHARING
    "consentType" varchar(500),
    -- EMAIL | SMS | CALL
    "channel" varchar(500),
    "isGranted" boolean,
    "grantedAt" timestamp,
    "revokedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CONSENT"."consentType" IS 'KVKK | MARKETING | DATA_SHARING';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CONSENT"."channel" IS 'EMAIL | SMS | CALL';

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."IDENTITY_SERVICE_USER_ROLE" (
    "userId" uuid NOT NULL,
    "roleId" uuid NOT NULL,
    PRIMARY KEY ("userId", "roleId")
);

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_PLAN_HISTORY" (
    "id" uuid NOT NULL,
    "subscriptionId" uuid NOT NULL,
    -- logical ref -> product-service.PLAN.id
    "planId" uuid NOT NULL,
    "startDate" timestamp,
    "endDate" timestamp,
    "reason" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_PLAN_HISTORY"."planId" IS 'logical ref -> product-service.PLAN.id';

CREATE TABLE "public"."NOTIFICATION_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."USAGE_SERVICE_USAGE_AGGREGATE" (
    "id" uuid NOT NULL,
    -- logical ref -> subscription-service.SUBSCRIPTION.id
    "subscriptionId" uuid NOT NULL,
    "periodStart" date,
    "periodEnd" date,
    "totalVoiceSeconds" bigint,
    "totalSmsCount" bigint,
    "totalDataBytes" bigint,
    "totalCost" numeric,
    "currency" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."USAGE_SERVICE_USAGE_AGGREGATE"."subscriptionId" IS 'logical ref -> subscription-service.SUBSCRIPTION.id';

CREATE TABLE "public"."USAGE_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."USAGE_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."CUSTOMER_SERVICE_NOTE" (
    "id" uuid NOT NULL,
    "customerId" uuid NOT NULL,
    -- logical ref -> identity-service.USER.id
    "authorUserId" uuid,
    "noteText" text,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_NOTE"."authorUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."ORDER_SERVICE_STATUS_HISTORY" (
    "id" uuid NOT NULL,
    "orderId" uuid NOT NULL,
    "oldStatus" varchar(500),
    "newStatus" varchar(500),
    "changedAt" timestamp,
    -- logical ref -> identity-service.USER.id
    "changedByUserId" uuid,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."ORDER_SERVICE_STATUS_HISTORY"."changedByUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_AUDIT_LOG" (
    "id" uuid NOT NULL,
    -- logical ref -> identity-service.USER.id
    "actorUserId" uuid,
    "action" varchar(500),
    "entityType" varchar(500),
    "entityId" uuid,
    "oldValueJson" jsonb,
    "newValueJson" jsonb,
    "correlationId" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_AUDIT_LOG"."actorUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."NOTIFICATION_SERVICE_PREFERENCE" (
    "id" uuid NOT NULL,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    "channel" varchar(500),
    "isOptedIn" boolean,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_PREFERENCE"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';

CREATE TABLE "public"."IDENTITY_SERVICE_ROLE" (
    "id" uuid NOT NULL,
    "name" varchar(500) NOT NULL UNIQUE,
    "description" varchar(500),
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."PAYMENT_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."CAMPAIGN_SERVICE_OFFER" (
    "id" uuid NOT NULL,
    "campaignId" uuid NOT NULL,
    -- logical ref -> product-service.PRODUCT.id
    "productId" uuid,
    -- PERCENT | AMOUNT | FREE_ADDON
    "discountType" varchar(500),
    "discountValue" numeric,
    "durationMonths" int,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_OFFER"."productId" IS 'logical ref -> product-service.PRODUCT.id';
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_OFFER"."discountType" IS 'PERCENT | AMOUNT | FREE_ADDON';

CREATE TABLE "public"."PAYMENT_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."INVENTORY_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."USAGE_SERVICE_USAGE_RECORD" (
    "id" uuid NOT NULL,
    -- logical ref -> subscription-service.SUBSCRIPTION.id
    "subscriptionId" uuid NOT NULL,
    "msisdn" varchar(500),
    -- VOICE | SMS | DATA
    "recordType" varchar(500),
    "startTime" timestamp,
    "endTime" timestamp,
    "durationSeconds" int,
    "dataVolumeBytes" bigint,
    "destinationNumber" varchar(500),
    "location" varchar(500),
    "cost" numeric,
    "currency" varchar(500),
    "isBilled" boolean,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."USAGE_SERVICE_USAGE_RECORD"."subscriptionId" IS 'logical ref -> subscription-service.SUBSCRIPTION.id';
COMMENT ON COLUMN "public"."USAGE_SERVICE_USAGE_RECORD"."recordType" IS 'VOICE | SMS | DATA';

CREATE TABLE "public"."CUSTOMER_SERVICE_AUDIT_LOG" (
    "id" uuid NOT NULL,
    -- logical ref -> identity-service.USER.id
    "actorUserId" uuid,
    "action" varchar(500),
    "entityType" varchar(500),
    "entityId" uuid,
    "oldValueJson" jsonb,
    "newValueJson" jsonb,
    "correlationId" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_AUDIT_LOG"."actorUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."INVENTORY_SERVICE_STOCK_ITEM" (
    "id" uuid NOT NULL,
    "locationId" uuid NOT NULL,
    -- SIM | DEVICE
    "itemType" varchar(500),
    -- soft ref - SIM_CARD.id veya DEVICE.id (heterojen)
    "itemRefId" uuid,
    "quantity" int,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_STOCK_ITEM"."itemType" IS 'SIM | DEVICE';
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_STOCK_ITEM"."itemRefId" IS 'soft ref - SIM_CARD.id veya DEVICE.id (heterojen)';

CREATE TABLE "public"."INVENTORY_SERVICE_MSISDN" (
    "id" uuid NOT NULL,
    "msisdn" varchar(500) UNIQUE,
    -- REGULAR | GOLD | PLATINUM
    "msisdnType" varchar(500),
    -- AVAILABLE | RESERVED | ACTIVE | QUARANTINED
    "status" varchar(500),
    -- logical ref -> subscription-service.SUBSCRIPTION.id
    "assignedSubscriptionId" uuid,
    "reservedUntil" timestamp,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_MSISDN"."msisdnType" IS 'REGULAR | GOLD | PLATINUM';
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_MSISDN"."status" IS 'AVAILABLE | RESERVED | ACTIVE | QUARANTINED';
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_MSISDN"."assignedSubscriptionId" IS 'logical ref -> subscription-service.SUBSCRIPTION.id';

CREATE TABLE "public"."IDENTITY_SERVICE_AUDIT_LOG" (
    "id" uuid NOT NULL,
    "actorUserId" uuid,
    "action" varchar(500),
    "entityType" varchar(500),
    "entityId" uuid,
    "oldValueJson" jsonb,
    "newValueJson" jsonb,
    "correlationId" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."CUSTOMER_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    -- PENDING | PUBLISHED | FAILED
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_OUTBOX_EVENT"."status" IS 'PENDING | PUBLISHED | FAILED';

CREATE TABLE "public"."PAYMENT_SERVICE_PAYMENT_METHOD" (
    "id" uuid NOT NULL,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    -- CARD | BANK_ACCOUNT | AUTO_DEBIT | WALLET
    "methodType" varchar(500),
    "providerToken" varchar(500),
    "maskedAccount" varchar(500),
    "expiryMonth" int,
    "expiryYear" int,
    "isDefault" boolean,
    -- ACTIVE | EXPIRED | REVOKED
    "status" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_PAYMENT_METHOD"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_PAYMENT_METHOD"."methodType" IS 'CARD | BANK_ACCOUNT | AUTO_DEBIT | WALLET';
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_PAYMENT_METHOD"."status" IS 'ACTIVE | EXPIRED | REVOKED';

CREATE TABLE "public"."PAYMENT_SERVICE_REFUND" (
    "id" uuid NOT NULL,
    "paymentId" uuid NOT NULL,
    "amount" numeric,
    "reason" varchar(500),
    -- PENDING | COMPLETED | FAILED
    "status" varchar(500),
    "requestedAt" timestamp,
    "completedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_REFUND"."status" IS 'PENDING | COMPLETED | FAILED';

CREATE TABLE "public"."PRODUCT_SERVICE_PRICE" (
    "id" uuid NOT NULL,
    "productId" uuid NOT NULL,
    "price" numeric,
    "currency" varchar(500),
    "taxIncluded" boolean,
    "validFrom" date,
    "validTo" date,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."ORDER_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."ORDER_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."BILLING_SERVICE_TAX_RATE" (
    "id" uuid NOT NULL,
    "code" varchar(500) UNIQUE,
    "name" varchar(500),
    "percentage" numeric,
    "validFrom" date,
    "validTo" date,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."BILLING_SERVICE_INVOICE_ITEM" (
    "id" uuid NOT NULL,
    "invoiceId" uuid NOT NULL,
    "productCode" varchar(500),
    "description" varchar(500),
    "quantity" numeric,
    "unitPrice" numeric,
    "taxRate" numeric,
    "totalPrice" numeric,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."IDENTITY_SERVICE_ROLE_PERMISSION" (
    "roleId" uuid NOT NULL,
    "permissionId" uuid NOT NULL,
    PRIMARY KEY ("roleId", "permissionId")
);

CREATE TABLE "public"."PAYMENT_SERVICE_TRANSACTION" (
    "id" uuid NOT NULL,
    "paymentId" uuid NOT NULL,
    -- AUTHORIZE | CAPTURE | REFUND | CHARGEBACK
    "transactionType" varchar(500),
    "providerTxnId" varchar(500),
    "amount" numeric,
    "status" varchar(500),
    "processedAt" timestamp,
    "rawResponseJson" jsonb,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_TRANSACTION"."transactionType" IS 'AUTHORIZE | CAPTURE | REFUND | CHARGEBACK';

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION" (
    "id" uuid NOT NULL,
    "subscriptionNumber" varchar(500) UNIQUE,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    -- logical ref -> product-service.PLAN.id
    "planId" uuid NOT NULL,
    -- logical ref -> inventory-service.MSISDN.msisdn
    "msisdn" varchar(500),
    -- logical ref -> inventory-service.SIM_CARD.iccid
    "iccid" varchar(500),
    -- PENDING | ACTIVE | SUSPENDED | CANCELLED | TERMINATED
    "status" varchar(500),
    "billingDay" int,
    "activatedAt" timestamp,
    "deactivatedAt" timestamp,
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"."planId" IS 'logical ref -> product-service.PLAN.id';
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"."msisdn" IS 'logical ref -> inventory-service.MSISDN.msisdn';
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"."iccid" IS 'logical ref -> inventory-service.SIM_CARD.iccid';
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"."status" IS 'PENDING | ACTIVE | SUSPENDED | CANCELLED | TERMINATED';

CREATE TABLE "public"."CUSTOMER_SERVICE_ADDRESS" (
    "id" uuid NOT NULL,
    "customerId" uuid NOT NULL,
    -- BILLING | SHIPPING | CONTACT
    "addressType" varchar(500),
    "country" varchar(500),
    "city" varchar(500),
    "district" varchar(500),
    "street" varchar(500),
    "buildingNo" varchar(500),
    "postalCode" varchar(500),
    "isDefault" boolean,
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_ADDRESS"."addressType" IS 'BILLING | SHIPPING | CONTACT';

CREATE TABLE "public"."INVENTORY_SERVICE_SIM_CARD" (
    "id" uuid NOT NULL,
    "iccid" varchar(500) UNIQUE,
    "imsi" varchar(500) UNIQUE,
    -- PHYSICAL | ESIM
    "simType" varchar(500),
    -- AVAILABLE | RESERVED | ACTIVE | DEACTIVATED
    "status" varchar(500),
    -- logical ref -> subscription-service.SUBSCRIPTION.id
    "assignedSubscriptionId" uuid,
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_SIM_CARD"."simType" IS 'PHYSICAL | ESIM';
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_SIM_CARD"."status" IS 'AVAILABLE | RESERVED | ACTIVE | DEACTIVATED';
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_SIM_CARD"."assignedSubscriptionId" IS 'logical ref -> subscription-service.SUBSCRIPTION.id';

CREATE TABLE "public"."CUSTOMER_SERVICE_CONTACT" (
    "id" uuid NOT NULL,
    "customerId" uuid NOT NULL,
    -- EMAIL | PHONE | MOBILE
    "contactType" varchar(500),
    "contactValue" varchar(500),
    "isVerified" boolean,
    "isPrimary" boolean,
    "verifiedAt" timestamp,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CONTACT"."contactType" IS 'EMAIL | PHONE | MOBILE';

CREATE TABLE "public"."TICKET_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."TICKET_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."PRODUCT_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PRODUCT_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."USAGE_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."BILLING_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."IDENTITY_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    -- PENDING | PUBLISHED | FAILED
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."IDENTITY_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';
COMMENT ON COLUMN "public"."IDENTITY_SERVICE_OUTBOX_EVENT"."status" IS 'PENDING | PUBLISHED | FAILED';

CREATE TABLE "public"."INVENTORY_SERVICE_DEVICE" (
    "id" uuid NOT NULL,
    "imei" varchar(500) UNIQUE,
    "brand" varchar(500),
    "model" varchar(500),
    -- IN_STOCK | SOLD | RETURNED | DEFECTIVE
    "status" varchar(500),
    -- logical ref -> customer-service.CUSTOMER.id
    "assignedCustomerId" uuid,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_DEVICE"."status" IS 'IN_STOCK | SOLD | RETURNED | DEFECTIVE';
COMMENT ON COLUMN "public"."INVENTORY_SERVICE_DEVICE"."assignedCustomerId" IS 'logical ref -> customer-service.CUSTOMER.id';

CREATE TABLE "public"."CUSTOMER_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    -- PROCESSED | FAILED
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_PROCESSED_EVENT"."status" IS 'PROCESSED | FAILED';

CREATE TABLE "public"."IDENTITY_SERVICE_REFRESH_TOKEN" (
    "id" uuid NOT NULL,
    "userId" uuid NOT NULL,
    "tokenHash" varchar(500) NOT NULL,
    "expiresAt" timestamp,
    "revokedAt" timestamp,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_STATUS_HISTORY" (
    "id" uuid NOT NULL,
    "subscriptionId" uuid NOT NULL,
    "oldStatus" varchar(500),
    "newStatus" varchar(500),
    "reason" varchar(500),
    "changedAt" timestamp,
    -- logical ref -> identity-service.USER.id
    "changedByUserId" uuid,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_STATUS_HISTORY"."changedByUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."IDENTITY_SERVICE_USER" (
    "id" uuid NOT NULL,
    "email" varchar(500) NOT NULL UNIQUE,
    "passwordHash" varchar(500) NOT NULL,
    -- ACTIVE | LOCKED | DISABLED
    "status" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."IDENTITY_SERVICE_USER"."status" IS 'ACTIVE | LOCKED | DISABLED';

CREATE TABLE "public"."NOTIFICATION_SERVICE_TEMPLATE" (
    "id" uuid NOT NULL,
    "code" varchar(500) UNIQUE,
    -- EMAIL | SMS | PUSH
    "channel" varchar(500),
    "language" varchar(500),
    "subject" varchar(500),
    "bodyTemplate" text,
    -- DRAFT | ACTIVE | RETIRED
    "status" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_TEMPLATE"."channel" IS 'EMAIL | SMS | PUSH';
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_TEMPLATE"."status" IS 'DRAFT | ACTIVE | RETIRED';

CREATE TABLE "public"."PRODUCT_SERVICE_PLAN" (
    "id" uuid NOT NULL,
    -- one-to-one alt-tip extension of PRODUCT
    "productId" uuid NOT NULL UNIQUE,
    -- PREPAID | POSTPAID | HYBRID
    "planType" varchar(500),
    "commitmentMonths" int,
    "monthlyPrice" numeric,
    "currency" varchar(500),
    "validFrom" date,
    "validTo" date,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PRODUCT_SERVICE_PLAN"."productId" IS 'one-to-one alt-tip extension of PRODUCT';
COMMENT ON COLUMN "public"."PRODUCT_SERVICE_PLAN"."planType" IS 'PREPAID | POSTPAID | HYBRID';

CREATE TABLE "public"."PRODUCT_SERVICE_PRODUCT" (
    "id" uuid NOT NULL,
    "code" varchar(500) UNIQUE,
    "name" varchar(500),
    "description" text,
    -- PLAN | ADDON | DEVICE | SERVICE
    "productType" varchar(500),
    -- DRAFT | ACTIVE | RETIRED
    "status" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PRODUCT_SERVICE_PRODUCT"."productType" IS 'PLAN | ADDON | DEVICE | SERVICE';
COMMENT ON COLUMN "public"."PRODUCT_SERVICE_PRODUCT"."status" IS 'DRAFT | ACTIVE | RETIRED';

CREATE TABLE "public"."TICKET_SERVICE_STATUS_HISTORY" (
    "id" uuid NOT NULL,
    "ticketId" uuid NOT NULL,
    "oldStatus" varchar(500),
    "newStatus" varchar(500),
    "changedAt" timestamp,
    -- logical ref -> identity-service.USER.id
    "changedByUserId" uuid,
    "comment" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."TICKET_SERVICE_STATUS_HISTORY"."changedByUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."ORDER_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."TICKET_SERVICE_TICKET" (
    "id" uuid NOT NULL,
    "ticketNumber" varchar(500) UNIQUE,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    "subject" varchar(500),
    "description" text,
    -- BILLING | TECHNICAL | SALES | COMPLAINT
    "ticketType" varchar(500),
    -- LOW | MEDIUM | HIGH | CRITICAL
    "priority" varchar(500),
    -- OPEN | IN_PROGRESS | WAITING_CUSTOMER | RESOLVED | CLOSED
    "status" varchar(500),
    "slaDueAt" timestamp,
    -- logical ref -> identity-service.USER.id
    "assignedToUserId" uuid,
    -- logical ref -> identity-service.USER.id
    "createdByUserId" uuid,
    "createdAt" timestamp,
    "resolvedAt" timestamp,
    "closedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."TICKET_SERVICE_TICKET"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."TICKET_SERVICE_TICKET"."ticketType" IS 'BILLING | TECHNICAL | SALES | COMPLAINT';
COMMENT ON COLUMN "public"."TICKET_SERVICE_TICKET"."priority" IS 'LOW | MEDIUM | HIGH | CRITICAL';
COMMENT ON COLUMN "public"."TICKET_SERVICE_TICKET"."status" IS 'OPEN | IN_PROGRESS | WAITING_CUSTOMER | RESOLVED | CLOSED';
COMMENT ON COLUMN "public"."TICKET_SERVICE_TICKET"."assignedToUserId" IS 'logical ref -> identity-service.USER.id';
COMMENT ON COLUMN "public"."TICKET_SERVICE_TICKET"."createdByUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."ORDER_SERVICE_ORDER_ITEM" (
    "id" uuid NOT NULL,
    "orderId" uuid NOT NULL,
    -- logical ref -> product-service.PRODUCT.id
    "productId" uuid NOT NULL,
    "quantity" int,
    "unitPrice" numeric,
    "totalPrice" numeric,
    "metadataJson" jsonb,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."ORDER_SERVICE_ORDER_ITEM"."productId" IS 'logical ref -> product-service.PRODUCT.id';

CREATE TABLE "public"."IDENTITY_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid NOT NULL UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    -- PROCESSED | FAILED
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."IDENTITY_SERVICE_PROCESSED_EVENT"."status" IS 'PROCESSED | FAILED';

CREATE TABLE "public"."TICKET_SERVICE_COMMENT" (
    "id" uuid NOT NULL,
    "ticketId" uuid NOT NULL,
    -- logical ref -> identity-service.USER.id
    "authorUserId" uuid,
    "commentText" text,
    "isInternal" boolean,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."TICKET_SERVICE_COMMENT"."authorUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."INVENTORY_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."PRODUCT_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."BILLING_SERVICE_INVOICE" (
    "id" uuid NOT NULL,
    "invoiceNumber" varchar(500) UNIQUE,
    "billingAccountId" uuid NOT NULL,
    "billingCycleId" uuid,
    -- logical ref -> subscription-service.SUBSCRIPTION.id
    "subscriptionId" uuid,
    "periodStart" date,
    "periodEnd" date,
    "subtotal" numeric,
    "taxAmount" numeric,
    "totalAmount" numeric,
    "currency" varchar(500),
    -- DRAFT | ISSUED | PARTIALLY_PAID | PAID | OVERDUE | CANCELLED
    "status" varchar(500),
    "issuedAt" timestamp,
    "dueDate" date,
    "paidAt" timestamp,
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."BILLING_SERVICE_INVOICE"."subscriptionId" IS 'logical ref -> subscription-service.SUBSCRIPTION.id';
COMMENT ON COLUMN "public"."BILLING_SERVICE_INVOICE"."status" IS 'DRAFT | ISSUED | PARTIALLY_PAID | PAID | OVERDUE | CANCELLED';

CREATE TABLE "public"."CAMPAIGN_SERVICE_CAMPAIGN" (
    "id" uuid NOT NULL,
    "code" varchar(500) UNIQUE,
    "name" varchar(500),
    "description" text,
    -- PROMOTION | DISCOUNT | LOYALTY | RETENTION
    "campaignType" varchar(500),
    "targetSegment" varchar(500),
    "startDate" timestamp,
    "endDate" timestamp,
    -- DRAFT | ACTIVE | PAUSED | COMPLETED
    "status" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_CAMPAIGN"."campaignType" IS 'PROMOTION | DISCOUNT | LOYALTY | RETENTION';
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_CAMPAIGN"."status" IS 'DRAFT | ACTIVE | PAUSED | COMPLETED';

CREATE TABLE "public"."ORDER_SERVICE_ORDER" (
    "id" uuid NOT NULL,
    "orderNumber" varchar(500) UNIQUE,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    -- NEW | UPGRADE | PORT_IN | ADDON | DEVICE_SALE
    "orderType" varchar(500),
    -- CREATED | CONFIRMED | IN_FULFILLMENT | COMPLETED | CANCELLED
    "status" varchar(500),
    "totalAmount" numeric,
    "currency" varchar(500),
    -- WEB | STORE | CALL_CENTER | DEALER
    "channel" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."ORDER_SERVICE_ORDER"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."ORDER_SERVICE_ORDER"."orderType" IS 'NEW | UPGRADE | PORT_IN | ADDON | DEVICE_SALE';
COMMENT ON COLUMN "public"."ORDER_SERVICE_ORDER"."status" IS 'CREATED | CONFIRMED | IN_FULFILLMENT | COMPLETED | CANCELLED';
COMMENT ON COLUMN "public"."ORDER_SERVICE_ORDER"."channel" IS 'WEB | STORE | CALL_CENTER | DEALER';

CREATE TABLE "public"."TICKET_SERVICE_ATTACHMENT" (
    "id" uuid NOT NULL,
    "ticketId" uuid NOT NULL,
    "fileName" varchar(500),
    "fileUrl" varchar(500),
    "fileSize" bigint,
    -- logical ref -> identity-service.USER.id
    "uploadedByUserId" uuid,
    "uploadedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."TICKET_SERVICE_ATTACHMENT"."uploadedByUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."BILLING_SERVICE_BILLING_ACCOUNT" (
    "id" uuid NOT NULL,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL UNIQUE,
    "currency" varchar(500),
    "balance" numeric,
    "creditLimit" numeric,
    -- ACTIVE | SUSPENDED | CLOSED
    "status" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."BILLING_SERVICE_BILLING_ACCOUNT"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."BILLING_SERVICE_BILLING_ACCOUNT"."status" IS 'ACTIVE | SUSPENDED | CLOSED';

CREATE TABLE "public"."PRODUCT_SERVICE_PLAN_FEATURE" (
    "id" uuid NOT NULL,
    "planId" uuid NOT NULL,
    -- VOICE_MIN | SMS_COUNT | DATA_MB | INTL_MIN
    "featureType" varchar(500),
    "allowance" numeric,
    "unit" varchar(500),
    "isUnlimited" boolean,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PRODUCT_SERVICE_PLAN_FEATURE"."featureType" IS 'VOICE_MIN | SMS_COUNT | DATA_MB | INTL_MIN';

CREATE TABLE "public"."PAYMENT_SERVICE_PAYMENT" (
    "id" uuid NOT NULL,
    "paymentReference" varchar(500) UNIQUE,
    -- logical ref -> customer-service.CUSTOMER.id
    "customerId" uuid NOT NULL,
    -- logical ref -> billing-service.INVOICE.id
    "invoiceId" uuid,
    "paymentMethodId" uuid,
    "amount" numeric,
    "currency" varchar(500),
    -- PENDING | AUTHORIZED | CAPTURED | FAILED | REFUNDED
    "status" varchar(500),
    "initiatedAt" timestamp,
    "completedAt" timestamp,
    "failureReason" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_PAYMENT"."customerId" IS 'logical ref -> customer-service.CUSTOMER.id';
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_PAYMENT"."invoiceId" IS 'logical ref -> billing-service.INVOICE.id';
COMMENT ON COLUMN "public"."PAYMENT_SERVICE_PAYMENT"."status" IS 'PENDING | AUTHORIZED | CAPTURED | FAILED | REFUNDED';

CREATE TABLE "public"."NOTIFICATION_SERVICE_NOTIFICATION" (
    "id" uuid NOT NULL,
    -- CUSTOMER | USER
    "recipientType" varchar(500),
    -- logical ref -> customer-service.CUSTOMER.id veya identity-service.USER.id
    "recipientId" uuid,
    -- email/phone resolved at send-time
    "recipientAddress" varchar(500),
    "channel" varchar(500),
    -- logical ref -> NOTIFICATION_TEMPLATE.code (esnek baglanti)
    "templateCode" varchar(500),
    "subject" varchar(500),
    "body" text,
    -- PENDING | SENT | DELIVERED | FAILED
    "status" varchar(500),
    "providerMessageId" varchar(500),
    "sentAt" timestamp,
    "deliveredAt" timestamp,
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_NOTIFICATION"."recipientType" IS 'CUSTOMER | USER';
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_NOTIFICATION"."recipientId" IS 'logical ref -> customer-service.CUSTOMER.id veya identity-service.USER.id';
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_NOTIFICATION"."recipientAddress" IS 'email/phone resolved at send-time';
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_NOTIFICATION"."templateCode" IS 'logical ref -> NOTIFICATION_TEMPLATE.code (esnek baglanti)';
COMMENT ON COLUMN "public"."NOTIFICATION_SERVICE_NOTIFICATION"."status" IS 'PENDING | SENT | DELIVERED | FAILED';

CREATE TABLE "public"."PRODUCT_SERVICE_CATEGORY" (
    "id" uuid NOT NULL,
    "code" varchar(500) UNIQUE,
    "name" varchar(500),
    "parentCategoryId" uuid,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."CAMPAIGN_SERVICE_RULE" (
    "id" uuid NOT NULL,
    "campaignId" uuid NOT NULL,
    -- MIN_USAGE | MIN_TENURE | SEGMENT_MATCH
    "ruleType" varchar(500),
    "ruleJson" jsonb,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_RULE"."ruleType" IS 'MIN_USAGE | MIN_TENURE | SEGMENT_MATCH';

CREATE TABLE "public"."TICKET_SERVICE_PROCESSED_EVENT" (
    "id" uuid NOT NULL,
    "eventId" uuid UNIQUE,
    "eventType" varchar(500),
    "sourceService" varchar(500),
    "aggregateId" uuid,
    "processedAt" timestamp,
    "status" varchar(500),
    "errorMessage" varchar(500),
    "createdAt" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."CUSTOMER_SERVICE_CUSTOMER" (
    "id" uuid NOT NULL,
    -- logical ref -> identity-service.USER.id
    "identityUserId" uuid UNIQUE,
    "customerNumber" varchar(500) UNIQUE,
    -- INDIVIDUAL | CORPORATE
    "customerType" varchar(500),
    "firstName" varchar(500),
    "lastName" varchar(500),
    "companyName" varchar(500),
    "taxNumber" varchar(500),
    -- Encrypted TCKN/VKN
    "nationalId" varchar(500),
    "dateOfBirth" date,
    -- MASS | SME | CORPORATE | VIP
    "segment" varchar(500),
    -- PROSPECT | ACTIVE | SUSPENDED | CLOSED
    "status" varchar(500),
    "createdAt" timestamp,
    "updatedAt" timestamp,
    -- Soft delete (KVKK/GDPR)
    "deletedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CUSTOMER"."identityUserId" IS 'logical ref -> identity-service.USER.id';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CUSTOMER"."customerType" IS 'INDIVIDUAL | CORPORATE';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CUSTOMER"."nationalId" IS 'Encrypted TCKN/VKN';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CUSTOMER"."segment" IS 'MASS | SME | CORPORATE | VIP';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CUSTOMER"."status" IS 'PROSPECT | ACTIVE | SUSPENDED | CLOSED';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_CUSTOMER"."deletedAt" IS 'Soft delete (KVKK/GDPR)';

CREATE TABLE "public"."PRODUCT_SERVICE_PRODUCT_CATEGORY" (
    "productId" uuid NOT NULL,
    "categoryId" uuid NOT NULL,
    PRIMARY KEY ("productId", "categoryId")
);

CREATE TABLE "public"."IDENTITY_SERVICE_PERMISSION" (
    "id" uuid NOT NULL,
    "code" varchar(500) NOT NULL UNIQUE,
    "description" varchar(500),
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."SUBSCRIPTION_SERVICE_ADDON" (
    "id" uuid NOT NULL,
    "subscriptionId" uuid NOT NULL,
    -- logical ref -> product-service.PRODUCT.id
    "productId" uuid NOT NULL,
    -- ACTIVE | EXPIRED | CANCELLED
    "status" varchar(500),
    "activatedAt" timestamp,
    "expiresAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_ADDON"."productId" IS 'logical ref -> product-service.PRODUCT.id';
COMMENT ON COLUMN "public"."SUBSCRIPTION_SERVICE_ADDON"."status" IS 'ACTIVE | EXPIRED | CANCELLED';

CREATE TABLE "public"."BILLING_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."BILLING_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."CAMPAIGN_SERVICE_OUTBOX_EVENT" (
    "id" uuid NOT NULL,
    -- soft ref - heterojen aggregate tipleri
    "aggregateId" uuid,
    "aggregateType" varchar(500),
    "eventType" varchar(500),
    "payloadJson" jsonb,
    "status" varchar(500),
    "createdAt" timestamp,
    "publishedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CAMPAIGN_SERVICE_OUTBOX_EVENT"."aggregateId" IS 'soft ref - heterojen aggregate tipleri';

CREATE TABLE "public"."CUSTOMER_SERVICE_DOCUMENT" (
    "id" uuid NOT NULL,
    "customerId" uuid NOT NULL,
    -- ID_CARD | PASSPORT | CONTRACT | TAX_CERT
    "documentType" varchar(500),
    "documentNumber" varchar(500),
    "fileUrl" varchar(500),
    -- PENDING | VERIFIED | REJECTED
    "verificationStatus" varchar(500),
    -- logical ref -> identity-service.USER.id
    "uploadedByUserId" uuid,
    "uploadedAt" timestamp,
    "verifiedAt" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_DOCUMENT"."documentType" IS 'ID_CARD | PASSPORT | CONTRACT | TAX_CERT';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_DOCUMENT"."verificationStatus" IS 'PENDING | VERIFIED | REJECTED';
COMMENT ON COLUMN "public"."CUSTOMER_SERVICE_DOCUMENT"."uploadedByUserId" IS 'logical ref -> identity-service.USER.id';

CREATE TABLE "public"."BILLING_SERVICE_BILLING_CYCLE" (
    "id" uuid NOT NULL,
    "billingAccountId" uuid NOT NULL,
    -- MONTHLY | QUARTERLY
    "cycleType" varchar(500),
    "periodStart" date,
    "periodEnd" date,
    -- OPEN | CLOSED | INVOICED
    "status" varchar(500),
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."BILLING_SERVICE_BILLING_CYCLE"."cycleType" IS 'MONTHLY | QUARTERLY';
COMMENT ON COLUMN "public"."BILLING_SERVICE_BILLING_CYCLE"."status" IS 'OPEN | CLOSED | INVOICED';

CREATE TABLE "public"."USAGE_SERVICE_QUOTA" (
    "id" uuid NOT NULL,
    -- logical ref -> subscription-service.SUBSCRIPTION.id
    "subscriptionId" uuid NOT NULL,
    -- VOICE_MIN | SMS_COUNT | DATA_MB
    "quotaType" varchar(500),
    "totalAllowance" numeric,
    "usedAmount" numeric,
    "remainingAmount" numeric,
    "periodStart" timestamp,
    "periodEnd" timestamp,
    PRIMARY KEY ("id")
);
COMMENT ON COLUMN "public"."USAGE_SERVICE_QUOTA"."subscriptionId" IS 'logical ref -> subscription-service.SUBSCRIPTION.id';
COMMENT ON COLUMN "public"."USAGE_SERVICE_QUOTA"."quotaType" IS 'VOICE_MIN | SMS_COUNT | DATA_MB';

-- Foreign key constraints
-- Schema: public
ALTER TABLE "public"."BILLING_SERVICE_BILLING_CYCLE" ADD CONSTRAINT "fk_BILLING_SERVICE_BILLING_CYCLE_billingAccountId_BILLING_SE" FOREIGN KEY("billingAccountId") REFERENCES "public"."BILLING_SERVICE_BILLING_ACCOUNT"("id");
ALTER TABLE "public"."BILLING_SERVICE_INVOICE" ADD CONSTRAINT "fk_BILLING_SERVICE_INVOICE_billingAccountId_BILLING_SERVICE_" FOREIGN KEY("billingAccountId") REFERENCES "public"."BILLING_SERVICE_BILLING_ACCOUNT"("id");
ALTER TABLE "public"."BILLING_SERVICE_INVOICE" ADD CONSTRAINT "fk_BILLING_SERVICE_INVOICE_billingCycleId_BILLING_SERVICE_BI" FOREIGN KEY("billingCycleId") REFERENCES "public"."BILLING_SERVICE_BILLING_CYCLE"("id");
ALTER TABLE "public"."BILLING_SERVICE_INVOICE_ITEM" ADD CONSTRAINT "fk_BILLING_SERVICE_INVOICE_ITEM_invoiceId_BILLING_SERVICE_IN" FOREIGN KEY("invoiceId") REFERENCES "public"."BILLING_SERVICE_INVOICE"("id");
ALTER TABLE "public"."CAMPAIGN_SERVICE_ELIGIBILITY" ADD CONSTRAINT "fk_CAMPAIGN_SERVICE_ELIGIBILITY_campaignId_CAMPAIGN_SERVICE_" FOREIGN KEY("campaignId") REFERENCES "public"."CAMPAIGN_SERVICE_CAMPAIGN"("id");
ALTER TABLE "public"."CAMPAIGN_SERVICE_OFFER" ADD CONSTRAINT "fk_CAMPAIGN_SERVICE_OFFER_campaignId_CAMPAIGN_SERVICE_CAMPAI" FOREIGN KEY("campaignId") REFERENCES "public"."CAMPAIGN_SERVICE_CAMPAIGN"("id");
ALTER TABLE "public"."CAMPAIGN_SERVICE_RULE" ADD CONSTRAINT "fk_CAMPAIGN_SERVICE_RULE_campaignId_CAMPAIGN_SERVICE_CAMPAIG" FOREIGN KEY("campaignId") REFERENCES "public"."CAMPAIGN_SERVICE_CAMPAIGN"("id");
ALTER TABLE "public"."CUSTOMER_SERVICE_ADDRESS" ADD CONSTRAINT "fk_CUSTOMER_SERVICE_ADDRESS_customerId_CUSTOMER_SERVICE_CUST" FOREIGN KEY("customerId") REFERENCES "public"."CUSTOMER_SERVICE_CUSTOMER"("id");
ALTER TABLE "public"."CUSTOMER_SERVICE_CONSENT" ADD CONSTRAINT "fk_CUSTOMER_SERVICE_CONSENT_customerId_CUSTOMER_SERVICE_CUST" FOREIGN KEY("customerId") REFERENCES "public"."CUSTOMER_SERVICE_CUSTOMER"("id");
ALTER TABLE "public"."CUSTOMER_SERVICE_CONTACT" ADD CONSTRAINT "fk_CUSTOMER_SERVICE_CONTACT_customerId_CUSTOMER_SERVICE_CUST" FOREIGN KEY("customerId") REFERENCES "public"."CUSTOMER_SERVICE_CUSTOMER"("id");
ALTER TABLE "public"."CUSTOMER_SERVICE_DOCUMENT" ADD CONSTRAINT "fk_CUSTOMER_SERVICE_DOCUMENT_customerId_CUSTOMER_SERVICE_CUS" FOREIGN KEY("customerId") REFERENCES "public"."CUSTOMER_SERVICE_CUSTOMER"("id");
ALTER TABLE "public"."CUSTOMER_SERVICE_NOTE" ADD CONSTRAINT "fk_CUSTOMER_SERVICE_NOTE_customerId_CUSTOMER_SERVICE_CUSTOME" FOREIGN KEY("customerId") REFERENCES "public"."CUSTOMER_SERVICE_CUSTOMER"("id");
ALTER TABLE "public"."IDENTITY_SERVICE_ROLE_PERMISSION" ADD CONSTRAINT "fk_IDENTITY_SERVICE_ROLE_PERMISSION_permissionId_IDENTITY_SE" FOREIGN KEY("permissionId") REFERENCES "public"."IDENTITY_SERVICE_PERMISSION"("id");
ALTER TABLE "public"."IDENTITY_SERVICE_ROLE_PERMISSION" ADD CONSTRAINT "fk_IDENTITY_SERVICE_ROLE_PERMISSION_roleId_IDENTITY_SERVICE_" FOREIGN KEY("roleId") REFERENCES "public"."IDENTITY_SERVICE_ROLE"("id");
ALTER TABLE "public"."IDENTITY_SERVICE_USER_ROLE" ADD CONSTRAINT "fk_IDENTITY_SERVICE_USER_ROLE_roleId_IDENTITY_SERVICE_ROLE_i" FOREIGN KEY("roleId") REFERENCES "public"."IDENTITY_SERVICE_ROLE"("id");
ALTER TABLE "public"."IDENTITY_SERVICE_AUDIT_LOG" ADD CONSTRAINT "fk_IDENTITY_SERVICE_AUDIT_LOG_actorUserId_IDENTITY_SERVICE_U" FOREIGN KEY("actorUserId") REFERENCES "public"."IDENTITY_SERVICE_USER"("id");
ALTER TABLE "public"."IDENTITY_SERVICE_REFRESH_TOKEN" ADD CONSTRAINT "fk_IDENTITY_SERVICE_REFRESH_TOKEN_userId_IDENTITY_SERVICE_US" FOREIGN KEY("userId") REFERENCES "public"."IDENTITY_SERVICE_USER"("id");
ALTER TABLE "public"."IDENTITY_SERVICE_USER_ROLE" ADD CONSTRAINT "fk_IDENTITY_SERVICE_USER_ROLE_userId_IDENTITY_SERVICE_USER_i" FOREIGN KEY("userId") REFERENCES "public"."IDENTITY_SERVICE_USER"("id");
ALTER TABLE "public"."INVENTORY_SERVICE_STOCK_ITEM" ADD CONSTRAINT "fk_INVENTORY_SERVICE_STOCK_ITEM_locationId_INVENTORY_SERVICE" FOREIGN KEY("locationId") REFERENCES "public"."INVENTORY_SERVICE_STOCK_LOCATION"("id");
ALTER TABLE "public"."ORDER_SERVICE_FULFILLMENT" ADD CONSTRAINT "fk_ORDER_SERVICE_FULFILLMENT_orderId_ORDER_SERVICE_ORDER_id" FOREIGN KEY("orderId") REFERENCES "public"."ORDER_SERVICE_ORDER"("id");
ALTER TABLE "public"."ORDER_SERVICE_ORDER_ITEM" ADD CONSTRAINT "fk_ORDER_SERVICE_ORDER_ITEM_orderId_ORDER_SERVICE_ORDER_id" FOREIGN KEY("orderId") REFERENCES "public"."ORDER_SERVICE_ORDER"("id");
ALTER TABLE "public"."ORDER_SERVICE_SAGA_STATE" ADD CONSTRAINT "fk_ORDER_SERVICE_SAGA_STATE_orderId_ORDER_SERVICE_ORDER_id" FOREIGN KEY("orderId") REFERENCES "public"."ORDER_SERVICE_ORDER"("id");
ALTER TABLE "public"."ORDER_SERVICE_STATUS_HISTORY" ADD CONSTRAINT "fk_ORDER_SERVICE_STATUS_HISTORY_orderId_ORDER_SERVICE_ORDER_" FOREIGN KEY("orderId") REFERENCES "public"."ORDER_SERVICE_ORDER"("id");
ALTER TABLE "public"."PAYMENT_SERVICE_REFUND" ADD CONSTRAINT "fk_PAYMENT_SERVICE_REFUND_paymentId_PAYMENT_SERVICE_PAYMENT_" FOREIGN KEY("paymentId") REFERENCES "public"."PAYMENT_SERVICE_PAYMENT"("id");
ALTER TABLE "public"."PAYMENT_SERVICE_TRANSACTION" ADD CONSTRAINT "fk_PAYMENT_SERVICE_TRANSACTION_paymentId_PAYMENT_SERVICE_PAY" FOREIGN KEY("paymentId") REFERENCES "public"."PAYMENT_SERVICE_PAYMENT"("id");
ALTER TABLE "public"."PAYMENT_SERVICE_PAYMENT" ADD CONSTRAINT "fk_PAYMENT_SERVICE_PAYMENT_paymentMethodId_PAYMENT_SERVICE_P" FOREIGN KEY("paymentMethodId") REFERENCES "public"."PAYMENT_SERVICE_PAYMENT_METHOD"("id");
ALTER TABLE "public"."PRODUCT_SERVICE_PRODUCT_CATEGORY" ADD CONSTRAINT "fk_PRODUCT_SERVICE_PRODUCT_CATEGORY_categoryId_PRODUCT_SERVI" FOREIGN KEY("categoryId") REFERENCES "public"."PRODUCT_SERVICE_CATEGORY"("id");
ALTER TABLE "public"."PRODUCT_SERVICE_PLAN_FEATURE" ADD CONSTRAINT "fk_PRODUCT_SERVICE_PLAN_FEATURE_planId_PRODUCT_SERVICE_PLAN_" FOREIGN KEY("planId") REFERENCES "public"."PRODUCT_SERVICE_PLAN"("id");
ALTER TABLE "public"."PRODUCT_SERVICE_PLAN" ADD CONSTRAINT "fk_PRODUCT_SERVICE_PLAN_productId_PRODUCT_SERVICE_PRODUCT_id" FOREIGN KEY("productId") REFERENCES "public"."PRODUCT_SERVICE_PRODUCT"("id");
ALTER TABLE "public"."PRODUCT_SERVICE_PRICE" ADD CONSTRAINT "fk_PRODUCT_SERVICE_PRICE_productId_PRODUCT_SERVICE_PRODUCT_i" FOREIGN KEY("productId") REFERENCES "public"."PRODUCT_SERVICE_PRODUCT"("id");
ALTER TABLE "public"."PRODUCT_SERVICE_PRODUCT_CATEGORY" ADD CONSTRAINT "fk_PRODUCT_SERVICE_PRODUCT_CATEGORY_productId_PRODUCT_SERVIC" FOREIGN KEY("productId") REFERENCES "public"."PRODUCT_SERVICE_PRODUCT"("id");
ALTER TABLE "public"."SUBSCRIPTION_SERVICE_ADDON" ADD CONSTRAINT "fk_SUBSCRIPTION_SERVICE_ADDON_subscriptionId_SUBSCRIPTION_SE" FOREIGN KEY("subscriptionId") REFERENCES "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"("id");
ALTER TABLE "public"."SUBSCRIPTION_SERVICE_PLAN_HISTORY" ADD CONSTRAINT "fk_SUBSCRIPTION_SERVICE_PLAN_HISTORY_subscriptionId_SUBSCRIP" FOREIGN KEY("subscriptionId") REFERENCES "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"("id");
ALTER TABLE "public"."SUBSCRIPTION_SERVICE_STATUS_HISTORY" ADD CONSTRAINT "fk_SUBSCRIPTION_SERVICE_STATUS_HISTORY_subscriptionId_SUBSCR" FOREIGN KEY("subscriptionId") REFERENCES "public"."SUBSCRIPTION_SERVICE_SUBSCRIPTION"("id");
ALTER TABLE "public"."TICKET_SERVICE_ATTACHMENT" ADD CONSTRAINT "fk_TICKET_SERVICE_ATTACHMENT_ticketId_TICKET_SERVICE_TICKET_" FOREIGN KEY("ticketId") REFERENCES "public"."TICKET_SERVICE_TICKET"("id");
ALTER TABLE "public"."TICKET_SERVICE_COMMENT" ADD CONSTRAINT "fk_TICKET_SERVICE_COMMENT_ticketId_TICKET_SERVICE_TICKET_id" FOREIGN KEY("ticketId") REFERENCES "public"."TICKET_SERVICE_TICKET"("id");
ALTER TABLE "public"."TICKET_SERVICE_STATUS_HISTORY" ADD CONSTRAINT "fk_TICKET_SERVICE_STATUS_HISTORY_ticketId_TICKET_SERVICE_TIC" FOREIGN KEY("ticketId") REFERENCES "public"."TICKET_SERVICE_TICKET"("id");