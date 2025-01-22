--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_file ADD COLUMN file_logical_type_temp VARCHAR;
UPDATE m4d_file SET file_logical_type_temp = file_logical_type::VARCHAR;
ALTER TABLE m4d_file DROP COLUMN file_logical_type;
ALTER TABLE m4d_file RENAME COLUMN file_logical_type_temp TO file_logical_type;
DROP CAST (character varying AS enum_file_logical_type);
--changeset Helix:2
DROP TYPE enum_file_logical_type;
COMMIT;