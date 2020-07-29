create TABLE stations
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

create TABLE events
(
    id                 serial primary key,
    start_date_time    timestamp not null,
    end_date_time      timestamp not null,
    recurrence_rule_id int,
    station_id         int not null,
    partner_id         int not null,
    FOREIGN KEY (recurrence_rule_id) references recurrence_rules on delete cascade,
    FOREIGN KEY (station_id) references stations on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade
);

create TABLE pickups
(
    id         serial primary key,
    start_time timestamp not null,
    end_time   timestamp not null,
    station_id int not null,
    FOREIGN KEY (station_id) references stations on delete cascade
);

create TABLE requests
(
    partner_id int not null,
    pickup_id  int not null,
    FOREIGN KEY (pickup_id) references pickups on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade

);

create TABLE reports
(
    id                serial primary key,
    event_id          int not null,
    weight            int not null,
    created_date_time timestamp not null,
    start_date_time   timestamp not null,
    end_date_time     timestamp not null,
    partner_id        int not null,
    station_id        int not null,
    FOREIGN KEY (station_id) references stations on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade
);