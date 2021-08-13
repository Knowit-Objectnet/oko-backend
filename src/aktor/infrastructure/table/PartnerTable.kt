package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable

object PartnerTable : ArchivableUUIDTable("partner") {
    val navn = varchar("navn", 255)
    val ideell = bool("ideell")
}