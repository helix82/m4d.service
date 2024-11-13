--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_revoke ADD CONSTRAINT m4d_revoke_sig_file_id_key UNIQUE (sig_file_id);
