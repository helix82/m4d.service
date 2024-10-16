--liquibase formatted sql
--changeset Helix:1
CREATE TYPE enum_author_type AS ENUM('PRINCIPAL','REPRESENTATIVE','UNKNOWN');
--changeset Helix:2
CREATE CAST (character varying as enum_author_type) WITH INOUT AS IMPLICIT;