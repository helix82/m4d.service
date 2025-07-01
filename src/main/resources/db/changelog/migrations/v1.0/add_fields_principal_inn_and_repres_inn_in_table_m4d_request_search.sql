--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_request_search
    ADD COLUMN principal_inn varchar(255) NULL,
    ADD COLUMN representative_inn varchar(255) NULL;
