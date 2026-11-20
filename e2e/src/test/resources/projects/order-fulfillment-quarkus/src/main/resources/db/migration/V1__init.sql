-- Initial schema. Seeded from Hibernate DDL:
--   ./mvnw test -Dtest=OrderRepositoryIntegrationTest \
--     -Dquarkus.hibernate-orm.scripts.generation=create \
--     -Dquarkus.hibernate-orm.scripts.generation.create-target=target/create.sql
-- FlywaySchemaTest validates entities against these migrations.
create sequence order_table_seq start with 1 increment by 50;

create table order_table (
    id                 bigint       not null,
    version            integer      not null,
    order_number       varchar(36)  not null unique,
    status             integer      not null check (status in (1, 2, 3, 4)),
    total_amount       numeric(38, 2) not null,
    currency           varchar(3)   not null,
    payment_reference  varchar(255),
    tracking_number    varchar(255),
    items              jsonb,
    created_by         varchar(255),
    created_date       timestamp    not null,
    last_modified_by   varchar(255),
    last_modified_date timestamp    not null,
    primary key (id)
);
