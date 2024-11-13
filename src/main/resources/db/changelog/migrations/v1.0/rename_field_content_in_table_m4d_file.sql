--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_file RENAME COLUMN content TO file_content;