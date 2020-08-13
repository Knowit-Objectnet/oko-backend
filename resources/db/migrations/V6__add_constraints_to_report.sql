ALTER TABLE reports
    ADD CONSTRAINT event_id_fk FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE;

ALTER TABLE reports
    ADD CHECK (weight > 0);