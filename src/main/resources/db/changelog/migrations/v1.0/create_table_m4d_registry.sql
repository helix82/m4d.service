--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_registry (
    id bigserial PRIMARY KEY,
    create_date timestamp DEFAULT CURRENT_TIMESTAMP,
    end_date timestamp NOT NULL,
    guid varchar(255) NULL,
    internal_number varchar(255) NULL,
    organisation_id int8 NOT NULL,
    principal_id int8 NOT NULL,
    representative_id int8 NOT NULL,
    start_date timestamp NOT NULL,
    sig_file_id int8 NULL,
    mrpa_recall_id int8 NULL,
    FOREIGN KEY (organisation_id) REFERENCES m4d_organisation (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (principal_id) REFERENCES m4d_principal (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (representative_id) REFERENCES m4d_representative (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (mrpa_recall_id) REFERENCES m4d_revoke (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (mrpa_recall_id),
    FOREIGN KEY (sig_file_id) REFERENCES m4d_file (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (sig_file_id)
);
CREATE INDEX idx_registry_date_range ON m4d_registry (start_date, end_date);
CREATE INDEX idx_registry_sig_file_id ON m4d_registry (sig_file_id);
CREATE INDEX idx_registry_mrpa_recall_id ON m4d_registry (mrpa_recall_id);
--changeset Helix:2
INSERT INTO m4d_registry(end_date, guid, internal_number, create_date, organisation_id, principal_id, representative_id, sig_file_id, start_date, mrpa_recall_id) VALUES
    ('2025-06-09T21:00:00Z', '2bd5bf62-6795-4fa0-a157-a673a98da556', NULL, '2024-06-10T21:00:00Z', 1, 1, 1, NULL, '2024-06-10T21:00:00Z', 1),
    ('2025-06-17T21:00:00Z', '270220da-575b-44a0-8490-8b91b9f0b8fe', NULL, '2024-06-18T21:00:00Z', 2, 2, 2, NULL, '2024-06-18T21:00:00Z', null),
    ('2024-06-16T21:00:00Z', 'a8fc500b-ce1d-40e9-8474-9e0ced2cecbe', NULL, '2023-06-17T21:00:00Z', 3, 3, 3, NULL, '2023-06-17T21:00:00Z', null),
    ('2025-05-16T21:00:00Z', '66f3bf6f-2736-49dd-8993-5faf4ca69ebd', NULL, '2024-05-17T21:00:00Z', 4, 4, 4, NULL, '2024-05-17T21:00:00Z', null),
    ('2025-05-21T21:00:00Z', 'fe9d07ba-b8a5-417a-994c-e90a60e88d07', NULL, '2024-05-22T21:00:00Z', 5, 5, 5, NULL, '2024-05-22T21:00:00Z', null);