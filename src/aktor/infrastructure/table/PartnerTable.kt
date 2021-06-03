package ombruk.backend.aktor.infrastructure.table

import org.jetbrains.exposed.dao.id.UUIDTable

object PartnerTable : UUIDTable("partner") {
    val navn = varchar("navn", 255)
    val ideell = bool("ideell")
}