package ombruk.backend.avtale.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.domain.enum.HenteplanFrekvens
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.date

object HenteplanTable: IntIdTable("avtale") {
    val avtaleId = integer("avtale_id").references(AvtaleTable.id)
    val stasjonId = integer("stasjon_id").references(StasjonTable.id)
    val frekvens = enumeration("frekvens", HenteplanFrekvens::class)
    //FIXME: Apparently no LocalTime equivalent
    val startTidspunkt = varchar("start_tidspunkt", 255)
    val sluttTidspunkt = varchar("slutt_tidspunkt", 255)
    val ukeDag = varchar("ukedag", 255) //Enum?
    val startDato = date("start_dato")
    val sluttDato = date("slutt_dato")
    val merknad = varchar("merknad", 255)
}