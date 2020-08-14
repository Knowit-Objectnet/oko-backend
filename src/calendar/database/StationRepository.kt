package ombruk.backend.calendar.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.form.station.StationGetForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.form.station.StationUpdateForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ombruk.backend.database.StationRepository")
val json = Json(JsonConfiguration.Stable)

object Stations : IntIdTable("stations") {
    val name = varchar("name", 200)

    //The following strings are supposed to represent a time on the format "HH:MM:SS", followed by an optional "Z" for UTC.
    val hours = varchar("hours", 400).nullable()
}

object StationRepository : IStationRepository {

    override fun getStationById(id: Int) = runCatching {
        transaction { Stations.select { Stations.id eq id }.map { toStation(it) } }
    }
        .onFailure { logger.error("Failed to get station from db: ${it.message}") }
        .fold(
            { Either.cond(it.isNotEmpty(), { it.first() }, { RepositoryError.NoRowsFound("No rows matching $id") }) },
            { RepositoryError.SelectError("Failed to get station").left() })


    @KtorExperimentalLocationsAPI
    override fun getStations(stationGetForm: StationGetForm) = runCatching {
        transaction {
            val query = Stations.selectAll()
            stationGetForm.name?.let { query.andWhere { Stations.name eq it } }
            query.mapNotNull { toStation(it) }
        }
    }
        .onFailure { logger.error("Failed to get stations from db") }
        .fold({ it.right() }, { RepositoryError.SelectError("Failed to get stations").left() })


    override fun insertStation(stationPostForm: StationPostForm) =
        transaction {
            runCatching {
                Stations.insertAndGetId {
                    it[name] = stationPostForm.name
                    it[hours] = stationPostForm.hours?.let { hours ->
                        json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                            .toString()
                    }
                }.value
            }
        }
            .onFailure { logger.error("Failed to insert station to db: ${it.message}") }
            .fold(
                { getStationById(it) },
                { RepositoryError.InsertError("Failed to insert station $stationPostForm").left() }
            )


    override fun updateStation(stationUpdateForm: StationUpdateForm) = runCatching {
        transaction {
            Stations.update({ Stations.id eq stationUpdateForm.id }) { row ->
                stationUpdateForm.name?.let { row[name] = stationUpdateForm.name }
                stationUpdateForm.hours?.let {
                    row[hours] =
                        json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), it)
                            .toString()
                }
            }
        }
    }
        .onFailure { logger.error("Failed to update station to db") }
        .fold(
            { getStationById(stationUpdateForm.id) },
            { RepositoryError.UpdateError("Failed to update station $stationUpdateForm").left() })


    override fun deleteStation(id: Int) = runCatching {
        transaction { Stations.deleteWhere { Stations.id eq id } }
    }
        .onFailure { logger.error("Failed to delete station from db") }
        .fold({ id.right() }, { RepositoryError.DeleteError("Failed to delete station with ID $id").left() })

    fun deleteAllStations() = runCatching {
        transaction { Stations.deleteAll() }
    }
        .onFailure { logger.error("Failed to delete station from db") }
        .fold({ Unit.right() }, { RepositoryError.DeleteError("Failed to delete stations").left() })

    override fun exists(id: Int) = transaction { Stations.select { Stations.id eq id }.count() >= 1 }

    override fun exists(name: String) = transaction { Stations.select{Stations.name eq name}.count() >= 1 }

}

/**
 * Helper function used for converting a [ResultRow] from a query to a [Station] object.
 * @param row A [ResultRow]
 * @return A [Station] object
 */
fun toStation(row: ResultRow): Station {
    return Station(
        row[Stations.id].value,
        row[Stations.name],
        row[Stations.hours]?.let {
            json.parse(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), it)
        }
    )

}