--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_confirm_temp (
    id bigserial PRIMARY KEY,
    registered_pep_date timestamp DEFAULT CURRENT_TIMESTAMP,
    xml_file_registered_pep_id int8 NULL,
    sig_file_registered_pep_id int8 NULL,
    registered_rr_date timestamp,
    xml_file_registered_rr_id int8 NULL,
    sig_file_registered_rr_id int8 NULL,
    m4d_registry_id int8 NOT NULL,
    FOREIGN KEY (xml_file_registered_pep_id) REFERENCES m4d_file (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (sig_file_registered_pep_id) REFERENCES m4d_file (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (xml_file_registered_rr_id) REFERENCES m4d_file (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (sig_file_registered_rr_id) REFERENCES m4d_file (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
);
--changeset Helix:2
INSERT INTO m4d_confirm_temp (registered_pep_date, xml_file_registered_pep_id, sig_file_registered_pep_id, registered_rr_date, xml_file_registered_rr_id, sig_file_registered_rr_id, m4d_registry_id)
SELECT
    с.registered_pep_date,
    с.xml_file_registered_pep_id,
    с.sig_file_registered_pep_id,
    с.registered_rr_date,
    с.xml_file_registered_rr_id,
    с.sig_file_registered_rr_id,
    с.m4d_registry_id
FROM
    m4d_confirm с
--changeset Helix:3
DROP TABLE IF EXISTS m4d_confirm;
--changeset Helix:4
ALTER TABLE m4d_confirm_temp RENAME TO m4d_confirm;