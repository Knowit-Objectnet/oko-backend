CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE aarsak (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    beskrivelse text NOT NULL,
    type VARCHAR NOT NULL DEFAULT 'ALLE',
    arkivert timestamp
);

ALTER TABLE planlagt_henting
DROP COLUMN aarsak;

ALTER TABLE planlagt_henting
ADD COLUMN aarsak_id uuid;

ALTER TABLE planlagt_henting
ADD CONSTRAINT fk_aarsak_id FOREIGN KEY (aarsak_id) REFERENCES aarsak;

