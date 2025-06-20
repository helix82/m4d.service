--liquibase formatted sql
--changeset Helix:1
BEGIN;
    DELETE FROM m4d_confirm;
    DELETE FROM m4d_revoke_confirm;
    DELETE FROM m4d_revoke;
    DELETE FROM m4d_request_search;
    DELETE FROM m4d_representative_detail;
    DELETE FROM m4d_representative;
    DELETE FROM m4d_principal_detail;
    DELETE FROM m4d_principal;
    DELETE FROM m4d_registry_m4d_credential;
    DELETE FROM m4d_registry;
    DELETE FROM m4d_file;
END;