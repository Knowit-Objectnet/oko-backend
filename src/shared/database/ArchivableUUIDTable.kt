package ombruk.backend.shared.database

import ombruk.backend.henting.infrastructure.table.PlanlagtHentingTable.nullable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

abstract class ArchivableUUIDTable(name:String, columnName: String = "id"): UUIDTable(name, columnName) {
    val arkivert = datetime("arkivert").nullable()
}