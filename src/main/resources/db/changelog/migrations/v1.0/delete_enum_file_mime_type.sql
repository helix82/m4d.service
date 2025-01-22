--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_file ADD COLUMN file_mime_type_temp VARCHAR;
UPDATE m4d_file SET file_mime_type_temp = file_mime_type::VARCHAR;
ALTER TABLE m4d_file DROP COLUMN file_mime_type;
ALTER TABLE m4d_file RENAME COLUMN file_mime_type_temp TO file_mime_type;
DROP CAST (character varying AS enum_file_mime_type);
--changeset Helix:2
DROP TYPE enum_file_mime_type;
COMMIT;