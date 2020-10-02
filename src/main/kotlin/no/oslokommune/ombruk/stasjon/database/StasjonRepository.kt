package no.oslokommune.ombruk.stasjon.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.model.serializer.DayOfWeekSerializer
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ombruk.backend.database.StasjonRepository")
val json = Json(JsonConfiguration.Stable)

object Stasjoner : IntIdTable("stasjoner") {
    val name = varchar("name", 200)

    //The following strings are supposed to represent a time on the format "HH:MM:SS", followed by an optional "Z" for UTC.
    val hours = varchar("hours", 400).nullable()
}

object StasjonRepository : IStasjonRepository {

    override fun getStasjonById(id: Int) = runCatching {
        transaction { Stasjoner.select { Stasjoner.id eq id }.map { toStasjon(it) } }
    }
        .onFailure { logger.error("Failed to get stasjon from db: ${it.message}") }
        .fold(
            { Either.cond(it.isNotEmpty(), { it.first() }, { RepositoryError.NoRowsFound("No rows matching $id") }) },
            { RepositoryError.SelectError("Failed to get stasjon").left() })


    @KtorExperimentalLocationsAPI
    override fun getStasjoner(stasjonGetForm: StasjonGetForm) = runCatching {
        transaction {
            val query = Stasjoner.selectAll()
            stasjonGetForm.name?.let { query.andWhere { Stasjoner.name eq it } }
            query.mapNotNull { toStasjon(it) }
        }
    }
        .onFailure { logger.error("Failed to get stasjoner from db") }
        .fold({ it.right() }, { RepositoryError.SelectError("Failed to get stasjoner").left() })


    override fun insertStasjon(stasjonPostForm: StasjonPostForm) =
        transaction {
            runCatching {
                Stasjoner.insertAndGetId {
                    it[name] = stasjonPostForm.name
                    it[hours] = stasjonPostForm.hours?.let { hours ->
                        json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                            .toString()
                    }
                }.value
            }
        }
            .onFailure { logger.error("Failed to insert stasjon to db: ${it.message}") }
            .fold(
                { getStasjonById(it) },
                { RepositoryError.InsertError("Failed to insert stasjon $stasjonPostForm").left() }
            )


    override fun updateStasjon(stasjonUpdateForm: StasjonUpdateForm) = runCatching {
        transaction {
            Stasjoner.update({ Stasjoner.id eq stasjonUpdateForm.id }) { row ->
                stasjonUpdateForm.name?.let { row[name] = stasjonUpdateForm.name }
                stasjonUpdateForm.hours?.let {
                    row[hours] =
                        json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), it)
                            .toString()
                }
            }
        }
    }
        .onFailure { logger.error("Failed to update stasjon to db") }
        .fold(
            { getStasjonById(stasjonUpdateForm.id) },
            { RepositoryError.UpdateError("Failed to update stasjon $stasjonUpdateForm").left() })


    override fun deleteStasjon(id: Int) = runCatching {
        transaction { Stasjoner.deleteWhere { Stasjoner.id eq id } }
    }
        .onFailure { logger.error("Failed to delete stasjon from db") }
        .fold({ id.right() }, { RepositoryError.DeleteError("Failed to delete stasjon with ID $id").left() })

    fun deleteAllStasjoner() = runCatching {
        transaction { Stasjoner.deleteAll() }
    }
        .onFailure { logger.error("Failed to delete stasjon from db") }
        .fold({ Unit.right() }, { RepositoryError.DeleteError("Failed to delete stasjoner").left() })

    override fun exists(id: Int) = transaction { Stasjoner.select { Stasjoner.id eq id }.count() >= 1 }

    override fun exists(name: String) = transaction { Stasjoner.select { Stasjoner.name eq name }.count() >= 1 }

}

/**
 * Helper function used for converting a [ResultRow] from a query to a [Stasjon] object.
 * @param row A [ResultRow]
 * @return A [Stasjon] object
 */
fun toStasjon(row: ResultRow): Stasjon {
    return Stasjon(
        row[Stasjoner.id].value,
        row[Stasjoner.name],
        row[Stasjoner.hours]?.let {
            json.parse(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), it)
        }
    )

}