package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable

object KontaktBridge: ArchivableUUIDTable("aktor_kontakt_bridge") {
    val aktorId = uuid("aktor_id").references(PartnerTable.id).references(StasjonTable.id)
    val kontaktId = uuid("kontakt_id").references(KontaktTable.id)
}