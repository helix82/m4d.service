--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_file ALTER COLUMN file_content DROP NOT NULL;
