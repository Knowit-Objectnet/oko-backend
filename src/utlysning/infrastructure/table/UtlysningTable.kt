package ombruk.backend.utlysning.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.henting.infrastructure.table.EkstraHentingTable
import ombruk.backend.shared.database.ArchivableUUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object UtlysningTable : ArchivableUUIDTable("utlysning"){
    val partnerId = uuid("partner_id").references(PartnerTable.id)
    val hentingId = uuid("henting_id").references(EkstraHentingTable.id)
    val partnerPameldt = datetime("partner_pameldt").nullable()
    val stasjonGodkjent = datetime("stasjon_godkjent").nullable()
    val partnerSkjult = bool("partner_skjult")
    val partnerVist = bool("partner_vist")
}