package ombruk.backend.calendar.database

import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.model.Station
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ombruk.backend.database.StationRepository")

object Stations : IntIdTable("stations") {
    val name = varchar("name", 200)

    /**
     * Get a Station by id or null if it doesn't exist.
     * @param id Id of the station to get
     * @return Either a Throwable or the Station
     */
    fun getById(id: Int) = runCatching {
        transaction {
            select { Stations.id eq id }.map {
                Station(
                    id,
                    it[name]
                )
            }.firstOrNull()
        }
    }
        .onFailure { logger.error("Failed to get station from db") }
        .fold({ it.right() }, { it.left() })

    /**
     * A list of all stations
     * @return Either a Throwable or the list of stations
     */
    fun getAll() = runCatching {
        transaction {
            selectAll().map {
                Station(
                    it[Stations.id].value,
                    it[name]
                )
            }
        }
    }
    .onFailure { logger.error("Failed to get stations from db") }
    .fold({ it.right() }, { it.left() })

    /**
     * Insert a Station to the db
     * @param station The Station to insert
     * @return Either a Throwable or the Station with the corret id
     */
    fun insertStation(station: Station) = runCatching {
        transaction {
            insertAndGetId {
                it[name] = station.name
            }.value
        }
    }
        .onFailure { logger.error("Failed to insert station to db") }
        .fold({ station.copy(id=it).right() }, { it.left() })

    /**
     * Update a given Station
     * @param station The Station to update
     * @return Either a Throwable or the updated Station
     */
    fun updateStation(station: Station) = runCatching {
        transaction {
            update({ Stations.id eq station.id}) {
                it[name] = station.name
            }
        }
    }
        .onFailure { logger.error("Failed to update station to db") }
        .fold({ station.right() }, { it.left() })

    /**
     * Delete a given station from the DB.
     * @param station The Station to delete
     * @return Either a Throwable or the deleted Station
     */
    fun deleteStation(station: Station) = runCatching {
        transaction { deleteWhere { Stations.id eq station.id} }
    }
        .onFailure { logger.error("Failed to delete station from db") }
        .fold({ station.right() }, { it.left() })
}