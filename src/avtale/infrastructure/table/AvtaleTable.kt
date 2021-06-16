package ombruk.backend.avtale.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.shared.database.ArchivableUUIDTable
import org.jetbrains.exposed.sql.`java-time`.date

object AvtaleTable : ArchivableUUIDTable("avtale"){
    val aktorId =  uuid("aktor_id").references(PartnerTable.id).references(StasjonTable.id)
    val type = varchar("type", 255)
    val startDato = date("start_dato")
    val sluttDato = date("slutt_dato")
}