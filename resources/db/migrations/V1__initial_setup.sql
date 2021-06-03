CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create TABLE partner (
    id uuid default uuid_generate_v4() primary key,
    navn varchar(255) not null unique,
    ideell boolean
);

create TABLE stasjon (
    id uuid default uuid_generate_v4() primary key,
    navn varchar(255) not null,
    type varchar not null
);

INSERT INTO stasjon (id, navn, type) VALUES ('a393406b-2e0a-42e9-9666-1c3df5ea8a1c', 'test1', 'GJENBRUK');


create TABLE kontakt (
    id uuid default uuid_generate_v4() primary key,
    navn varchar(255) not null,
    telefon varchar(20) not null,
    rolle varchar(40)
);

create TABLE stasjon_kontakt_person (
    id uuid default uuid_generate_v4() primary key,
    stasjon_id uuid not null,
    navn varchar(255) not null,
    telefon varchar(20) not null,
    rolle varchar(50),

    FOREIGN KEY (stasjon_id) references stasjon on delete cascade
);

create TABLE partner_kontakt_person (
    id uuid default uuid_generate_v4() primary key,
    partner_id uuid not null,
    navn varchar(255) not null,
    telefon varchar(20) not null,
    rolle varchar(50),

    FOREIGN KEY (partner_id) references partner on delete cascade
);

create TABLE avtale (
    id uuid default uuid_generate_v4() primary key,
    aktor_id uuid not null,
    type varchar not null,
    start_dato date,
    slutt_dato date
);

create TABLE henteplan (
    id uuid default uuid_generate_v4() primary key,
    avtale_id uuid not null,
    stasjon_id uuid not null,
    frekvens varchar not null,
    start_tidspunkt timestamp not null,
    slutt_tidspunkt timestamp not null,
    ukedag int not null,
    merknad text,
    FOREIGN KEY (avtale_id) references avtale,
    FOREIGN KEY (stasjon_id) references stasjon
);

create TABLE planlagt_henting (
    id uuid default uuid_generate_v4() primary key,
    start_tidspunkt timestamp not null,
    slutt_tidspunkt timestamp not null,
    merknad text,
    henteplan_id uuid not null,
    avlyst timestamp,
    FOREIGN KEY (henteplan_id) references henteplan
);

create TABLE ekstra_henting (
    id uuid default uuid_generate_v4() primary key,
    start_tidspunkt timestamp not null,
    slutt_tidspunkt timestamp not null,
    merknad text,
    stasjon_id uuid not null,
    FOREIGN KEY (stasjon_id) references stasjon
);

create TABLE kategori (
    id uuid default uuid_generate_v4() primary key,
    navn varchar(255) not null
);

create TABLE utlysning (
    id uuid default uuid_generate_v4() primary key,
    partner_id uuid not null,
    henting_id uuid not null,
    partner_pameldt timestamp,
    stasjon_godkjent timestamp,
    partner_skjult boolean,
    partner_vist boolean,
    FOREIGN KEY (partner_id) references partner,
    FOREIGN KEY (henting_id) references ekstra_henting
);