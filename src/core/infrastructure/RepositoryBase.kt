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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

abstract class RepositoryBase<Entity : Any, EntityParams, EntityUpdateParams: UpdateParams, EntityFindParams : FindParams> {

    val logger: Logger = LoggerFactory.getLogger("ombruk.backend.core.infrastructure.RepositoryBase")

    abstract fun insertQuery(params: EntityParams): EntityID<UUID>

    //NOTE: Returns the number of updated entities
    abstract fun updateQuery(params: EntityUpdateParams): Int

    abstract fun prepareQuery(params: EntityFindParams): Query

    abstract fun toEntity(row: ResultRow): Entity

    abstract val table: UUIDTable

    fun insert(params: EntityParams): Either<RepositoryError, Entity> {
        return runCatching {
            insertQuery(params)
        }
            .onFailure { logger.error("Failed to insert into database; ${it.message}") }
            .fold(
                { findOne(it.value) },
                { RepositoryError.InsertError("SQL error").left() }
            )
    }

    fun update(params: EntityUpdateParams): Either<RepositoryError, Entity> {
        return runCatching {
            updateQuery(params)
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

    fun findOne(id: UUID): Either<RepositoryError, Entity> {
        return runCatching {
            table.select { table.id eq id }.mapNotNull { toEntity(it) }
        }
            .onFailure { logger.error("Failed to findOne in database; ${it.message}") }
            .fold(
                {
                    if (it.isNotEmpty()) it.first().right() else RepositoryError.NoRowsFound("$id not found").left()
                },
                { RepositoryError.NoRowsFound("SQL error").left() }
            )
    }

    fun delete(id: UUID): Either<RepositoryError, Unit> {
        return runCatching {
            table.deleteWhere { table.id eq id }
        }
            .onFailure { logger.error("Failed to delete from database; ${it.message}") }
            .fold(
                { Unit.right() },
                { RepositoryError.DeleteError(it.message).left() }
            )

    }

    fun find(params: EntityFindParams): Either<RepositoryError, List<Entity>> = runCatching {
        prepareQuery(params).mapNotNull { toEntity(it) }
    }
        .onFailure { logger.error("Failed to find in database; ${it.message}") }
        .fold(
            { it.right() },
            { RepositoryError.SelectError(it.message).left() }
        )
}