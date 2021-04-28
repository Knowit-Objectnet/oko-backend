package ombruk.backend.aktor.infrastructure.repository

import ombruk.backend.aktor.domain.entity.Stasjon
import ombruk.backend.aktor.domain.model.StasjonCreateParams
import ombruk.backend.aktor.domain.model.StasjonFindParams
import ombruk.backend.aktor.domain.model.StasjonUpdateParams
import ombruk.backend.aktor.domain.port.IStasjonRepository
import ombruk.backend.aktor.infrastructure.table.StasjonTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

class StasjonRepository : RepositoryBase<Stasjon, StasjonCreateParams, StasjonUpdateParams, StasjonFindParams>(),
    IStasjonRepository {
    override fun insertQuery(params: StasjonCreateParams): EntityID<Int> {
        return table.insertAndGetId {
            it[navn] = params.navn
            it[type] = params.type
        }
    }

    override fun prepareQuery(params: StasjonFindParams): Query {
        val query = table.selectAll()
        params.navn?.let { query.andWhere { table.navn eq it } }
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
    override fun updateQuery(params: StasjonUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.navn?.let { row[navn] = it }
            params.type?.let { row[type] = it }
        }
    }

}