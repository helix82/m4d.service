--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_file (
    id bigserial PRIMARY KEY,
    "content" text NOT NULL,
    file_logical_type enum_file_logical_type NOT NULL,
    file_mime_type enum_file_mime_type NOT NULL,
    filename varchar(255) NULL
);