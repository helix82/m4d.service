--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_revoke
    ADD COLUMN status varchar(255) NULL,
    ADD COLUMN error_detail text NULL,
    ADD COLUMN create_date timestamp DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN xml_file_id int8 NULL,
    ADD COLUMN m4d_registry_id int8 NULL;
--changeset Helix:2
UPDATE m4d_revoke r
SET m4d_registry_id = (
    SELECT mr.id
    FROM m4d_registry mr
    WHERE mr.mrpa_recall_id = r.id
);
--changeset Helix:3
ALTER TABLE m4d_revoke
    ADD CONSTRAINT fk_m4d_registry
        FOREIGN KEY (m4d_registry_id)
            REFERENCES m4d_registry (id)
            ON DELETE CASCADE;
--changeset Helix:4
ALTER TABLE m4d_revoke
    ALTER COLUMN m4d_registry_id SET NOT NULL;
--changeset Helix:5
ALTER TABLE m4d_revoke
    DROP COLUMN argument,
    DROP COLUMN author;
END;