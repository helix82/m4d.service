--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_revoke RENAME COLUMN content TO file_content;