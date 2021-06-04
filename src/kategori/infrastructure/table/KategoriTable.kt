package ombruk.backend.kategori.infrastructure.table

import org.jetbrains.exposed.dao.id.UUIDTable

object KategoriTable : UUIDTable("kategori"){
    val navn = varchar("navn", 255)
}