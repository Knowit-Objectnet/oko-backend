-- Soft Delete functionality taken from https://stackoverflow.com/a/53046345
CREATE OR REPLACE FUNCTION archive_record()
RETURNS TRIGGER AS $$
BEGIN
    RAISE NOTICE 'Running trigger';
    EXECUTE format('INSERT INTO %I.%I SELECT $1.*', TG_TABLE_SCHEMA, (TG_TABLE_NAME || '_archive'))
    USING OLD;
    RETURN NULL;
END;
$$ LANGUAGE PLPGSQL;

CREATE FUNCTION moveDeleted() RETURNS trigger AS $$
    BEGIN
        INSERT INTO samarbeidspartnere_archive VALUES((OLD).*);
        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER soft_delete_samarbeidspartnere
    AFTER DELETE
    ON samarbeidspartnere
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_stasjoner
    AFTER DELETE
    ON stasjoner
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();


CREATE TRIGGER soft_delete_gjentakelsesregler
    AFTER DELETE
    ON gjentakelsesregler
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_uttak
    AFTER DELETE
    ON uttak
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_uttaksdata
    AFTER DELETE
    ON uttaksdata
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_uttaksforesporsel
    AFTER DELETE
    ON uttaksforesporsel
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();