-- Soft Delete functionality taken from https://stackoverflow.com/a/53046345
CREATE OR REPLACE FUNCTION archive_record()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE' AND NEW.slettet_tidspunkt IS NOT NULL) THEN
        EXECUTE format('DELETE FROM %I.%I WHERE id = $1', TG_TABLE_SCHEMA, TG_TABLE_NAME) USING OLD.id;
        RETURN OLD;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        IF (OLD.slettet_tidspunkt IS NULL) THEN
            OLD.slettet_tidspunkt := now();
        END IF;
        EXECUTE format('INSERT INTO %I.%I SELECT $1.*'
                    , TG_TABLE_SCHEMA, TG_TABLE_NAME || '_archive')
        USING OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER soft_delete_samarbeidspartnere
    AFTER
        UPDATE OF slettet_tidspunkt
        OR DELETE
    ON samarbeidspartnere
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_stasjoner
    AFTER
        UPDATE OF slettet_tidspunkt
        OR DELETE
    ON stasjoner
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();


CREATE TRIGGER soft_delete_gjentakelsesregler
    AFTER
        UPDATE OF slettet_tidspunkt
        OR DELETE
    ON gjentakelsesregler
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_uttak
    AFTER
        UPDATE OF slettet_tidspunkt
        OR DELETE
    ON uttak
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_uttaksdata
    AFTER
        UPDATE OF slettet_tidspunkt
        OR DELETE
    ON uttaksdata
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();

CREATE TRIGGER soft_delete_uttaksforesporsel
    AFTER
        UPDATE OF slettet_tidspunkt
        OR DELETE
    ON uttaksforesporsel
    FOR EACH ROW
    EXECUTE PROCEDURE archive_record();