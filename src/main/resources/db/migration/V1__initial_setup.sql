--create table uttakstype
--(
--    id      serial primary key,
--    type    varchar(64) not null unique
--);
--insert into uttakstype(type)
--    values
--           ('Enkelt'), ('Gjentakende'), ('Ekstra'), ('Ombruksdag');

create table samarbeidspartnere
(
    id                  serial primary key,
    navn                varchar(128) not null,
    beskrivelse         text not null,
    telefon             varchar(32) not null,
    epost               varchar(64) not null,
    endret_tidspunkt    timestamp not null,
    slettet_tidspunkt   timestamp
);

create table stasjoner
(
    id                  serial primary key,
    endret_tidspunkt    timestamp not null,
    slettet_tidspunkt   timestamp ,
    navn                varchar(128) not null,
    aapningstider       varchar(400) not null
);

create table gjentakelsesregler
(
    id                  serial primary key,
    antall              int,
    endret_tidspunkt    timestamp not null,
    slettet_tidspunkt   timestamp,
    slutt_tidspunkt     timestamp,
    dager               text not null,
    intervall           integer not null
);

create table uttak
(
    id                                   serial primary key,
    endret_tidspunkt                     timestamp not null,
    slettet_tidspunkt                    timestamp,
    -- Disabled for now - varchar works nicely with exposed's enumerationByName
    --type_id                              int not null,
    type                                 varchar(64),
    start_tidspunkt                      timestamp not null,
    slutt_tidspunkt                      timestamp not null,
    stasjon_id                           int not null,
    samarbeidspartner_id                 int,
    gjentakelsesregel_id                 int,
    beskrivelse                          text,
    --foreign key (type_id)                references uttakstype,
    foreign key (samarbeidspartner_id)   references samarbeidspartnere ON DELETE CASCADE,
    foreign key (stasjon_id)             references stasjoner ON DELETE CASCADE,
    foreign key (gjentakelsesregel_id)   references gjentakelsesregler ON DELETE CASCADE
);

create table uttaksdata
(
    uttak_id                int not null,
    vekt                    int,
    rapportert_tidspunkt    timestamp,
    slettet_tidspunkt       timestamp,
    check                   (vekt > 0),
    foreign key             (uttak_id) references uttak ON DELETE CASCADE
);

--create table uttaksforesporselstatus
--(
--    id      serial primary key,
--    status  varchar(64) not null unique
--);
--insert into uttaksforesporselstatus(status)
--    values
--        ('Avventer'), ('Godkjent'), ('Avvist');

create table uttaksforesporsel
(
    id                                  serial primary key,
    endret_tidspunkt                    timestamp not null,
    uttak_id                            int not null,
    partner_id                          int not null,
    slettet_tidspunkt                   timestamp,
    --status                              varchar(64),
    --status_id                           int not null,
    --foreign key (status_id)             references uttaksforesporselstatus,
    foreign key (uttak_id)              references uttak ON DELETE CASCADE,
    foreign key (partner_id)  references samarbeidspartnere ON DELETE CASCADE
);

