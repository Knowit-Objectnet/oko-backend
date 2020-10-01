ALTER table pickups
    ADD description text;

ALTER table pickups
    ADD chosen_partner_id INTEGER references partners;