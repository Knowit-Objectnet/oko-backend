package ombruk.backend.aktor.infrastructure.repository

import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.*
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.infrastructure.table.KontaktTable
import ombruk.backend.core.infrastructure.RepositoryBase
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class KontaktRepository : RepositoryBase<Kontakt, KontaktCreateParams, KontaktUpdateParams, KontaktFindParams>(),
    IKontaktRepository {
    override val table = KontaktTable

    override fun insertQuery(params: KontaktCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[navn] = params.navn
            it[telefon] = params.telefon
            it[rolle] = params.rolle
        }
    }

    override fun prepareQuery(params: KontaktFindParams): Query {
        val query = table.selectAll()
        params.navn?.let { query.andWhere { table.navn eq it } }
        params.telefon?.let { query.andWhere { table.telefon eq it } }
        params.rolle?.let { query.andWhere { table.rolle eq it } }
        return query
    }

    override fun toEntity(row: ResultRow): Kontakt {
        return Kontakt(
            row[KontaktTable.id].value,
            row[KontaktTable.navn],
            row[KontaktTable.telefon],
            row[KontaktTable.rolle]
        )
    }

    override fun updateQuery(params: KontaktUpdateParams): Int {
        return table.update({ table.id eq params.id }) { row ->
            params.navn?.let { row[navn] = it }
            params.telefon?.let { row[telefon] = it }
            params.rolle?.let { row[rolle] = it }
        }
    }
}