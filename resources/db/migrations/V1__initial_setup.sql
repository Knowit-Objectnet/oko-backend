CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create TABLE partner (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    navn varchar(255) not null unique,
    ideell boolean
);

create TABLE stasjon (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    navn varchar(255) not null,
    type varchar not null
);

INSERT INTO stasjon (id, navn, type) VALUES ('83c9b534-7b2a-46d4-9e02-2bdab38601c2', 'Grønmo', 'GJENBRUK');
INSERT INTO stasjon (id, navn, type) VALUES ('ddf55fd5-e711-43a4-9345-4270e5d125dc', 'Haraldrud', 'GJENBRUK');
INSERT INTO stasjon (id, navn, type) VALUES ('f0d36631-cfc2-4bff-8af6-5ee9505e14ea', 'Smestad', 'GJENBRUK');
INSERT INTO stasjon (id, navn, type) VALUES ('a2e8e766-e1a1-42bb-9cb8-044e55a90900', 'Ryen', 'GJENBRUK');
INSERT INTO stasjon (id, navn, type) VALUES ('e294f824-6494-41e2-b0be-aca1943e5067', 'Grefsen', 'GJENBRUK');

create TABLE kontakt (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    aktor_id uuid not null,
    navn varchar(255) not null,
    telefon varchar(20),
    epost varchar(255),
    rolle varchar(50)
);

create TABLE avtale (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    aktor_id uuid not null,
    type varchar not null,
    start_dato date,
    slutt_dato date
);

create TABLE henteplan (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    avtale_id uuid not null,
    stasjon_id uuid not null,
    frekvens varchar not null,
    start_tidspunkt timestamp not null,
    slutt_tidspunkt timestamp not null,
    ukedag int,
    merknad text,
    FOREIGN KEY (avtale_id) references avtale,
    FOREIGN KEY (stasjon_id) references stasjon
);

create TABLE planlagt_henting (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    start_tidspunkt timestamp not null,
    slutt_tidspunkt timestamp not null,
    merknad text,
    henteplan_id uuid not null,
    avlyst timestamp,
    FOREIGN KEY (henteplan_id) references henteplan
);

create TABLE ekstra_henting (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    start_tidspunkt timestamp not null,
    slutt_tidspunkt timestamp not null,
    merknad text,
    stasjon_id uuid not null,
    FOREIGN KEY (stasjon_id) references stasjon
);

create TABLE kategori (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    navn varchar(255) not null
);

INSERT INTO kategori (id, navn) VALUES ('cc4912ef-e2ed-4460-9c50-39caffde79de', 'Barne-utstyr og leker');
INSERT INTO kategori (id, navn) VALUES ('24a6786f-278d-4215-916a-dc467f6c828b', 'Byggevarer og materialer');
INSERT INTO kategori (id, navn) VALUES ('33812d39-75e9-4ba9-875f-b0724aa68185', 'Bøker');
INSERT INTO kategori (id, navn) VALUES ('35a95968-444c-45a8-93f2-e413295d4af1', 'Dekorasjon/interiør');
INSERT INTO kategori (id, navn) VALUES ('0ae54ff9-3d30-46ad-a747-0cbcf7c2710e', 'Filmer, musikk og dataspill');
INSERT INTO kategori (id, navn) VALUES ('b672bb2f-055c-4ad0-aea3-4edc190a28b5', 'Hage og uterom');
INSERT INTO kategori (id, navn) VALUES ('ef6ce0d0-5efc-442d-b18a-1fa27f065b15', 'Kjøkkenutstyr, kjøkkenredskap og dekketøy');
INSERT INTO kategori (id, navn) VALUES ('7febf1d5-35d7-4065-9d2c-4a307c154845', 'Små møbler');
INSERT INTO kategori (id, navn) VALUES ('e19d6892-d3e7-4699-9d2d-05407dfaaf7d', 'Store møbler (sofaer, vitrineskap, spisebord)');
INSERT INTO kategori (id, navn) VALUES ('b1c6de98-4a11-4526-8298-f16886e5b552', 'Sykler');
INSERT INTO kategori (id, navn) VALUES ('0e3c0a22-e1ee-4a97-afc3-c0f8b2714e88', 'Verktøy');


create TABLE utlysning (
    id uuid default uuid_generate_v4() primary key,
    arkivert timestamp,
    partner_id uuid not null,
    henting_id uuid not null,
    partner_pameldt timestamp,
    stasjon_godkjent timestamp,
    partner_skjult boolean,
    partner_vist boolean,
    FOREIGN KEY (partner_id) references partner,
    FOREIGN KEY (henting_id) references ekstra_henting
);