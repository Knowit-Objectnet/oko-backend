package ombruk.backend.aktor.infrastructure.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable

object KontaktTable: UUIDTable("kontakt") {
    val navn = varchar("navn", 255)
    val telefon = varchar("telefon", 20)
    val rolle = varchar("rolle", 200)
}