package ombruk.backend.aktor.infrastructure.table

import org.jetbrains.exposed.dao.id.IntIdTable

object KontaktBridge: IntIdTable("aktor_kontakt_bridge") {
    val aktorId = integer("aktor_id").references(PartnerTable.id).references(StasjonTable.id)
    val kontaktId = integer("kontakt_id").references(KontaktTable.id)
}