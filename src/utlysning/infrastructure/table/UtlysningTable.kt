package ombruk.backend.utlysning.infrastructure.table

import org.jetbrains.exposed.dao.id.UUIDTable

object UtlysningTable : UUIDTable("utlysning"){
    val partnerId = uuid("partner_id")
    val hentingId = uuid("henting_id")
    val partnerPameldt = bool("partner_pameldt")
    val stasjonGodkjent = bool("stasjon_godkjent")
    val partnerSkjult = bool("partner_skjult")
    val partnerVist = bool("partner_vist")
}