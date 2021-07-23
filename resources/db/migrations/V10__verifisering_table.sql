create TABLE verifisering (
    id uuid references kontakt(id),
    telefon_kode varchar(6),
    telefon_verifisert boolean,
    epost_kode varchar(6),
    epost_verifisert boolean,
    arkivert timestamp
);