package ombruk.backend.avtale.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.avtale.model.AvtaleType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.date

object AvtaleTable : UUIDTable("avtale"){
    val aktorId =  uuid("aktor_id")
    val type = enumeration("type", AvtaleType::class)
    val startDato = date("startDato")
    val sluttDato = date("sluttDato")
}