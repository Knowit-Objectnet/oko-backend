package ombruk.backend.avtale.infrastructure.repository

import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.model.AvtaleCreateParams
import ombruk.backend.avtale.domain.model.AvtaleFindParams
import ombruk.backend.avtale.domain.model.AvtaleUpdateParams
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

class AvtaleRepository : RepositoryBase<Avtale, AvtaleCreateParams, AvtaleUpdateParams, AvtaleFindParams>(),
    IAvtaleRepository {
    override fun insertQuery(params: AvtaleCreateParams): EntityID<Int> {
        return table.insertAndGetId {
            it[aktorId] = params.aktorId
            it[type] = params.type
        }
    }

    override fun updateQuery(params: AvtaleUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.aktorId?.let { row[aktorId] = it }
            params.type?.let { row[type] = it }
        }
    }

    override fun prepareQuery(params: AvtaleFindParams): Query {
        val query = table.selectAll()
        params.aktorId?.let { query.andWhere { table.aktorId eq it } }
        params.type?.let { query.andWhere { table.type eq it } }
        return query
    }

    override fun toEntity(row: ResultRow): Avtale {
        return Avtale(
            row[AvtaleTable.id].value,
            row[AvtaleTable.aktorId], //FIXME: Does it need .value?
            row[AvtaleTable.type]
        )
    }

    override val table = AvtaleTable

}