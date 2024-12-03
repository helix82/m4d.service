--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_verify_status (
                                        id bigserial PRIMARY KEY,
                                        guid varchar(255) NOT NULL,
                                        verify_date timestamp DEFAULT CURRENT_TIMESTAMP,
                                        status varchar(255) NULL
    );