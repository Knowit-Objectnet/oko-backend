create TABLE stations
(
    id   serial primary key,
    name varchar(200)
);

create TABLE partners
(
    id   serial primary key,
    name varchar(200),
    description varchar(100),
    phone varchar(20),
    email varchar(30)
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
    start_date_time    timestamp,
    end_date_time      timestamp,
    recurrence_rule_id int,
    station_id         int,
    partner_id         int,
    FOREIGN KEY (recurrence_rule_id) references recurrence_rules on delete cascade,
    FOREIGN KEY (station_id) references stations on delete cascade,
    FOREIGN KEY (partner_id) references partners on delete cascade
);

create TABLE pickups
(
    id         serial primary key,
    start_time timestamp,
    end_time   timestamp,
    station_id int,
    FOREIGN KEY (station_id) references stations
);

create TABLE requests
(
    partner_id int,
    pickup_id  int,
    FOREIGN KEY (pickup_id) references pickups,
    FOREIGN KEY (partner_id) references partners

);

create TABLE reports
(
    id                serial primary key,
    event_id          int,
    weight            int,
    created_date_time timestamp,
    start_date_time   timestamp,
    end_date_time     timestamp,
    partner_id        int,
    station_id        int,
    FOREIGN KEY (station_id) references stations,
    FOREIGN KEY (partner_id) references partners
);