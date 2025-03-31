--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_request_search_detail (
    id bigserial PRIMARY KEY,
    status varchar(255) NOT NULL,
    error_detail text NULL,
    status_date timestamp NULL,
    registration_date timestamp NULL,
    start_date timestamp NULL,
    end_date timestamp NULL,
    version int8 NOT NULL,
    is_active bool,
    request_search_id bigint,
    CONSTRAINT fk_request_search
        FOREIGN KEY (request_search_id)
            REFERENCES m4d_request_search (id)
            ON DELETE CASCADE
);