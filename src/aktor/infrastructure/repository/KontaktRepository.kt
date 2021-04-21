package ombruk.backend.aktor.infrastructure.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.aktor.domain.entity.Kontakt
import ombruk.backend.aktor.domain.model.KontaktFindParams
import ombruk.backend.aktor.domain.model.PartnerCreateParams
import ombruk.backend.aktor.domain.model.PartnerUpdateParams
import ombruk.backend.aktor.domain.port.IKontaktRepository
import ombruk.backend.aktor.infrastructure.repository.table.PartnerTable
import ombruk.backend.aktor.infrastructure.table.KontaktBridge
import ombruk.backend.aktor.infrastructure.table.KontaktTable
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class KontaktRepository : IKontaktRepository {
    override fun save(params: KontaktCreateParams): Either<RepositoryError, Kontakt> = transaction {
        runCatching {
            KontaktTable.insertAndGetId {
                it[navn] = params.navn
                it[telefon] = params.telefon
                it[rolle] = params.rolle
            }
        }
    }.fold(
        { findOne(it.value) },
        { RepositoryError.InsertError("SQL error").left() }
    )

    override fun update(params: KontaktUpdateParams): Either<RepositoryError, Kontakt> = runCatching {
        transaction {
            KontaktTable.update({ KontaktTable.id eq params.id })
            { row ->
                params.navn?.let { row[navn] = it }
                params.storrelse?.let { row[telefon] = it }
                params.ideell?.let { row[rolle] = it }
            }
        }
    }.fold(
        {
            if (it > 0)
                findOne(params.id)
            else
                RepositoryError.NoRowsFound("${params.id} not found").left()
        },
        { RepositoryError.UpdateError(it.message).left() }
    )


    override fun delete(id: Int): Either<RepositoryError, Unit> = runCatching {
        KontaktTable.deleteWhere { KontaktTable.id eq id }
    }.fold(
        { Unit.right() },
        { RepositoryError.DeleteError(it.message).left() }
    )

    override fun findOne(id: Int): Either<RepositoryError, Kontakt> = runCatching {
        KontaktTable.select { KontaktTable.id eq id }.mapNotNull { toKontakt(it) }
    }.fold(
        {
            if (it.isNotEmpty()) it.first().right() else RepositoryError.NoRowsFound("$id not found").left()
        },
        { RepositoryError.InsertError("SQL error").left() }
    )

    override fun find(params: KontaktFindParams): Either<RepositoryError, List<Kontakt>> = runCatching {
        transaction {
            val query = (KontaktTable).selectAll()
            params.navn?.let { query.andWhere { KontaktTable.navn eq it } }
            params.telefon?.let { query.andWhere { KontaktTable.telefon eq it } }
            params.rolle?.let { query.andWhere { KontaktTable.rolle eq it } }
            query.mapNotNull { toKontakt(it) }
        }
    }.fold(
        { it.right() },
        { RepositoryError.SelectError(it.message).left() }
    )

    override fun findForAktor(id: Int): Either<RepositoryError, List<Kontakt>> = runCatching {
        transaction {
            val query = (KontaktBridge).select { KontaktBridge.aktorId eq id }
            query.mapNotNull { toKontakt(it) }
        }
    }.fold(
        { it.right() },
        { RepositoryError.SelectError(it.message).left() }
    )

}

fun toKontakt(row: ResultRow): Kontakt? {
    if (!row.hasValue(KontaktBridge.id) || row.getOrNull(
            KontaktBridge.id
        ) == null
    ) {
        return null
    }

    return Kontakt(
        row[KontaktTable.id].value,
        row[KontaktTable.navn],
        row[KontaktTable.telefon],
        row[KontaktTable.rolle]
    )
}