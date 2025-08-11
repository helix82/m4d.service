--liquibase formatted sql
--changeset Peter:1
ALTER TABLE user_permissions
    ADD COLUMN payload VARCHAR(2000);