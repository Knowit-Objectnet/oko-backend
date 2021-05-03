package ombruk.backend.avtale.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.avtale.model.AvtaleType
import org.jetbrains.exposed.dao.id.IntIdTable

object AvtaleTable : IntIdTable("avtale"){
    val aktorId =  uuid("aktor_id")
    val type = enumeration("type", AvtaleType::class)
}