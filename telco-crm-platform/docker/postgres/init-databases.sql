-- =============================================================================
-- Database-per-service: her mikroservis icin ayri PostgreSQL veritabani
-- Mevcut bir PostgreSQL instance'a baglanip bu scripti calistir:
--   docker exec -i <container-name> psql -U admin -d postgres < init-databases.sql
-- =============================================================================

CREATE DATABASE identity_db;
CREATE DATABASE customer_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE subscription_db;
CREATE DATABASE usage_db;
CREATE DATABASE billing_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;
CREATE DATABASE ticket_db;

GRANT ALL PRIVILEGES ON DATABASE identity_db     TO admin;
GRANT ALL PRIVILEGES ON DATABASE customer_db     TO admin;
GRANT ALL PRIVILEGES ON DATABASE product_db      TO admin;
GRANT ALL PRIVILEGES ON DATABASE order_db        TO admin;
GRANT ALL PRIVILEGES ON DATABASE subscription_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE usage_db        TO admin;
GRANT ALL PRIVILEGES ON DATABASE billing_db      TO admin;
GRANT ALL PRIVILEGES ON DATABASE payment_db      TO admin;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE ticket_db       TO admin;
