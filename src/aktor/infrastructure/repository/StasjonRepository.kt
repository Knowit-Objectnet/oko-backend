package ombruk.backend.aktor.infrastructure.repository

import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.core.db.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

class StasjonRepository : RepositoryBase<Stasjon, StasjonCreateParams, StasjonFindParams>() {
    override fun insertQuery(params: StasjonCreateParams): EntityID<Int> {
        return table.insertAndGetId {
            it[navn] = params.navn
            it[type] = params.type
        }
    }

    override fun prepareQuery(params: StasjonFindParams): Query {
        val query = table.selectAll()
        params.navn?.let{query.andWhere { table.navn eq it }}
        params.type?.let { query.andWhere { table.type eq it } }
        return query
    }

    override fun toEntity(row: ResultRow): Stasjon {
        return Stasjon(
            row[StasjonTable.id].value,
            row[StasjonTable.navn],
            emptyList(),
            row[StasjonTable.type]
        )
    }

    override val table = StasjonTable

}