package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.aktor.infrastructure.repository.table.PartnerTable
import org.jetbrains.exposed.dao.id.IntIdTable

object PartnerKontaktPersonBridge: IntIdTable("partner_kontakt_person_bridge") {
    val partnerId = integer("partner_id").references(PartnerTable.id)
    val kontaktPersonid = integer("partner_id").references(KontaktPersonTable.id)
}