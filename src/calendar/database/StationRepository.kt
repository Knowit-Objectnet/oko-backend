package ombruk.backend.calendar.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.form.StationPostForm
import ombruk.backend.calendar.form.StationUpdateForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val logger = LoggerFactory.getLogger("ombruk.backend.database.StationRepository")

object Stations : IntIdTable("stations") {
    val name = varchar("name", 200)
    val openingTime = varchar("opening_time", 20)
    val closingTime = varchar("closing_time", 20)
}

object StationRepository : IStationRepository {

    /**
     * Get a Station by id or null if it doesn't exist.
     * @param id Id of the station to get
     * @return Either a Throwable or the Station
     */
    override fun getStationById(id: Int) = runCatching {
        transaction { Stations.select { Stations.id eq id }.map { toStation(it) } }
    }
        .onFailure { logger.error("Failed to get station from db: ${it.message}") }
        .fold(
            { Either.cond(it.isNotEmpty(), { it.first() }, { RepositoryError.NoRowsFound("No rows matchind $id") }) },
            { RepositoryError.SelectError("Failed to get station").left() })

    /**
     * A list of all stations
     * @return Either a Throwable or the list of stations
     */
    override fun getStations() = runCatching {
        transaction { Stations.selectAll().map { toStation(it) } }
    }
        .onFailure { logger.error("Failed to get stations from db") }
        .fold({ it.right() }, { RepositoryError.SelectError("Failed to get stations").left() })

    /**
     * Insert a Station to the db
     * @param stationPostForm The Station to insert
     * @return Either a Throwable or the Station with the correct id
     */
    override fun insertStation(stationPostForm: StationPostForm) = runCatching {
        transaction {
            Stations.insertAndGetId {
                it[name] = stationPostForm.name
                it[openingTime] = stationPostForm.openingTime.toString()
                it[closingTime] = stationPostForm.closingTime.toString()
            }.value
        }
    }
        .onFailure { logger.error("Failed to insert station to db") }
        .fold(
            { getStationById(it) },
            { RepositoryError.InsertError("Failed to insert station $stationPostForm").left() })

    /**
     * Update a given Station
     * @param station The Station to update
     * @return Either a Throwable or the updated Station
     */
    override fun updateStation(stationUpdateForm: StationUpdateForm) = runCatching {
        transaction {
            Stations.update({ Stations.id eq stationUpdateForm.id }) { row ->
                stationUpdateForm.name?.let {row[name] = stationUpdateForm.name}
                stationUpdateForm.openingTime?.let { row[openingTime] = stationUpdateForm.openingTime.toString() }
                stationUpdateForm.closingTime?.let { row[closingTime] = stationUpdateForm.closingTime.toString() }
            }
        }
    }
        .onFailure { logger.error("Failed to update station to db") }
        .fold({ getStationById(stationUpdateForm.id) }, { RepositoryError.UpdateError("Failed to update station $stationUpdateForm").left() })

    /**
     * Delete a given station from the DB.
     * @param station The Station to delete
     * @return Either a Throwable or the deleted Station
     */
    override fun deleteStation(id: Int) = runCatching {
        transaction { Stations.deleteWhere { Stations.id eq id } }
    }
        .onFailure { logger.error("Failed to delete station from db") }
        .fold({ id.right() }, { RepositoryError.DeleteError("Failed to delete station with ID $id").left() })
}

fun toStation(row: ResultRow): Station {
    return Station(
        row[Stations.id].value,
        row[Stations.name],
        LocalTime.parse(row[Stations.openingTime], DateTimeFormatter.ISO_TIME),
        LocalTime.parse(row[Stations.closingTime], DateTimeFormatter.ISO_TIME)
    )
}