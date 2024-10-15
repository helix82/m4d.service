--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_registry_m4d_credential (
    id bigserial PRIMARY KEY,
    credential_id int8 NOT NULL,
    mrpa_registry_id int8 NOT NULL,
    FOREIGN KEY (credential_id) REFERENCES m4d_credential (id),
    FOREIGN KEY (mrpa_registry_id) REFERENCES m4d_registry (id)
);
CREATE INDEX idx_credential_id ON m4d_registry_m4d_credential (credential_id);
CREATE INDEX idx_mrpa_registry_id ON m4d_registry_m4d_credential (mrpa_registry_id);
--changeset Helix:2
INSERT INTO m4d_registry_m4d_credential(credential_id, mrpa_registry_id) VALUES
    (1, 1),
    (4, 1),
    (2, 2),
    (3, 3),
    (5, 4),
    (1, 5),
    (2, 5),
    (3, 5)