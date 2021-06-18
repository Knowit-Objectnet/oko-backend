package ombruk.backend.kategori.infrastructure.table

import ombruk.backend.henting.infrastructure.table.EkstraHentingTable
import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.shared.database.ArchivableUUIDTable

object EkstraHentingKategoriTable : ArchivableUUIDTable("ekstra_henting_kategori"){
    val ekstraHentingId = uuid("ekstrahenting_id").references(EkstraHentingTable.id)
    val kategoriId = uuid("kategori_id").references(KategoriTable.id)
    val mengde = float("mengde")
}