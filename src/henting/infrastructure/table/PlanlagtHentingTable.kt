package ombruk.backend.henting.infrastructure.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object PlanlagtHentingTable: UUIDTable("planlagt_henting") {
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val merknad = text("merknad").nullable()
    val henteplanId = uuid("henteplan_id").references(HenteplanTable.id)
    val avlyst = datetime("avlyst").nullable()
}