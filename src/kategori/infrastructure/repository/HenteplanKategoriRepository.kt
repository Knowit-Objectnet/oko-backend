package ombruk.backend.kategori.infrastructure.repository

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
        }
    }

    override fun prepareQuery(params: HenteplanKategoriFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        //params.henteplanId?.let { query.andWhere { table.navn eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): HenteplanKategori {
        return HenteplanKategori(
            row[table.id].value,
            row[table.henteplanId],
            row[table.kategoriId],
            row[table.merknad]
        )
    }

    override fun updateQuery(params: Nothing): Int {
        TODO("Not yet implemented")
    }
}