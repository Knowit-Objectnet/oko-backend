package ombruk.backend.aarsak.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable

object AarsakTable: ArchivableUUIDTable("aarsak") {
    val beskrivelse = varchar("beskrivelse", 255)
    val type = varchar("type", 255)
}