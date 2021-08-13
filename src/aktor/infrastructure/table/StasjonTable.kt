package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable

object StasjonTable : ArchivableUUIDTable("stasjon") {
    val navn = varchar("navn", 255)
    val type = varchar("type", 255)
}