package ombruk.backend.henting.infrastructure

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.henting.domain.model.HenteplanFrekvens
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object HenteplanTable: IntIdTable("avtale") {
    val avtaleId = integer("avtale_id").references(AvtaleTable.id)
    val stasjonId = integer("stasjon_id").references(StasjonTable.id)
    val frekvens = enumeration("frekvens", HenteplanFrekvens::class)
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val ukedag = integer("ukedag")
    val merknad = varchar("merknad", 255).nullable()
}