--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_revoke_confirm (
    id bigserial PRIMARY KEY,
    registered_pep_date timestamp DEFAULT CURRENT_TIMESTAMP,
    xml_file_registered_pep_id int8 NULL,
    sig_file_registered_pep_id int8 NULL,
    registered_rr_date timestamp,
    xml_file_registered_rr_id int8 NULL,
    sig_file_registered_rr_id int8 NULL,
    m4d_revoke_id int8 NOT NULL,
    FOREIGN KEY (xml_file_registered_pep_id) REFERENCES m4d_file (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (xml_file_registered_pep_id),
    FOREIGN KEY (sig_file_registered_pep_id) REFERENCES m4d_file (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (sig_file_registered_pep_id),
    FOREIGN KEY (xml_file_registered_rr_id) REFERENCES m4d_file (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (xml_file_registered_rr_id),
    FOREIGN KEY (sig_file_registered_rr_id) REFERENCES m4d_file (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (sig_file_registered_rr_id)
);