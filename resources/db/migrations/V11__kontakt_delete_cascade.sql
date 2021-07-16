ALTER TABLE verifisering
DROP CONSTRAINT verifisering_id_fkey,
ADD CONSTRAINT verifisering_id_fkey
    FOREIGN KEY (id)
    REFERENCES kontakt(id)
    ON DELETE CASCADE;