ALTER TABLE events
    ALTER column partner_id DROP not null;

ALTER TABLE reports
    ALTER column partner_id DROP not null;
