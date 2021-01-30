CREATE TABLE samarbeidspartnere_archive (
    CHECK (slettet_tidspunkt IS NOT NULL)
) INHERITS(samarbeidspartnere);

CREATE TABLE stasjoner_archive (
    CHECK (slettet_tidspunkt IS NOT NULL)
) INHERITS(stasjoner);

CREATE TABLE gjentakelsesregler_archive (
    CHECK (slettet_tidspunkt IS NOT NULL)
) INHERITS(gjentakelsesregler);

CREATE TABLE uttak_archive (
    CHECK (slettet_tidspunkt IS NOT NULL)
) INHERITS(uttak);

CREATE TABLE uttaksdata_archive (
    CHECK (slettet_tidspunkt IS NOT NULL)
) INHERITS(uttaksdata);

CREATE TABLE uttaksforesporsel_archive (
    CHECK (slettet_tidspunkt IS NOT NULL)
) INHERITS(uttaksforesporsel);