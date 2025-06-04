--liquibase formatted sql
--changeset Helix:1
BEGIN;
ALTER TABLE m4d_request_search
    ADD COLUMN status varchar(255) NULL,
    ADD COLUMN error_detail text NULL,
    ADD COLUMN status_date timestamp DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN registration_date timestamp NULL,
    ADD COLUMN start_date timestamp NULL,
    ADD COLUMN end_date timestamp NULL;
--changeset Helix:2
UPDATE m4d_request_search rs
SET
    status = rsd.status,
    start_date = rsd.start_date,
    end_date = rsd.end_date
    FROM m4d_request_search_detail rsd
    WHERE rsd.request_search_id = rs.id AND rsd.is_active = true
--changeset Helix:3
ALTER TABLE m4d_request_search
    ALTER COLUMN status SET NOT NULL;
--changeset Helix:4
DROP TABLE IF EXISTS m4d_request_search_detail;
END;