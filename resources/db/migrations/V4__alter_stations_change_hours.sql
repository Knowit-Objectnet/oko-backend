ALTER TABLE stations
    DROP COLUMN opening_time;

ALTER TABLE stations
    DROP COLUMN closing_time;

ALTER TABLE stations
    ADD hours varchar(400);