```mermaid
erDiagram
    direction LR

    %% =========================================================
    %% identity-service / identity_db
    %% =========================================================
    IDENTITY_SERVICE_USER {
        uuid id PK
        string email UK
        string passwordHash
        string status
        datetime createdAt
        datetime updatedAt
    }

    IDENTITY_SERVICE_ROLE {
        uuid id PK
        string name UK
        string description
    }

    IDENTITY_SERVICE_PERMISSION {
        uuid id PK
        string code UK
        string description
    }

    IDENTITY_SERVICE_USER_ROLE {
        uuid userId PK
        uuid roleId PK
    }

    IDENTITY_SERVICE_ROLE_PERMISSION {
        uuid roleId PK
        uuid permissionId PK
    }

    IDENTITY_SERVICE_REFRESH_TOKEN {
        uuid id PK
        uuid userId
        string tokenHash
        datetime expiresAt
        datetime revokedAt
        datetime createdAt
    }

    IDENTITY_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    IDENTITY_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    IDENTITY_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% customer-service / customer_db
    %% =========================================================
    CUSTOMER_SERVICE_CUSTOMER {
        uuid id PK
        string type
        string firstName
        string lastName
        string identityNumber
        date dateOfBirth
        string status
        datetime createdAt
        datetime updatedAt
        datetime deletedAt
    }

    CUSTOMER_SERVICE_ADDRESS {
        uuid id PK
        uuid customerId
        string line1
        string city
        string district
        string postalCode
        boolean isDefault
        datetime createdAt
    }

    CUSTOMER_SERVICE_DOCUMENT {
        uuid id PK
        uuid customerId
        string type
        string fileRef
        string status
        datetime verifiedAt
        datetime createdAt
    }

    CUSTOMER_SERVICE_CUSTOMER_CONTACT {
        uuid id PK
        uuid customerId
        string type
        string value
        boolean isPrimary
        datetime verifiedAt
    }

    CUSTOMER_SERVICE_CUSTOMER_PREFERENCE {
        uuid id PK
        uuid customerId
        boolean smsOptIn
        boolean emailOptIn
        boolean pushOptIn
        string locale
    }

    CUSTOMER_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    CUSTOMER_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    CUSTOMER_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% product-catalog-service / catalog_db
    %% =========================================================
    PRODUCT_CATALOG_SERVICE_TARIFF {
        uuid id PK
        string code UK
        string name
        string type
        decimal monthlyFee
        int minutesIncluded
        int smsIncluded
        long dataMbIncluded
        string status
        datetime effectiveFrom
        datetime effectiveTo
        int version
        datetime createdAt
    }

    PRODUCT_CATALOG_SERVICE_ADDON {
        uuid id PK
        string code UK
        string name
        decimal price
        string type
        int validityDays
        string status
    }

    PRODUCT_CATALOG_SERVICE_TARIFF_ADDON {
        uuid tariffId PK
        uuid addonId PK
    }

    PRODUCT_CATALOG_SERVICE_PRODUCT_OFFERING {
        uuid id PK
        string code UK
        string name
        string productType
        string targetSegment
        datetime effectiveFrom
        datetime effectiveTo
        string status
    }

    PRODUCT_CATALOG_SERVICE_TARIFF_VERSION {
        uuid id PK
        string tariffCode
        int version
        json snapshotJson
        datetime effectiveFrom
        datetime effectiveTo
    }

    PRODUCT_CATALOG_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    PRODUCT_CATALOG_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    PRODUCT_CATALOG_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% order-service / order_db
    %% =========================================================
    ORDER_SERVICE_ORDER {
        uuid id PK
        uuid customerId
        string status
        decimal totalAmount
        string currency
        datetime createdAt
        datetime updatedAt
    }

    ORDER_SERVICE_ORDER_ITEM {
        uuid id PK
        uuid orderId
        string productCode
        string productType
        int quantity
        decimal unitPrice
        decimal lineTotal
    }

    ORDER_SERVICE_SAGA_STATE {
        uuid id PK
        uuid orderId
        string currentStep
        json payloadJson
        string status
        datetime lastUpdated
    }

    ORDER_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    ORDER_SERVICE_IDEMPOTENCY_KEY {
        uuid id PK
        string idempotencyKey UK
        string requestHash
        json responseJson
        datetime createdAt
        datetime expiresAt
    }

    ORDER_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    ORDER_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% subscription-service / subscription_db
    %% =========================================================
    SUBSCRIPTION_SERVICE_SUBSCRIPTION {
        uuid id PK
        uuid customerId
        string msisdn
        string tariffCode
        json tariffSnapshotJson
        string status
        datetime activatedAt
        datetime suspendedAt
        datetime terminatedAt
        datetime createdAt
    }

    SUBSCRIPTION_SERVICE_MSISDN_POOL {
        string msisdn PK
        string status
        datetime reservedUntil
        datetime allocatedAt
    }

    SUBSCRIPTION_SERVICE_SIM_CARD {
        string iccid PK
        string imsi UK
        string msisdn
        string status
        datetime assignedAt
    }

    SUBSCRIPTION_SERVICE_SUBSCRIPTION_STATUS_HISTORY {
        uuid id PK
        uuid subscriptionId
        string oldStatus
        string newStatus
        string reason
        datetime changedAt
    }

    SUBSCRIPTION_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    SUBSCRIPTION_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    SUBSCRIPTION_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% usage-service / usage_db
    %% =========================================================
    USAGE_SERVICE_CDR_EVENT {
        uuid id PK
        string cdrRef UK
        uuid subscriptionId
        string msisdn
        string type
        long quantity
        datetime eventTime
        json rawPayloadJson
        datetime processedAt
    }

    USAGE_SERVICE_USAGE_RECORD {
        uuid id PK
        uuid subscriptionId
        string type
        long quantity
        datetime recordedAt
        string cdrRef UK
    }

    USAGE_SERVICE_QUOTA {
        uuid id PK
        uuid subscriptionId
        datetime periodStart
        datetime periodEnd
        int minutesRemaining
        int smsRemaining
        long mbRemaining
        datetime updatedAt
    }

    USAGE_SERVICE_USAGE_AGGREGATE {
        uuid id PK
        uuid subscriptionId
        datetime periodStart
        datetime periodEnd
        long voiceTotal
        long smsTotal
        long dataMbTotal
        decimal overageAmount
        datetime sentToBillingAt
    }

    USAGE_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    USAGE_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    USAGE_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% billing-service / billing_db
    %% =========================================================
    BILLING_SERVICE_BILL_RUN {
        uuid id PK
        datetime startedAt
        datetime completedAt
        string status
        int processedCount
        int failedCount
    }

    BILLING_SERVICE_INVOICE {
        uuid id PK
        uuid customerId
        uuid subscriptionId
        uuid billRunId
        uuid billCycleId
        datetime periodStart
        datetime periodEnd
        decimal subTotal
        decimal tax
        decimal grandTotal
        string currency
        string status
        datetime dueDate
        datetime issuedAt
        string pdfFileRef
    }

    BILLING_SERVICE_INVOICE_LINE {
        uuid id PK
        uuid invoiceId
        string description
        int quantity
        decimal unitPrice
        decimal lineTotal
        string lineType
    }

    BILLING_SERVICE_BILL_CYCLE {
        uuid id PK
        uuid customerId
        int dayOfMonth
        datetime nextRunDate
        string status
    }

    BILLING_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    BILLING_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    BILLING_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% payment-service / payment_db
    %% =========================================================
    PAYMENT_SERVICE_PAYMENT {
        uuid id PK
        uuid invoiceId
        uuid customerId
        uuid paymentMethodId
        decimal amount
        string currency
        string method
        string status
        string externalRef
        datetime paidAt
        datetime createdAt
    }

    PAYMENT_SERVICE_PAYMENT_ATTEMPT {
        uuid id PK
        uuid paymentId
        int attemptNo
        json responseJson
        datetime attemptedAt
        string status
    }

    PAYMENT_SERVICE_PAYMENT_METHOD {
        uuid id PK
        uuid customerId
        string type
        string maskedCardNumber
        string tokenRef
        boolean isDefault
        datetime createdAt
    }

    PAYMENT_SERVICE_IDEMPOTENCY_KEY {
        uuid id PK
        string idempotencyKey UK
        string requestHash
        json responseJson
        datetime createdAt
        datetime expiresAt
    }

    PAYMENT_SERVICE_PAYMENT_RETRY_SCHEDULE {
        uuid id PK
        uuid paymentId
        int currentAttemptNo
        datetime nextRetryAt
        int retryDelayHours
        string status
        datetime createdAt
        datetime updatedAt
    }

    PAYMENT_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    PAYMENT_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    PAYMENT_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% notification-service / notification_db
    %% =========================================================
    NOTIFICATION_SERVICE_NOTIFICATION_TEMPLATE {
        uuid id PK
        string code UK
        string channel
        string locale
        string subject
        string bodyTemplate
        string status
        datetime createdAt
    }

    NOTIFICATION_SERVICE_NOTIFICATION {
        uuid id PK
        uuid userId
        uuid customerId
        string templateCode
        string channel
        json payloadJson
        string status
        datetime sentAt
        datetime createdAt
    }

    NOTIFICATION_SERVICE_NOTIFICATION_DELIVERY_ATTEMPT {
        uuid id PK
        uuid notificationId
        int attemptNo
        json providerResponseJson
        string status
        datetime attemptedAt
    }

    NOTIFICATION_SERVICE_USER_COMMUNICATION_PREFERENCE {
        uuid id PK
        uuid customerId
        boolean smsOptIn
        boolean emailOptIn
        boolean pushOptIn
        string locale
    }

    NOTIFICATION_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    NOTIFICATION_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    NOTIFICATION_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% ticket-service / ticket_db
    %% =========================================================
    TICKET_SERVICE_TICKET {
        uuid id PK
        uuid customerId
        string category
        string priority
        string status
        datetime slaDueAt
        datetime createdAt
        datetime resolvedAt
    }

    TICKET_SERVICE_TICKET_COMMENT {
        uuid id PK
        uuid ticketId
        uuid authorId
        string body
        datetime createdAt
    }

    TICKET_SERVICE_SLA_POLICY {
        uuid id PK
        string category
        string priority
        int responseTimeMinutes
        int resolutionTimeMinutes
    }

    TICKET_SERVICE_TICKET_ASSIGNMENT {
        uuid id PK
        uuid ticketId
        uuid assignedToUserId
        string assignedTeam
        datetime assignedAt
    }

    TICKET_SERVICE_OUTBOX_EVENT {
        uuid id PK
        uuid aggregateId
        string aggregateType
        string eventType
        json payloadJson
        string status
        datetime createdAt
        datetime publishedAt
    }

    TICKET_SERVICE_AUDIT_LOG {
        uuid id PK
        uuid actorUserId
        string action
        string entityType
        uuid entityId
        json oldValueJson
        json newValueJson
        string correlationId
        datetime createdAt
    }

    TICKET_SERVICE_PROCESSED_EVENT {
        uuid id PK
        uuid eventId UK
        string eventType
        string sourceService
        uuid aggregateId
        datetime processedAt
        string status
        string errorMessage
        datetime createdAt
    }

    %% =========================================================
    %% relationships
    %% =========================================================
    IDENTITY_SERVICE_USER ||--o{ IDENTITY_SERVICE_USER_ROLE : "USER 1-N USER_ROLE"
    IDENTITY_SERVICE_ROLE ||--o{ IDENTITY_SERVICE_USER_ROLE : "USER M-N ROLE via USER_ROLE"
    IDENTITY_SERVICE_ROLE ||--o{ IDENTITY_SERVICE_ROLE_PERMISSION : "ROLE 1-N ROLE_PERMISSION"
    IDENTITY_SERVICE_PERMISSION ||--o{ IDENTITY_SERVICE_ROLE_PERMISSION : "ROLE M-N PERMISSION via ROLE_PERMISSION"
    IDENTITY_SERVICE_USER ||--o{ IDENTITY_SERVICE_REFRESH_TOKEN : "USER 1-N REFRESH_TOKEN"
    IDENTITY_SERVICE_USER ||..o{ IDENTITY_SERVICE_AUDIT_LOG : "USER 1-N AUDIT_LOG"
    IDENTITY_SERVICE_USER ||..o{ IDENTITY_SERVICE_OUTBOX_EVENT : "USER 1-N OUTBOX_EVENT"
    CUSTOMER_SERVICE_CUSTOMER ||--o{ CUSTOMER_SERVICE_ADDRESS : "CUSTOMER 1-N ADDRESS"
    CUSTOMER_SERVICE_CUSTOMER ||--o{ CUSTOMER_SERVICE_DOCUMENT : "CUSTOMER 1-N DOCUMENT"
    CUSTOMER_SERVICE_CUSTOMER ||--o{ CUSTOMER_SERVICE_CUSTOMER_CONTACT : "CUSTOMER 1-N CUSTOMER_CONTACT"
    CUSTOMER_SERVICE_CUSTOMER ||--|| CUSTOMER_SERVICE_CUSTOMER_PREFERENCE : "CUSTOMER 1-1 CUSTOMER_PREFERENCE"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ CUSTOMER_SERVICE_AUDIT_LOG : "CUSTOMER 1-N AUDIT_LOG"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ CUSTOMER_SERVICE_OUTBOX_EVENT : "CUSTOMER 1-N OUTBOX_EVENT"
    PRODUCT_CATALOG_SERVICE_TARIFF ||--o{ PRODUCT_CATALOG_SERVICE_TARIFF_ADDON : "TARIFF 1-N TARIFF_ADDON"
    PRODUCT_CATALOG_SERVICE_ADDON ||--o{ PRODUCT_CATALOG_SERVICE_TARIFF_ADDON : "TARIFF M-N ADDON via TARIFF_ADDON"
    PRODUCT_CATALOG_SERVICE_TARIFF ||..o{ PRODUCT_CATALOG_SERVICE_TARIFF_VERSION : "TARIFF 1-N TARIFF_VERSION"
    PRODUCT_CATALOG_SERVICE_TARIFF ||..o{ PRODUCT_CATALOG_SERVICE_PRODUCT_OFFERING : "TARIFF 1-N PRODUCT_OFFERING"
    PRODUCT_CATALOG_SERVICE_ADDON ||..o{ PRODUCT_CATALOG_SERVICE_PRODUCT_OFFERING : "ADDON 1-N PRODUCT_OFFERING"
    PRODUCT_CATALOG_SERVICE_TARIFF ||..o{ PRODUCT_CATALOG_SERVICE_AUDIT_LOG : "TARIFF 1-N AUDIT_LOG"
    PRODUCT_CATALOG_SERVICE_TARIFF ||..o{ PRODUCT_CATALOG_SERVICE_OUTBOX_EVENT : "TARIFF 1-N OUTBOX_EVENT"
    ORDER_SERVICE_ORDER ||--o{ ORDER_SERVICE_ORDER_ITEM : "ORDER 1-N ORDER_ITEM"
    ORDER_SERVICE_ORDER ||--|| ORDER_SERVICE_SAGA_STATE : "ORDER 1-1 SAGA_STATE"
    ORDER_SERVICE_ORDER ||..o{ ORDER_SERVICE_OUTBOX_EVENT : "ORDER 1-N OUTBOX_EVENT"
    ORDER_SERVICE_ORDER ||..o{ ORDER_SERVICE_AUDIT_LOG : "ORDER 1-N AUDIT_LOG"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ ORDER_SERVICE_ORDER : "CUSTOMER 1-N ORDER"
    PRODUCT_CATALOG_SERVICE_PRODUCT_OFFERING ||..o{ ORDER_SERVICE_ORDER_ITEM : "PRODUCT_OFFERING 1-N ORDER_ITEM"
    SUBSCRIPTION_SERVICE_MSISDN_POOL ||--o{ SUBSCRIPTION_SERVICE_SUBSCRIPTION : "MSISDN_POOL 1-N SUBSCRIPTION"
    SUBSCRIPTION_SERVICE_MSISDN_POOL ||--o{ SUBSCRIPTION_SERVICE_SIM_CARD : "MSISDN_POOL 1-0..1 SIM_CARD"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||--o{ SUBSCRIPTION_SERVICE_SUBSCRIPTION_STATUS_HISTORY : "SUBSCRIPTION 1-N STATUS_HISTORY"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ SUBSCRIPTION_SERVICE_OUTBOX_EVENT : "SUBSCRIPTION 1-N OUTBOX_EVENT"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ SUBSCRIPTION_SERVICE_AUDIT_LOG : "SUBSCRIPTION 1-N AUDIT_LOG"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ SUBSCRIPTION_SERVICE_SUBSCRIPTION : "CUSTOMER 1-N SUBSCRIPTION"
    PRODUCT_CATALOG_SERVICE_TARIFF ||..o{ SUBSCRIPTION_SERVICE_SUBSCRIPTION : "TARIFF 1-N SUBSCRIPTION"
    USAGE_SERVICE_CDR_EVENT ||..|| USAGE_SERVICE_USAGE_RECORD : "CDR_EVENT 1-0..1 USAGE_RECORD"
    USAGE_SERVICE_USAGE_RECORD ||..o{ USAGE_SERVICE_OUTBOX_EVENT : "USAGE_RECORD 1-N OUTBOX_EVENT"
    USAGE_SERVICE_USAGE_RECORD ||..o{ USAGE_SERVICE_AUDIT_LOG : "USAGE_RECORD 1-N AUDIT_LOG"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ USAGE_SERVICE_CDR_EVENT : "SUBSCRIPTION 1-N CDR_EVENT"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ USAGE_SERVICE_USAGE_RECORD : "SUBSCRIPTION 1-N USAGE_RECORD"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ USAGE_SERVICE_QUOTA : "SUBSCRIPTION 1-N QUOTA"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ USAGE_SERVICE_USAGE_AGGREGATE : "SUBSCRIPTION 1-N USAGE_AGGREGATE"
    USAGE_SERVICE_USAGE_RECORD }o..o{ USAGE_SERVICE_USAGE_AGGREGATE : "no physical FK"
    BILLING_SERVICE_BILL_RUN ||--o{ BILLING_SERVICE_INVOICE : "BILL_RUN 1-N INVOICE"
    BILLING_SERVICE_BILL_CYCLE ||--o{ BILLING_SERVICE_INVOICE : "BILL_CYCLE 1-N INVOICE"
    BILLING_SERVICE_INVOICE ||--o{ BILLING_SERVICE_INVOICE_LINE : "INVOICE 1-N INVOICE_LINE"
    BILLING_SERVICE_INVOICE ||..o{ BILLING_SERVICE_OUTBOX_EVENT : "INVOICE 1-N OUTBOX_EVENT"
    BILLING_SERVICE_INVOICE ||..o{ BILLING_SERVICE_AUDIT_LOG : "INVOICE 1-N AUDIT_LOG"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ BILLING_SERVICE_BILL_CYCLE : "CUSTOMER 1-N BILL_CYCLE"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ BILLING_SERVICE_INVOICE : "CUSTOMER 1-N INVOICE"
    SUBSCRIPTION_SERVICE_SUBSCRIPTION ||..o{ BILLING_SERVICE_INVOICE : "SUBSCRIPTION 1-N INVOICE"
    BILLING_SERVICE_INVOICE }o..o{ USAGE_SERVICE_USAGE_AGGREGATE : "no physical FK"
    PAYMENT_SERVICE_PAYMENT_METHOD ||--o{ PAYMENT_SERVICE_PAYMENT : "PAYMENT_METHOD 1-N PAYMENT"
    PAYMENT_SERVICE_PAYMENT ||--o{ PAYMENT_SERVICE_PAYMENT_ATTEMPT : "PAYMENT 1-N PAYMENT_ATTEMPT"
    PAYMENT_SERVICE_PAYMENT ||--o{ PAYMENT_SERVICE_PAYMENT_RETRY_SCHEDULE : "PAYMENT 1-N PAYMENT_RETRY_SCHEDULE"
    PAYMENT_SERVICE_PAYMENT ||..o{ PAYMENT_SERVICE_OUTBOX_EVENT : "PAYMENT 1-N OUTBOX_EVENT"
    PAYMENT_SERVICE_PAYMENT ||..o{ PAYMENT_SERVICE_AUDIT_LOG : "PAYMENT 1-N AUDIT_LOG"
    BILLING_SERVICE_INVOICE ||..o{ PAYMENT_SERVICE_PAYMENT : "INVOICE 1-N PAYMENT"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ PAYMENT_SERVICE_PAYMENT : "CUSTOMER 1-N PAYMENT"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ PAYMENT_SERVICE_PAYMENT_METHOD : "CUSTOMER 1-N PAYMENT_METHOD"
    NOTIFICATION_SERVICE_NOTIFICATION_TEMPLATE ||..o{ NOTIFICATION_SERVICE_NOTIFICATION : "TEMPLATE 1-N NOTIFICATION"
    NOTIFICATION_SERVICE_NOTIFICATION ||--o{ NOTIFICATION_SERVICE_NOTIFICATION_DELIVERY_ATTEMPT : "NOTIFICATION 1-N DELIVERY_ATTEMPT"
    NOTIFICATION_SERVICE_NOTIFICATION ||..o{ NOTIFICATION_SERVICE_OUTBOX_EVENT : "NOTIFICATION 1-N OUTBOX_EVENT"
    NOTIFICATION_SERVICE_NOTIFICATION ||..o{ NOTIFICATION_SERVICE_AUDIT_LOG : "NOTIFICATION 1-N AUDIT_LOG"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ NOTIFICATION_SERVICE_NOTIFICATION : "CUSTOMER 1-N NOTIFICATION"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ NOTIFICATION_SERVICE_USER_COMMUNICATION_PREFERENCE : "CUSTOMER 1-N USER_COMMUNICATION_PREFERENCE"
    IDENTITY_SERVICE_USER ||..o{ NOTIFICATION_SERVICE_NOTIFICATION : "USER 1-N NOTIFICATION"
    TICKET_SERVICE_SLA_POLICY ||..o{ TICKET_SERVICE_TICKET : "SLA_POLICY 1-N TICKET"
    TICKET_SERVICE_TICKET ||--o{ TICKET_SERVICE_TICKET_COMMENT : "TICKET 1-N TICKET_COMMENT"
    TICKET_SERVICE_TICKET ||--o{ TICKET_SERVICE_TICKET_ASSIGNMENT : "TICKET 1-N TICKET_ASSIGNMENT"
    TICKET_SERVICE_TICKET ||..o{ TICKET_SERVICE_OUTBOX_EVENT : "TICKET 1-N OUTBOX_EVENT"
    TICKET_SERVICE_TICKET ||..o{ TICKET_SERVICE_AUDIT_LOG : "TICKET 1-N AUDIT_LOG"
    CUSTOMER_SERVICE_CUSTOMER ||..o{ TICKET_SERVICE_TICKET : "CUSTOMER 1-N TICKET"
    IDENTITY_SERVICE_USER ||..o{ TICKET_SERVICE_TICKET_COMMENT : "USER 1-N TICKET_COMMENT"
    IDENTITY_SERVICE_USER ||..o{ TICKET_SERVICE_TICKET_ASSIGNMENT : "USER 1-N TICKET_ASSIGNMENT"
```
