--liquibase formatted sql
--changeset Helix:1
ALTER TABLE m4d_request_search
    ADD COLUMN zip_file_id bigint NULL;

ALTER TABLE m4d_request_search
    ADD CONSTRAINT fk_zip_file
        FOREIGN KEY (zip_file_id) REFERENCES m4d_file (id)
            ON UPDATE CASCADE ON DELETE RESTRICT;