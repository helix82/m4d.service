--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_registry
    ADD COLUMN status_in_rr varchar(255) NULL,
    ADD COLUMN error_in_rr_detail text NULL;
--changeset Helix:2
ALTER TABLE m4d_registry
    DROP COLUMN error_detail;
END;