package ombruk.backend.kategori.infrastructure.table

import ombruk.backend.henting.infrastructure.table.HenteplanTable
import ombruk.backend.shared.database.ArchivableUUIDTable

object HenteplanKategoriTable : ArchivableUUIDTable("henteplan_kategori"){
    val henteplanId = uuid("henteplan_id").references(HenteplanTable.id)
    val kategoriId = uuid("kategori_id").references(KategoriTable.id)
}