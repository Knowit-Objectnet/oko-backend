package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.aktor.domain.enum.PartnerStorrelse
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable

object PartnerTable : UUIDTable("partner") {
    val navn = varchar("navn", 255)
    val storrelse = enumeration("partner_storrelse", PartnerStorrelse::class)
    val ideell = bool("ideell")
}