package ombruk.backend.aktor.infrastructure.repository

import ombruk.backend.aktor.domain.entity.Verifisering
import ombruk.backend.aktor.domain.model.*
import ombruk.backend.aktor.domain.port.IVerifiseringRepository
import ombruk.backend.aktor.infrastructure.table.VerifiseringTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class VerifiseringRepository : RepositoryBase<Verifisering, VerifiseringCreateParams, VerifiseringUpdateParams, VerifiseringFindParams>(),
    IVerifiseringRepository {
    override val table: VerifiseringTable = VerifiseringTable

    override fun insertQuery(params: VerifiseringCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[id] = params.id
            it[telefonKode] = params.telefonKode?.ifBlank { null }
            it[telefonVerifisert] = false
            it[epostKode] = params.epostKode?.ifBlank { null }
            it[epostVerifisert] = false
        }
    }

    override fun prepareQuery(params: VerifiseringFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        VerifiseringTable.id.let { query.andWhere { table.id eq it }}
        params.telefonKode?.let { query.andWhere { table.telefonKode eq it } }
        params.telefonVerifisert.let { query.andWhere { table.telefonVerifisert eq it } }
        params.epostKode?.let { query.andWhere { table.epostKode eq it } }
        params.epostVerifisert.let { query.andWhere { table.epostVerifisert eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Verifisering {
        return Verifisering(
            row[table.id].value,
            row[table.telefonKode],
            row[table.telefonVerifisert],
            row[table.epostKode],
            row[table.epostVerifisert]
        )
    }

    override fun updateQuery(params: VerifiseringUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.telefonKode?.let { row[table.telefonKode] = it }
            params.telefonVerifisert?.let { row[table.telefonVerifisert] = it }
            params.epostKode?.let { row[table.epostKode] = it }
            params.epostVerifisert?.let { row[table.epostVerifisert] = it }
        }
    }
}