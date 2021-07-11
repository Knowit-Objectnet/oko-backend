package ombruk.backend.kategori.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable

object KategoriTable : ArchivableUUIDTable("kategori"){
    val navn = varchar("navn", 255)
    val vektkategori = bool("vektkategori")
}