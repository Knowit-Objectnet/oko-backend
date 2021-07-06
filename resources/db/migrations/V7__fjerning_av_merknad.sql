ALTER TABLE planlagt_henting
DROP COLUMN merknad;

ALTER TABLE henteplan_kategori
DROP COLUMN merknad;

ALTER TABLE ekstra_henting
DROP COLUMN merknad;

ALTER TABLE ekstra_henting
ADD COLUMN beskrivelse text;