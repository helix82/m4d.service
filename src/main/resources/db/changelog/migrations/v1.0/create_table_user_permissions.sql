--liquibase formatted sql
--changeset Peter:1
CREATE TABLE user_permissions
(
    id BIGSERIAL PRIMARY KEY,
    login TEXT NOT NULL UNIQUE,
    permissions INTEGER NOT NULL
);