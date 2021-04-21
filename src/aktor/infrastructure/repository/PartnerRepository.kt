package ombruk.backend.aktor.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.domain.entity.Partner
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.PartnerFindParams
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.aktor.domain.port.IPartnerRepository
import ombruk.backend.aktor.infrastructure.table.PartnerTable
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PartnerRepository : IPartnerRepository {
    override fun save(params: PartnerCreateParams): Either<RepositoryError, Partner> = transaction {
        runCatching {
            PartnerTable.insertAndGetId {
                it[navn] = params.navn
                it[storrelse] = params.storrelse
                it[ideell] = params.ideell
            }
        }
    }.fold(
        { findOne(it.value) },
        { RepositoryError.InsertError("SQL error").left() }
    )

    override fun update(params: PartnerUpdateParams): Either<RepositoryError, Partner> = runCatching {
        transaction {
            PartnerTable.update({ PartnerTable.id eq params.id })
            { row ->
                params.navn?.let { row[navn] = it }
                params.storrelse?.let { row[storrelse] = it }
                params.ideell?.let { row[ideell] = it }
            }
        }
    }
        .fold(
            {
                if (it > 0) {
                    findOne(params.id)
                }
                else {
                    RepositoryError.NoRowsFound("${params.id} not found").left()
                }
            },
            { RepositoryError.UpdateError(it.message).left() }
        )

    val test: Int? = null

    override fun delete(id: Int): Either<RepositoryError, Unit> = runCatching {
        PartnerTable.deleteWhere { PartnerTable.id eq id }
    }.fold(
        { Unit.right() },
        { RepositoryError.DeleteError(it.message).left() }
    )

    override fun findOne(id: Int): Either<RepositoryError, Partner> {
        return runCatching {
            PartnerTable.select { PartnerTable.id eq id }.mapNotNull { toPartner(it) }
        }.fold(
            {
                if (it.isNotEmpty()) it.first().right() else RepositoryError.NoRowsFound("$id not found").left()
            },
            { RepositoryError.InsertError("SQL error").left() }
        )
    }

    override fun find(params: PartnerFindParams): Either<RepositoryError, List<Partner>> = runCatching {
        transaction {
            val query = (PartnerTable).selectAll()
            params.navn?.let { query.andWhere { PartnerTable.navn eq it } }
            params.ideell?.let { query.andWhere { PartnerTable.ideell eq it } }
            params.storrelse?.let { query.andWhere { PartnerTable.storrelse eq it } }
            query.mapNotNull { toPartner(it) }
        }
    }.fold(
        { it.right() },
        { RepositoryError.SelectError(it.message).left() }
    )

}

fun toPartner(row: ResultRow): Partner? {
    if (!row.hasValue(PartnerTable.id) || row.getOrNull(
            PartnerTable.id
        ) == null
    ) {
        return null
    }

    return Partner(
        row[PartnerTable.id].value,
        row[PartnerTable.navn],
        emptyList(),
        row[PartnerTable.storrelse],
        row[PartnerTable.ideell]
    )
}