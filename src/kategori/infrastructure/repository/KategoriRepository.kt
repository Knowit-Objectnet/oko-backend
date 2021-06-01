package ombruk.backend.kategori.infrastructure.repository

import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.kategori.domain.entity.Kategori
import ombruk.backend.kategori.domain.params.KategoriCreateParams
import ombruk.backend.kategori.domain.params.KategoriFindParams
import ombruk.backend.kategori.domain.port.IKategoriRepository
import ombruk.backend.kategori.infrastructure.table.KategoriTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class KategoriRepository : RepositoryBase<Kategori, KategoriCreateParams, Nothing, KategoriFindParams>(), IKategoriRepository {
    override fun insertQuery(params: KategoriCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[navn] = params.navn
        }
    }

    override fun prepareQuery(params: KategoriFindParams): Query {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.navn?.let { query.andWhere { table.navn eq it } }
        return query
    }

    override fun toEntity(row: ResultRow): Kategori {
        return Kategori(
            row[table.id].value,
            row[table.navn]
        )
    }

    override fun updateQuery(params: Nothing): Int {
        TODO("Not yet implemented")
    }

    override val table = KategoriTable
}