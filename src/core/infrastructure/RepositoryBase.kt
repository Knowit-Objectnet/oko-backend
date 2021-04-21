package ombruk.backend.core.db

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.core.domain.model.FindParams
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

abstract class RepositoryBase<Entity : Any, EntityParams, EntityFindParams : FindParams> {

    abstract fun insertQuery(params: EntityParams): EntityID<Int>

    abstract fun prepareQuery(params: EntityFindParams): Query

    abstract fun toEntity(row: ResultRow): Entity

    abstract val table: IntIdTable

    fun save(params: EntityParams): Either<RepositoryError, Entity> {
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

    fun findOne(id: Int): Either<RepositoryError, Entity> {
        return runCatching {
            table.select { table.id eq id }.mapNotNull { toEntity(it) }
        }.fold(
            {
                if (it.isNotEmpty()) it.first().right() else RepositoryError.NoRowsFound("$id not found").left()
            },
            { RepositoryError.InsertError("SQL error").left() }
        )
    }

    fun delete(id: Int): Either<RepositoryError, Unit> = runCatching {
        table.deleteWhere { table.id eq id  }
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