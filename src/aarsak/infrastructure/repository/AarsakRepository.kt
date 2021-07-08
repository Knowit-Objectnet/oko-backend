package ombruk.backend.aarsak.infrastructure.repository

import ombruk.backend.aarsak.domain.entity.Aarsak
import ombruk.backend.aarsak.domain.enum.AarsakType
import ombruk.backend.aarsak.domain.model.AarsakCreateParams
import ombruk.backend.aarsak.domain.model.AarsakFindParams
import ombruk.backend.aarsak.domain.model.AarsakUpdateParams
import ombruk.backend.aarsak.domain.port.IAarsakRepository
import ombruk.backend.aarsak.infrastructure.table.AarsakTable
import ombruk.backend.aktor.domain.model.*
import ombruk.backend.aktor.infrastructure.table.KontaktTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class AarsakRepository : RepositoryBase<Aarsak, AarsakCreateParams, AarsakUpdateParams, AarsakFindParams>(),
    IAarsakRepository {
    override val table = AarsakTable

    override fun insertQuery(params: AarsakCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            params.id?.let { paramId -> it[id] = paramId }
            it[beskrivelse] = params.beskrivelse
            it[type] = params.type.name
        }
    }

    override fun prepareQuery(params: AarsakFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.beskrivelse?.let { query.andWhere { table.beskrivelse eq it } }
        params.type?.let { query.andWhere { table.type eq it.name } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Aarsak {
        return Aarsak(
            row[table.id].value,
            row[table.beskrivelse],
            AarsakType.valueOf(row[AarsakTable.type])
        )
    }

    override fun updateQuery(params: AarsakUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.beskrivelse?.let { row[beskrivelse] = it }
            params.type?.let { row[type] = it.name }
        }
    }
}