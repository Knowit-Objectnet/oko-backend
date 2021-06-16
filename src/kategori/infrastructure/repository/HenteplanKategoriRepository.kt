package ombruk.backend.kategori.infrastructure.repository

import arrow.core.left
import arrow.core.rightIfNotNull
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.kategori.domain.entity.HenteplanKategori
import ombruk.backend.kategori.domain.params.HenteplanKategoriCreateParams
import ombruk.backend.kategori.domain.params.HenteplanKategoriFindParams
import ombruk.backend.kategori.domain.port.IHenteplanKategoriRepository
import ombruk.backend.kategori.infrastructure.table.HenteplanKategoriTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class HenteplanKategoriRepository : RepositoryBase<HenteplanKategori, HenteplanKategoriCreateParams, Nothing, HenteplanKategoriFindParams>(),
    IHenteplanKategoriRepository {
    override val table = HenteplanKategoriTable

    override fun insertQuery(params: HenteplanKategoriCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[henteplanId] = params.henteplanId
            it[kategoriId] = params.kategoriId
            it[merknad] = params.merknad ?: ""
        }
    }

    override fun prepareQuery(params: HenteplanKategoriFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.henteplanId?.let { query.andWhere { table.henteplanId eq it } }
        params.kategoriId?.let { query.andWhere { table.kategoriId eq it } }
        params.merknad?.let { query.andWhere { table.merknad eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): HenteplanKategori {
        return HenteplanKategori(
            row[table.id].value,
            row[table.henteplanId],
            row[table.kategoriId],
            null,
            row[table.merknad]
        )
    }

    override fun updateQuery(params: Nothing): Int {
        TODO("Not yet implemented")
    }

    override fun archiveCondition(params: HenteplanKategoriFindParams): Op<Boolean>? {
        return Op.TRUE
            .andIfNotNull(params.id){table.id eq params.id}
            .andIfNotNull(params.henteplanId){table.henteplanId eq params.henteplanId!!}
            .andIfNotNull(params.kategoriId){table.kategoriId eq params.kategoriId!!}
    }
}