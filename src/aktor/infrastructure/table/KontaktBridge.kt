package ombruk.backend.aktor.infrastructure.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable

object KontaktBridge: UUIDTable("aktor_kontakt_bridge") {
    val aktorId = uuid("aktor_id").references(PartnerTable.id).references(StasjonTable.id)
    val kontaktId = uuid("kontakt_id").references(KontaktTable.id)
}