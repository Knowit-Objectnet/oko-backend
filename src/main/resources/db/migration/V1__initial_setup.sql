create TABLE stasjoner
(
    id   serial primary key,
    name varchar(255) not null unique,
    opening_time varchar(20) not null,
    closing_time varchar(20) not null
);

create TABLE partners
(
    id   serial primary key,
    name varchar(255) not null unique,
    description text not null,
    phone varchar(20) not null,
    email varchar(255) not null
);

create table recurrence_rules
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
    recurrence_rule_id int,
    stasjon_id         int not null,
    partner_id         int not null,
    FOREIGN KEY (recurrence_rule_id) references recurrence_rules on delete cascade,
    FOREIGN KEY (stasjon_id) references stasjoner on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade
);

create TABLE pickups
(
    id         serial primary key,
    start_time timestamp not null,
    end_time   timestamp not null,
    stasjon_id int not null,
    FOREIGN KEY (stasjon_id) references stasjoner on delete cascade
);

create TABLE requests
(
    partner_id int not null,
    pickup_id  int not null,
    FOREIGN KEY (pickup_id) references pickups on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade

);

create TABLE uttaksdata
(
    id                serial primary key,
    uttak_id          int not null,
    weight            int,
    modified_date_time timestamp,
    start_date_time   timestamp not null,
    end_date_time     timestamp not null,
    partner_id        int not null,
    stasjon_id        int not null,
    FOREIGN KEY (stasjon_id) references stasjoner on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade
);

ALTER table pickups
    ADD description text;

ALTER table pickups
    ADD chosen_partner_id INTEGER references partners;
ALTER table partners
    ALTER column description DROP not null;

ALTER table partners
    ALTER column email DROP not null;

ALTER table partners
    ALTER column phone DROP not null;

ALTER TABLE stasjoner
    DROP COLUMN opening_time;

ALTER TABLE stasjoner
    DROP COLUMN closing_time;

ALTER TABLE stasjoner
    ADD hours varchar(400);

delete from stasjoner;

ALTER TABLE uttaksdata
    ADD CONSTRAINT uttak_id_fk FOREIGN KEY (uttak_id) REFERENCES uttak (id) ON DELETE CASCADE;

ALTER TABLE uttaksdata
    ADD CHECK (weight > 0);

ALTER TABLE uttak
    ALTER column partner_id DROP not null;

ALTER TABLE uttaksdata
    ALTER column partner_id DROP not null;
