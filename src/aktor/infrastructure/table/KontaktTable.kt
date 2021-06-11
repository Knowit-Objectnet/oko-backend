package ombruk.backend.aktor.infrastructure.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable

object KontaktTable: UUIDTable("kontakt") {
    val aktorId = uuid("aktor_id").references(PartnerTable.id).references(StasjonTable.id)
    val navn = varchar("navn", 255)
    val telefon = varchar("telefon", 20).nullable()
    val epost = varchar("epost", 255).nullable()
    val rolle = varchar("rolle", 200).nullable()
}