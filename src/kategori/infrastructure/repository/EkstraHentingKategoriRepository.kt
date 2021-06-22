package ombruk.backend.kategori.infrastructure.repository

import arrow.core.left
import arrow.core.rightIfNotNull
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.kategori.domain.entity.EkstraHentingKategori
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriCreateParams
import ombruk.backend.kategori.domain.params.EkstraHentingKategoriFindParams
import ombruk.backend.kategori.domain.port.IEkstraHentingKategoriRepository
import ombruk.backend.kategori.infrastructure.table.EkstraHentingKategoriTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class EkstraHentingKategoriRepository : RepositoryBase<EkstraHentingKategori, EkstraHentingKategoriCreateParams, Nothing, EkstraHentingKategoriFindParams>(),
    IEkstraHentingKategoriRepository {
    override val table = EkstraHentingKategoriTable

    override fun insertQuery(params: EkstraHentingKategoriCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[ekstraHentingId] = params.ekstraHentingId
            it[kategoriId] = params.kategoriId
            it[mengde] = params.mengde ?: 0.0f
        }
    }

    override fun prepareQuery(params: EkstraHentingKategoriFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.ekstraHentingId?.let { query.andWhere { table.ekstraHentingId eq it } }
        params.kategoriId?.let { query.andWhere { table.kategoriId eq it } }
        params.mengde?.let { query.andWhere { table.mengde eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): EkstraHentingKategori {
        return EkstraHentingKategori(
            row[table.id].value,
            row[table.ekstraHentingId],
            row[table.kategoriId],
            null,
            row[table.mengde]
        )
    }

    override fun updateQuery(params: Nothing): Int {
        TODO("Not yet implemented")
    }

    override fun archiveCondition(params: EkstraHentingKategoriFindParams): Op<Boolean>? {
        return Op.TRUE
            .andIfNotNull(params.id){table.id eq params.id}
            .andIfNotNull(params.ekstraHentingId){table.ekstraHentingId eq params.ekstraHentingId!!}
            .andIfNotNull(params.kategoriId){table.kategoriId eq params.kategoriId!!}
    }
}