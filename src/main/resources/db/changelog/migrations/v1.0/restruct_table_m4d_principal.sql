--liquibase formatted sql
--changeset Helix:1
BEGIN;
CREATE TABLE m4d_principal_detail (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    surname VARCHAR(255),
    patronymic VARCHAR(255),
    job_title VARCHAR(255),
    version int8,
    is_active bool,
    principal_id BIGINT,
    CONSTRAINT fk_principal
      FOREIGN KEY (principal_id)
          REFERENCES m4d_principal (id)
          ON DELETE CASCADE
);
--changeset Helix:2
INSERT INTO m4d_principal_detail (name, surname, patronymic, job_title, principal_id, version, is_active)
    SELECT name, surname, patronymic, job_title, id, '1', 'true'
    FROM m4d_principal;
--changeset Helix:3
ALTER TABLE m4d_principal
    DROP COLUMN name,
    DROP COLUMN surname,
    DROP COLUMN patronymic,
    DROP COLUMN job_title;
--changeset Helix:4
ALTER TABLE m4d_registry
    ADD COLUMN principal_detail_id int8;
--changeset Helix:5
ALTER TABLE m4d_registry
    ADD CONSTRAINT fk_principal_detail
        FOREIGN KEY (principal_detail_id)
            REFERENCES m4d_principal_detail (id)
            ON DELETE CASCADE;
--changeset Helix:6
UPDATE m4d_registry r
SET principal_detail_id = (
    SELECT pd.id
    FROM m4d_principal_detail pd
    WHERE pd.principal_id = r.principal_id
);
--changeset Helix:7
ALTER TABLE m4d_registry
    ALTER COLUMN principal_detail_id SET NOT NULL;
END;