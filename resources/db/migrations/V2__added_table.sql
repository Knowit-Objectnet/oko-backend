CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create TABLE ekstra_henting_kategori (
                                         id uuid default uuid_generate_v4() primary key,
                                         ekstrahenting_id uuid not null,
                                         kategori_id uuid not null,
                                         mengde float,
                                         arkivert timestamp
)