package ombruk.backend.henting.infrastructure.table

import ombruk.backend.shared.database.ArchivableUUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object PlanlagtHentingTable: ArchivableUUIDTable("planlagt_henting") {
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val merknad = text("merknad").nullable()
    val henteplanId = uuid("henteplan_id").references(HenteplanTable.id)
    val avlyst = datetime("avlyst").nullable()
    val aarsak = text("aarsak").nullable()
}