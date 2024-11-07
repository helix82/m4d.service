--liquibase formatted sql
--changeset Helix:1
CREATE TABLE IF NOT EXISTS m4d_revoke (
    id bigserial PRIMARY KEY,
    argument text NOT NULL,
    author enum_author_type NOT NULL,
    sig_file_id int8 NULL,
    FOREIGN KEY (sig_file_id) REFERENCES m4d_file (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (sig_file_id)
);
CREATE INDEX idx_revoke_author ON m4d_revoke (author);
CREATE INDEX idx_revoke_sig_file_id ON m4d_revoke (sig_file_id);
--changeset Helix:2
INSERT INTO m4d_revoke(argument, author, sig_file_id) VALUES
    ('Test', 'PRINCIPAL', NULL);