package ombruk.backend.utlysning.infrastructure.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.`java-time`.timestamp

object UtlysningTable : UUIDTable("utlysning"){
    val partnerId = uuid("partner_id")
    val hentingId = uuid("henting_id")
    val partnerPameldt = datetime("partner_pameldt").nullable()
    val stasjonGodkjent = datetime("stasjon_godkjent").nullable()
    val partnerSkjult = bool("partner_skjult")
    val partnerVist = bool("partner_vist")
}