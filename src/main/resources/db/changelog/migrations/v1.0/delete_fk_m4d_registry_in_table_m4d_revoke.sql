--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_revoke
DROP CONSTRAINT fk_m4d_registry;
END;