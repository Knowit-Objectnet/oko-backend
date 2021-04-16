package ombruk.backend.aktor.infrastructure.table

import ombruk.backend.aktor.infrastructure.repository.table.PartnerTable
import org.jetbrains.exposed.dao.id.IntIdTable

object StasjonKontaktPersonBridge :IntIdTable("stasjon_kontakt_person_bridge"){
    val partnerId = PartnerKontaktPersonBridge.integer("partner_id").references(PartnerTable.id)
    val kontaktPersonid = PartnerKontaktPersonBridge.integer("partner_id").references(KontaktPersonTable.id)
}