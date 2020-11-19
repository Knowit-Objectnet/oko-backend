package no.oslokommune.ombruk.stasjon.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.database.Partnere
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.shared.model.serializer.DayOfWeekSerializer
import no.oslokommune.ombruk.shared.model.serializer.LocalTimeSerializer
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val logger = LoggerFactory.getLogger("ombruk.backend.database.StasjonRepository")
val json = Json(JsonConfiguration.Stable)

object Stasjoner : IntIdTable("stasjoner") {
    val navn                = varchar("navn", 128)
    val endretTidspunkt     = datetime("endret_tidspunkt")
    val slettetTidspunkt    = datetime("slettet_tidspunkt")
    //The following strings are supposed to represent a time on the format "HH:MM:SS", followed by an optional "Z" for UTC.
    val aapningstider       = varchar("aapningstider", 400).nullable()
}

object StasjonRepository : IStasjonRepository {

    override fun getStasjonById(id: Int) = runCatching {
        transaction { Stasjoner.select { Stasjoner.id eq id and Stasjoner.slettetTidspunkt.isNull() }.map { toStasjon(it) } }
    }
        .onFailure { logger.error("Failed to get stasjon from db: ${it.message}") }
        .fold(
            { Either.cond(it.isNotEmpty(), { it.first() }, { RepositoryError.NoRowsFound("No rows matching $id") }) },
            { RepositoryError.SelectError("Failed to get stasjon").left() })


    @KtorExperimentalLocationsAPI
    override fun getStasjoner(stasjonGetForm: StasjonGetForm) = runCatching {
        transaction {
            val query = Stasjoner.select { Stasjoner.slettetTidspunkt.isNull() }
            stasjonGetForm.navn?.let { query.andWhere { Stasjoner.navn eq it } }
            query.mapNotNull { toStasjon(it) }
        }
    }
        .onFailure { logger.error("Failed to get stasjoner from db") }
        .fold({ it.right() }, { RepositoryError.SelectError("Failed to get stasjoner").left() })


    override fun insertStasjon(stasjonPostForm: StasjonPostForm) = runCatching {
                transaction {
                    Stasjoner.insertAndGetId {
                        it[navn] = stasjonPostForm.navn
                        it[endretTidspunkt] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                        it[aapningstider] = stasjonPostForm.aapningstider?.let { hours ->
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
                stasjonUpdateForm.navn?.let { row[navn] = stasjonUpdateForm.navn }
                stasjonUpdateForm.aapningstider?.let {
                    row[aapningstider] =
                        json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), it)
                            .toString()
                }
                row[endretTidspunkt] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            }
        }
    }
        .onFailure { logger.error("Failed to update stasjon to db") }
        .fold(
            { getStasjonById(stasjonUpdateForm.id) },
            { RepositoryError.UpdateError("Failed to update stasjon $stasjonUpdateForm").left() })


    override fun deleteStasjon(id: Int) = runCatching {
        transaction {
            Stasjoner.update({ Stasjoner.id eq id }) {
                it[slettetTidspunkt] = LocalDateTime.now()
            }
        }
    }
        .onFailure { logger.error("Failed to delete stasjon from db") }
        .fold({ id.right() }, { RepositoryError.DeleteError("Failed to delete stasjon with ID $id").left() })

    fun deleteAllStasjoner() = runCatching {
        transaction { Stasjoner.update { it[slettetTidspunkt] = LocalDateTime.now() } }
    }
        .onFailure { logger.error("Failed to delete stasjon from db") }
        .fold({ Unit.right() }, { RepositoryError.DeleteError("Failed to delete stasjoner").left() })

    override fun exists(id: Int) = transaction { Stasjoner.select { Stasjoner.id eq id }.count() >= 1 }

    override fun exists(name: String) = transaction { Stasjoner.select { Stasjoner.navn eq name }.count() >= 1 }

    /**
     * Used by teardown() when testing.
     */
    fun deleteAllStasjonerForTesting() = runCatching {
        val appConfig = HoconApplicationConfig(ConfigFactory.load())
        val debug = appConfig.property("ktor.oko.debug").getString().toBoolean()
        if (!debug) { throw Exception() }
        transaction {
            Stasjoner.deleteAll()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Unit.right() },
            { RepositoryError.DeleteError("Failed to delete all pickups").left() }
        )

}

/**
 * Helper function used for converting a [ResultRow] from a query to a [Stasjon] object.
 * @param row A [ResultRow]
 * @return A [Stasjon] object
 */
fun toStasjon(row: ResultRow): Stasjon {
    return Stasjon(
        row[Stasjoner.id].value,
        row[Stasjoner.navn],
        row[Stasjoner.aapningstider]?.let {
            json.parse(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), it)
        }
    )

}