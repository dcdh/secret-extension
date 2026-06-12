CREATE TABLE IF NOT EXISTS secret
(
    name  character varying(255) not null,
    value character varying(255) not null,
    CONSTRAINT aggregate_root_pkey PRIMARY KEY (name)
);
