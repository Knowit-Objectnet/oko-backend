package ombruk.backend.utlysning.infrastructure.repository

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import ombruk.backend.core.infrastructure.RepositoryBase
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.utlysning.domain.entity.Utlysning
import ombruk.backend.utlysning.domain.params.*
import ombruk.backend.utlysning.domain.port.IUtlysningRepository
import ombruk.backend.utlysning.infrastructure.table.UtlysningTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
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
        return table.update({table.id eq params.id}) { row ->
            params.partnerPameldt?.let { row[partnerPameldt] = it }
            params.stasjonGodkjent?.let { row[stasjonGodkjent] = it }
            params.partnerSkjult?.let { row[partnerSkjult] = it }
            params.partnerVist?.let { row[partnerVist] = it }
        }
    }

    override val table = UtlysningTable


    override fun acceptPartner(params: UtlysningPartnerAcceptParams): Either<RepositoryError, Utlysning> {

        var partnerPameldtValue: LocalDateTime? = null
        if (params.toAccept) partnerPameldtValue = LocalDateTime.now()

        return runCatching {
            table.update({table.id eq params.id}) { row ->
                 row[partnerPameldt] = partnerPameldtValue
            }
        }
            .onFailure { logger.error("Failed to update database; ${it.message}") }
            .fold(
                //Return right if more than 1 partner has been updated. Else, return an Error
                {
                    Either.cond(it > 0,
                        { findOne(params.id) },
                        { RepositoryError.NoRowsFound("${params.id} not found") })
                },
                { RepositoryError.UpdateError(it.message).left() }
            )
            .flatMap { it }
    }

    override fun acceptStasjon(params: UtlysningStasjonAcceptParams): Either<RepositoryError, Utlysning> {

        var stasjonGodkjentValue: LocalDateTime? = null
        if (params.toAccept) stasjonGodkjentValue = LocalDateTime.now()

        return runCatching {
            table.update({table.id eq params.id}) { row ->
                row[stasjonGodkjent] = stasjonGodkjentValue
            }
        }
            .onFailure { logger.error("Failed to update database; ${it.message}") }
            .fold(
                //Return right if more than 1 partner has been updated. Else, return an Error
                {
                    Either.cond(it > 0,
                        { findOne(params.id) },
                        { RepositoryError.NoRowsFound("${params.id} not found") })
                },
                { RepositoryError.UpdateError(it.message).left() }
            )
            .flatMap { it }
    }
}