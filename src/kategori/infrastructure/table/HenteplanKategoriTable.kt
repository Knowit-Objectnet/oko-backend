package ombruk.backend.kategori.infrastructure.table

import ombruk.backend.henting.infrastructure.table.HenteplanTable
import org.jetbrains.exposed.dao.id.UUIDTable

object HenteplanKategoriTable : UUIDTable("henteplan_kategori") {
    val henteplanId =  uuid("henteplan_id").references(HenteplanTable.id)
    val kategoriId =  uuid("kategori_id").references(KategoriTable.id)
    val merknad = varchar("merknad", 255)
}