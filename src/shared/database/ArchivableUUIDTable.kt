package ombruk.backend.shared.database

import org.jetbrains.exposed.dao.id.UUIDTable

abstract class ArchivableUUIDTable(name:String, columnName: String = "id"): UUIDTable(name, columnName) {
    val arkivert = bool("arkivert")
}