--liquibase formatted sql
--changeset Helix:1
BEGIN;
CREATE TABLE m4d_representative_detail (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    surname VARCHAR(255),
    patronymic VARCHAR(255),
    series_number VARCHAR(255),
    date_issue timestamp NOT NULL,
    who_issue text,
    department_code VARCHAR(255),
    version int8,
    is_active bool,
    representative_id BIGINT,
    CONSTRAINT fk_representative
      FOREIGN KEY (representative_id)
          REFERENCES m4d_representative (id)
          ON DELETE CASCADE
);
--changeset Helix:2
INSERT INTO m4d_representative_detail (name, surname, patronymic, series_number, date_issue, who_issue, department_code, representative_id, version, is_active)
SELECT
    r.name,
    r.surname,
    r.patronymic,
    p.series_number,
    p.date_issue,
    p.who_issue,
    p.department_code,
    r.id,
    '1',
    'true'
FROM
    m4d_representative r
        JOIN
    m4d_representative_passport p ON r.id = p.representative_id;
--changeset Helix:3
ALTER TABLE m4d_representative
    DROP COLUMN name,
    DROP COLUMN surname,
    DROP COLUMN patronymic;
--changeset Helix:4
DROP TABLE IF EXISTS m4d_representative_passport;
--changeset Helix:5
ALTER TABLE m4d_registry
    ADD COLUMN representative_detail_id int8;
--changeset Helix:6
ALTER TABLE m4d_registry
    ADD CONSTRAINT fk_representative_detail
        FOREIGN KEY (representative_detail_id)
            REFERENCES m4d_representative_detail (id)
            ON DELETE CASCADE;
--changeset Helix:7
UPDATE m4d_registry r
SET representative_detail_id = (
    SELECT rd.id
    FROM m4d_representative_detail rd
    WHERE rd.representative_id = r.representative_id
);
--changeset Helix:8
ALTER TABLE m4d_registry
    ALTER COLUMN representative_detail_id SET NOT NULL;
END;