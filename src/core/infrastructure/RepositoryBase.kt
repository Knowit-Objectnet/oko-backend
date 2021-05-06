package ombruk.backend.core.infrastructure

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.core.domain.model.FindParams
import ombruk.backend.core.domain.model.UpdateParams
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

abstract class RepositoryBase<Entity : Any, EntityParams, EntityUpdateParams: UpdateParams, EntityFindParams : FindParams> {

    abstract fun insertQuery(params: EntityParams): EntityID<UUID>

    abstract fun updateQuery(params: EntityUpdateParams): Int
    //FIXME: It has to return Int as Table.update returns this. If this is an issue, Table.update might have to be overridden.

    abstract fun prepareQuery(params: EntityFindParams): Query

    abstract fun toEntity(row: ResultRow): Entity

    abstract val table: UUIDTable

    fun insert(params: EntityParams): Either<RepositoryError, Entity> {
        return transaction {
            runCatching {
                insertQuery(params)
            }
        }
            .fold(
                { findOne(it.value) },
                { RepositoryError.InsertError("SQL error").left() }
            )
    }

    fun update(params: EntityUpdateParams): Either<RepositoryError, Entity> {
        return transaction {
            runCatching {
                updateQuery(params)
            }
        }.fold(
            //Return right if more than 1 partner has been updated. Else, return an Error
            {
                Either.cond(it > 0,
                    { findOne(params.id) },
                    { RepositoryError.NoRowsFound("${params.id} not found") })
            },
            { RepositoryError.UpdateError(it.message).left() })
            .flatMap { it }
    }

    fun findOne(id: UUID): Either<RepositoryError, Entity> {
        return runCatching {
            table.select { table.id eq id }.mapNotNull { toEntity(it) }
        }.fold(
            {
                if (it.isNotEmpty()) it.first().right() else RepositoryError.NoRowsFound("$id not found").left()
            },
            { RepositoryError.NoRowsFound("SQL error").left() }
        )
    }

    fun delete(id: UUID): Either<RepositoryError, Unit> = runCatching {
        table.deleteWhere { table.id eq id }
    }.fold(
        { Unit.right() },
        { RepositoryError.DeleteError(it.message).left() }
    )

    fun find(params: EntityFindParams): Either<RepositoryError, List<Entity>> = runCatching {
        transaction {
            prepareQuery(params).mapNotNull { toEntity(it) }
        }
    }.fold(
        { it.right() },
        { RepositoryError.SelectError(it.message).left() }
    )
}