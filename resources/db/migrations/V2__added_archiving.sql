ALTER table partner
    ADD arkivert boolean default false;

ALTER table stasjon
    ADD arkivert boolean default false;

ALTER table kontakt
    ADD arkivert boolean default false;

ALTER table stasjon_kontakt_person
    ADD arkivert boolean default false;

ALTER table partner_kontakt_person
    ADD arkivert boolean default false;

ALTER table avtale
    ADD arkivert boolean default false;

ALTER table henteplan
    ADD arkivert boolean default false;

ALTER table planlagt_henting
    ADD arkivert boolean default false;

ALTER table ekstra_henting
    ADD arkivert boolean default false;

ALTER table kategori
    ADD arkivert boolean default false;

ALTER table utlysning
    ADD arkivert boolean default false;