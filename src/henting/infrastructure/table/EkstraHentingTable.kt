package ombruk.backend.henting.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object EkstraHentingTable: UUIDTable("ekstra_henting") {
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val merknad = text("merknad").nullable()
    val stasjonId = uuid("stasjon_id").references(StasjonTable.id)
}