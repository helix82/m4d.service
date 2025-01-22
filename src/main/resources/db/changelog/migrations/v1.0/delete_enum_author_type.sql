--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_revoke ADD COLUMN author_temp VARCHAR;
UPDATE m4d_revoke SET author_temp = author::VARCHAR;
ALTER TABLE m4d_revoke DROP COLUMN author;
ALTER TABLE m4d_revoke RENAME COLUMN author_temp TO author;
DROP CAST (character varying AS enum_author_type);
--changeset Helix:2
DROP TYPE enum_author_type;
COMMIT;