--liquibase formatted sql
--changeset Helix:1
CREATE TYPE enum_file_mime_type AS ENUM('DSCP','XML','PDF','DOC','DOT','DOCX','DOTX','DOCM','DOTM','XLS','XLT','XLA',
    'XLSX','XLTX','PPT','POT','PPS','PPA','PPTX','POTX','PPSX','ODT','OTT','OTH','ODM','ODG','OTG','ODP','OTP','ODS',
    'OTS','ODC','ODF','ODB','ODI','OXT');
--changeset Helix:2
CREATE CAST (character varying as enum_file_mime_type) WITH INOUT AS IMPLICIT;