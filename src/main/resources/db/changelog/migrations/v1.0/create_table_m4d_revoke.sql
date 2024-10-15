--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_revoke (
    id bigserial PRIMARY KEY,
    argument text NOT NULL,
    author enum_author_type NOT NULL,
    sig_file_id int8 NULL
);
CREATE INDEX idx_revoke_author ON m4d_revoke (author);
--changeset Helix:2
INSERT INTO m4d_revoke(argument, author, sig_file_id) VALUES
    ('Test', 'PRINCIPAL', NULL);