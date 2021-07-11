package ombruk.backend.henting.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.shared.database.ArchivableUUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object EkstraHentingTable: ArchivableUUIDTable("ekstra_henting") {
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val beskrivelse = text("beskrivelse")
    val stasjonId = uuid("stasjon_id").references(StasjonTable.id)
}