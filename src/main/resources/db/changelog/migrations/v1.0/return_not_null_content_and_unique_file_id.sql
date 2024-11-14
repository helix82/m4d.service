--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_file ALTER COLUMN file_content SET NOT NULL;
ALTER TABLE m4d_revoke ADD CONSTRAINT m4d_revoke_sig_file_id_key UNIQUE (sig_file_id);
COMMIT;