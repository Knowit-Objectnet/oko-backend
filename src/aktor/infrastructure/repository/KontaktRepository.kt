package ombruk.backend.aktor.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.*
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.infrastructure.table.KontaktTable
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class KontaktRepository : RepositoryBase<Kontakt, KontaktCreateParams, KontaktUpdateParams, KontaktFindParams>(),
    IKontaktRepository {
    override val table = KontaktTable

    override fun insertQuery(params: KontaktCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[aktorId] = params.aktorId
            it[navn] = params.navn
            it[telefon] = params.telefon?.ifBlank { null }
            it[epost] = params.epost?.ifBlank { null }
            it[rolle] = params.rolle
        }
    }

    override fun prepareQuery(params: KontaktFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.aktorId?.let { query.andWhere { table.aktorId eq it }}
        params.navn?.let { query.andWhere { table.navn eq it } }
        params.telefon?.let { query.andWhere { table.telefon eq it } }
        params.epost?.let { query.andWhere { table.epost eq it } }
        params.rolle?.let { query.andWhere { table.rolle eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Kontakt {
        return Kontakt(
            row[KontaktTable.id].value,
            row[KontaktTable.aktorId],
            row[KontaktTable.navn],
            row[KontaktTable.telefon],
            row[KontaktTable.epost],
            row[KontaktTable.rolle]
        )
    }

    override fun updateQuery(params: KontaktUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.navn?.let { row[navn] = it }
            params.telefon?.let { row[telefon] = it.ifBlank { null } }
            params.epost?.let { row[epost] = it.ifBlank { null } }
            params.rolle?.let { row[rolle] = it }
        }
    }
}