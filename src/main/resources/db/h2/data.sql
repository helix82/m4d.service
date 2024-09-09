INSERT INTO mrpa_registry (end_date, guid, internal_number, create_date, mrpa_xml_filename, organisation_id, principal_id, recalled_xml_path, representative_id, sig_path, start_date, mrpa_recall_id)
VALUES
    ('2025-06-09T21:00:00Z', '2bd5bf62-6795-4fa0-a157-a673a98da556', NULL, '2024-06-10T21:00:00Z', '', 1, 1, NULL, 1, NULL, '2024-06-10T21:00:00Z', 1),
    ('2025-06-17T21:00:00Z', '270220da-575b-44a0-8490-8b91b9f0b8fe', NULL, '2024-06-18T21:00:00Z', '', 2, 2, NULL, 2, NULL, '2024-06-18T21:00:00Z', null),
    ('2024-06-16T21:00:00Z', 'a8fc500b-ce1d-40e9-8474-9e0ced2cecbe', NULL, '2023-06-17T21:00:00Z', '', 3, 3, NULL, 3, NULL, '2023-06-17T21:00:00Z', null),
    ('2025-05-16T21:00:00Z', '66f3bf6f-2736-49dd-8993-5faf4ca69ebd', NULL, '2024-05-17T21:00:00Z', '', 4, 4, NULL, 4, NULL, '2024-05-17T21:00:00Z', null),
    ('2025-05-21T21:00:00Z', 'fe9d07ba-b8a5-417a-994c-e90a60e88d07', NULL, '2024-05-22T21:00:00Z', '', 5, 5, NULL, 5, NULL, '2024-05-22T21:00:00Z', null);
INSERT INTO mrpa_registry_credential (credential_id, mrpa_registry_id)
VALUES
    (1, 1),
    (4, 1),
    (2, 2),
    (3, 3),
    (5, 4),
    (1, 5),
    (2, 5),
    (3, 5);
INSERT INTO mrpa_recall (argument, author, signature)
VALUES
    ('Test', 'PRINCIPAL', NULL);