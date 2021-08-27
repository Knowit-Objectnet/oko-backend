ALTER TABLE partner
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE partner
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE stasjon
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE stasjon
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE kontakt
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE kontakt
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE avtale
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE avtale
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE henteplan
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE henteplan
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE planlagt_henting
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE planlagt_henting
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE ekstra_henting
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE ekstra_henting
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE kategori
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE kategori
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE henteplan_kategori
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE henteplan_kategori
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE utlysning
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE utlysning
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE ekstra_henting_kategori
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE ekstra_henting_kategori
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE vektregistrering
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE vektregistrering
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE aarsak
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE aarsak
ADD COLUMN oppdatert timestamp DEFAULT now();

ALTER TABLE verifisering
ADD COLUMN laget timestamp DEFAULT now();

ALTER TABLE verifisering
ADD COLUMN oppdatert timestamp DEFAULT now();

