create TABLE stasjoner
(
    id   serial primary key,
    name varchar(255) not null unique,
    hours varchar(400)
);

create TABLE partnere
(
    id   serial primary key,
    name varchar(255) not null unique,
    description text,
    phone varchar(20),
    email varchar(255)
);

create table gjentakelses_regels
(
    id         serial primary key,
    count      int,
    until      timestamp,
    days       varchar(50),
    "interval" integer not null default 1
);

create TABLE uttak
(
    id                 serial primary key,
    start_date_time    timestamp not null,
    end_date_time      timestamp not null,
    gjentakelses_regel_id int,
    stasjon_id         int not null,
    partner_id         int,
    description        text,
    type         varchar(64) not null,
    FOREIGN KEY (gjentakelses_regel_id) references gjentakelses_regels on delete cascade,
    FOREIGN KEY (stasjon_id) references stasjoner on delete cascade,
    FOREIGN KEY (partner_id) references partnere on delete cascade
);

create TABLE uttaksforesporsel
(
    uttak_id int not null,
    partner_id int not null,
    FOREIGN KEY (partner_id) references partnere on delete cascade,
    FOREIGN KEY (uttak_id) references uttak on delete cascade

);

create TABLE uttaksdata
(
    id                serial primary key,
    uttak_id          int not null,
    weight            int,
    modified_date_time timestamp,
    start_date_time   timestamp not null,
    end_date_time     timestamp not null,
    partner_id        int,
    stasjon_id        int not null,
    CHECK (weight > 0),
    FOREIGN KEY (stasjon_id) references stasjoner on delete cascade,
    FOREIGN KEY (partner_id) references partnere on delete cascade,
    FOREIGN KEY (uttak_id) references uttak on delete cascade
);
