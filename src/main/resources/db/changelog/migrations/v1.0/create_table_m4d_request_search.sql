--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_request_search (
    id bigserial PRIMARY KEY,
    create_date timestamp DEFAULT CURRENT_TIMESTAMP,
    modify_date timestamp NULL,
    guid varchar(255) NULL,
    hash_xml varchar(255) NULL
    );