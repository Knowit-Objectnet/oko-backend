CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE vektregistrering (
    id uuid default uuid_generate_v4() primary key,
    henting_id uuid not null,
    kategori_id uuid not null,
    vekt float,
    registrerings_dato timestamp not null,
    arkivert timestamp,
    FOREIGN KEY (kategori_id) references kategori
);