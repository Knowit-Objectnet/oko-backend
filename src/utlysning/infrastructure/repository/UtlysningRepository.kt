package ombruk.backend.utlysning.infrastructure.repository

import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.UtlysningCreateParams
import ombruk.backend.utlysning.domain.params.UtlysningFindParams
import ombruk.backend.utlysning.domain.params.UtlysningUpdateParams
import ombruk.backend.utlysning.domain.port.IUtlysningRepository
import ombruk.backend.utlysning.infrastructure.table.UtlysningTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.util.*

class UtlysningRepository : RepositoryBase<Utlysning, UtlysningCreateParams, UtlysningUpdateParams, UtlysningFindParams>(),
    IUtlysningRepository {
    override fun insertQuery(params: UtlysningCreateParams): EntityID<UUID> {
        return table.insertAndGetId {
            it[partnerId] = params.partnerId
            it[hentingId] = params.hentingId
            it[partnerPameldt] = params.partnerPameldt
            it[stasjonGodkjent] = params.stasjonGodkjent
            it[partnerSkjult] = params.partnerSkjult
            it[partnerVist] = params.partnerVist
        }
    }

    override fun prepareQuery(params: UtlysningFindParams): Pair<Query, List<Alias<Table>>?> {
        val query = table.selectAll()
        params.id?.let { query.andWhere { table.id eq it } }
        params.partnerId?.let { query.andWhere { table.partnerId eq it } }
        params.hentingId?.let { query.andWhere { table.hentingId eq it } }
        params.partnerPameldt?.let { query.andWhere { table.partnerPameldt eq it } }
        params.stasjonGodkjent?.let { query.andWhere { table.stasjonGodkjent eq it } }
        params.partnerSkjult?.let { query.andWhere { table.partnerSkjult eq it } }
        params.partnerVist?.let { query.andWhere { table.partnerVist eq it } }
        return Pair(query, null)
    }

    override fun toEntity(row: ResultRow, aliases: List<Alias<Table>>?): Utlysning {
        return Utlysning(
            row[table.id].value,
            row[table.partnerId],
            row[table.hentingId],
            row[table.partnerPameldt],
            row[table.stasjonGodkjent],
            row[table.partnerSkjult],
            row[table.partnerVist]
        )
    }

    override fun updateQuery(params: UtlysningUpdateParams): Int {
        TODO("Not yet implemented")
    }

    override val table = UtlysningTable
}