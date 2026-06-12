CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;
CREATE TABLE IF NOT EXISTS secret
(
    name  character varying(255) not null,
    value bytea not null,
    CONSTRAINT aggregate_root_pkey PRIMARY KEY (name)
);
