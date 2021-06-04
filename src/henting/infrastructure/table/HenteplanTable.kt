package ombruk.backend.henting.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object HenteplanTable: UUIDTable("henteplan") {
    val avtaleId = uuid("avtale_id").references(AvtaleTable.id)
    val stasjonId = uuid("stasjon_id").references(StasjonTable.id)
    val frekvens = varchar("frekvens", 255)
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val ukedag = integer("ukedag")
    val merknad = text("merknad").nullable()
}