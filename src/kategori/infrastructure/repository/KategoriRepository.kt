package ombruk.backend.kategori.infrastructure.repository

import arrow.core.Either
import ombruk.backend.aktor.infrastructure.table.KontaktTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.kategori.domain.params.KategoriUpdateParams
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.kategori.infrastructure.table.KategoriTable
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class KategoriRepository : RepositoryBase<Kategori, KategoriCreateParams, KategoriUpdateParams, KategoriFindParams>(), IKategoriRepository {
    override fun insertQuery(params: KategoriCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[navn] = params.navn
        }
    }

    override fun prepareQuery(params: KategoriFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.navn?.let { query.andWhere { table.navn eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Kategori {
        return Kategori(
            row[table.id].value,
            row[table.navn]
        )
    }

    override fun updateQuery(params: KategoriUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.navn?.let { row[KontaktTable.navn] = it }
        }
    }

    override val table = KategoriTable
}