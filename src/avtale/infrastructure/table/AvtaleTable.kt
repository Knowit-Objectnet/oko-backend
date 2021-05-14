package ombruk.backend.avtale.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.avtale.model.AvtaleType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.date

object AvtaleTable : UUIDTable("avtale"){
    val aktorId =  uuid("aktor_id").references(PartnerTable.id).references(StasjonTable.id)
    val type = varchar("type", 255)
    val startDato = date("start_dato")
    val sluttDato = date("slutt_dato")
}