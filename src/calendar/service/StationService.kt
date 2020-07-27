package ombruk.backend.calendar.service

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import io.ktor.features.NotFoundException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import org.jetbrains.exposed.sql.transactions.transaction

object StationService : IStationService {

    private val json = Json(JsonConfiguration.Stable)

    override fun getStationById(id: Int) = Stations.getById(id)

    override fun getStations() = Stations.getAll()

    /**
     * Saves a station and submits a message to the message service
     * @param station Station to save
     * @return Either a throwable or a the station that was saved
     */
    override fun saveStation(station: Station) = transaction {
        val inserted = Stations.insertStation(station)

        inserted.map { json.stringify(Station.serializer(), it) }
            .fold({ it.left() }, { inserted })
    }

    /**
     * Update a station and submit a message to the message service
     * @param station Station to update
     * @return Either a Throwable or a the Station that was updated
     */
    override fun updateStation(station: Station) = transaction {
        Stations.updateStation(station)
            .fold({ it.left() }, { station.right() })
    }

    /**
     * Delete a station and submit a message to the message service
     * @param station Station to delete
     * @return Either a Throwable or the delete Station
     */
    override fun deleteStationById(id: Int) = transaction {
        val station = getStationById(id).getOrElse { null }
            ?: return@transaction NotFoundException("Failed to find station").left()

        return@transaction Stations.deleteStation(station)
            .fold({ it.left() }, { station.right() })
    }

}