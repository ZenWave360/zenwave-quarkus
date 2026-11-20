-- Seed data for @QuarkusTest (loaded by quarkus.hibernate-orm.sql-load-script after drop-and-create).
-- Keep in sync with src/test/resources/data/jpa/Order/1.json (in-memory tests).
INSERT INTO order_table (id, version, order_number, status, total_amount, currency, payment_reference, tracking_number, items,
                         created_by, created_date, last_modified_by, last_modified_date)
VALUES (nextval('order_table_seq'), 0, 'ORD-0001', 1, 59.90, 'EUR', NULL, NULL,
        '[{"productId":"P-001","productName":"Keyboard","quantity":1,"unitPrice":49.90},{"productId":"P-002","productName":"Mouse pad","quantity":1,"unitPrice":10.00}]',
        'system', now(), 'system', now());
