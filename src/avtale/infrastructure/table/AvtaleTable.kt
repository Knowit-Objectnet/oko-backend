package ombruk.backend.avtale.infrastructure.table

import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.avtale.model.AvtaleType
import org.jetbrains.exposed.dao.id.IntIdTable

object AvtaleTable : IntIdTable("avtale"){
    val aktorId =  integer("aktor_id").references(PartnerTable.id)
    val type = enumeration("type", AvtaleType::class)
}