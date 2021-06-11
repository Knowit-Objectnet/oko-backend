package ombruk.backend.henting.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.shared.database.ArchivableUUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object HenteplanTable: ArchivableUUIDTable("henteplan") {
    val avtaleId = uuid("avtale_id").references(AvtaleTable.id)
    val stasjonId = uuid("stasjon_id").references(StasjonTable.id)
    val frekvens = varchar("frekvens", 255)
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val ukedag = integer("ukedag").nullable()
    val merknad = text("merknad").nullable()
}