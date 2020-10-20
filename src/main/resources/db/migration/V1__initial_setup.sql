create table uttakstype
(
    id      serial primary key,
    type    varchar(64) not null unique
);
insert into uttakstype(type)
    values
           ('Enkelt'), ('Gjentakende'), ('Ekstra'), ('Ombruksdag');

create table samarbeidspartnere
(
    id                  serial primary key,
    navn                varchar(128) not null,
    beskrivelse         text not null,
    telefon             varchar(32) not null,
    epost               varchar(64) not null,
    endret_tidspunkt    timestamp with time zone not null,
    slettet_tidspunkt   timestamp with time zone
);

create table stasjoner
(
    id                  serial primary key,
    endret_tidspunkt    timestamp with time zone not null,
    slettet_tidspunkt   timestamp with time zone,
    navn                varchar(128),
    aapningstid         text
);

create table gjentakelsesregler
(
    id serial primary key,
    antall int not null,
    endret_tidspunkt timestamp with time zone not null,
    slettet_tidspunkt timestamp with time zone,
    slutt_tidspunkt timestamp with time zone not null,
    dager text not null,
    intervall integer not null
);

create table uttak
(
    id                                   serial primary key,
    endret_tidspunkt                     timestamp with time zone not null,
    slettet_tidspunkt                    timestamp with time zone,
    type_id                              int not null,
    start_tidspunkt                      timestamp with time zone not null,
    slutt_tidspunkt                      timestamp with time zone not null,
    stasjon_id                           int not null,
    samarbeidspartner_id                 int,
    gjentakelsesregel_id                 int,
    foreign key (type_id)                references uttakstype,
    foreign key (samarbeidspartner_id)   references samarbeidspartnere,
    foreign key (stasjon_id)             references stasjoner,
    foreign key (gjentakelsesregel_id)   references gjentakelsesregler
);

create table uttaksdata
(
    id                      serial primary key,
    uttak_id                int not null,
    vekt                    int not null,
    rapportert_tidspunkt    timestamp with time zone,
    check                   (vekt > 0),
    foreign key             (uttak_id) references uttak
);

create table uttaksforesporselstatus
(
    id      serial primary key,
    status  varchar(64) not null unique
);
insert into uttaksforesporselstatus(status)
    values
        ('Avventer'), ('Godkjent'), ('Avvist');

create table uttaksforesporsel
(
    id                                  serial primary key,
    endret_tidspunkt                    timestamp with time zone not null,
    uttak_id                            int not null,
    samarbeidspartner_id                int not null,
    status_id                           int not null,
    foreign key (status_id)             references uttaksforesporselstatus,
    foreign key (uttak_id)              references uttak,
    foreign key (samarbeidspartner_id)  references samarbeidspartnere
);

