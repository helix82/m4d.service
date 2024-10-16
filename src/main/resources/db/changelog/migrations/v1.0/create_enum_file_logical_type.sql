--liquibase formatted sql
--changeset Helix:1
CREATE TYPE enum_file_logical_type AS ENUM('CONTAINER','CERTIFICATE','DOCUMENT');
--changeset Helix:2
CREATE CAST (character varying as enum_file_logical_type) WITH INOUT AS IMPLICIT;