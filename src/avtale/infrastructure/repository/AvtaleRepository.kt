package ombruk.backend.avtale.infrastructure.repository

import ombruk.backend.avtale.domain.entity.Avtale
import ombruk.backend.avtale.domain.params.AvtaleCreateParams
import ombruk.backend.avtale.domain.params.AvtaleFindParams
import ombruk.backend.avtale.domain.params.AvtaleUpdateParams
import ombruk.backend.avtale.domain.port.IAvtaleRepository
import ombruk.backend.avtale.infrastructure.table.AvtaleTable
import ombruk.backend.avtale.model.AvtaleType
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class AvtaleRepository : RepositoryBase<Avtale, AvtaleCreateParams, AvtaleUpdateParams, AvtaleFindParams>(), IAvtaleRepository {
    override fun insertQuery(params: AvtaleCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[aktorId] = params.aktorId
            it[type] = params.type.name
            it[startDato] = params.startDato
            it[sluttDato] = params.sluttDato
            it[saksnummer] = params.saksnummer
        }
    }

    override fun updateQuery(params: AvtaleUpdateParams): Int {
        return table.update( { table.id eq params.id }) { row ->
            params.type?.let { row[type] = it.name }
            params.startDato?.let { row[startDato] = it }
            params.sluttDato?.let { row[sluttDato] = it }
            params.saksnummer?.let { row[saksnummer] = it }
        }
    }

    override fun prepareQuery(params: AvtaleFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.aktorId?.let { query.andWhere { table.aktorId eq it } }
        params.type?.let { query.andWhere { table.type eq it.name } }
        params.startDato?.let {query.andWhere { table.startDato eq it }}
        params.sluttDato?.let {query.andWhere { table.sluttDato eq it }}
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Avtale {
        return Avtale(
            row[table.id].value,
            row[table.aktorId],
            AvtaleType.valueOf(row[table.type]),
            row[table.startDato],
            row[table.sluttDato],
            emptyList(),
            row[table.saksnummer]
        )
    }

    override val table = AvtaleTable
}