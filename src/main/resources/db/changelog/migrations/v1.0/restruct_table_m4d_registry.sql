--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_registry
    ADD COLUMN organisation_inn varchar(10) NULL,
    ADD COLUMN status varchar(255) NULL,
    ADD COLUMN error_detail text NULL,
    ADD COLUMN xml_file_id int8 NULL;
--changeset Helix:2
UPDATE m4d_registry r
    SET organisation_inn = (
        SELECT o.inn
        FROM m4d_organisation o
        WHERE o.id = r.organisation_id
    );
--changeset Helix:3
UPDATE m4d_registry r
    SET status = 'CREATED';
--changeset Helix:4
ALTER TABLE m4d_registry
    DROP CONSTRAINT m4d_registry_organisation_id_fkey,
    DROP CONSTRAINT m4d_registry_mrpa_recall_id_fkey;
--changeset Helix:5
ALTER TABLE m4d_registry_m4d_credential
    DROP CONSTRAINT m4d_registry_m4d_credential_mrpa_registry_id_fkey;
--changeset Helix:6
ALTER TABLE m4d_registry
    DROP COLUMN internal_number,
    DROP COLUMN organisation_id,
    DROP COLUMN mrpa_recall_id;
END;