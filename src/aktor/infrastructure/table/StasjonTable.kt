package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.aktor.domain.enum.StasjonType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable

object StasjonTable : UUIDTable("stasjon") {
    val navn = varchar("navn", 255)
    val type = enumeration("type", StasjonType::class)
    val ideell = bool("ideell")
}