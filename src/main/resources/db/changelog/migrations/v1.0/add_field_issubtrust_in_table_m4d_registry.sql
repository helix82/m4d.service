--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_registry
    ADD COLUMN is_sub_trust BOOL NULL;
--changeset Helix:2
UPDATE m4d_registry r
SET is_sub_trust = false;
--changeset Helix:3
ALTER TABLE m4d_registry
    ALTER COLUMN is_sub_trust SET NOT NULL;
END;