--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_revoke DROP CONSTRAINT m4d_revoke_sig_file_id_key;