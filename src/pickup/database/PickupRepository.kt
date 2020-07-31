package ombruk.backend.pickup.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.database.toStation
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object Pickups : IntIdTable("pickups") {
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val stationID = integer("station_id").references(Stations.id)
}

object PickupRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.PickupRepository")

    object Pickups : IntIdTable("pickups") {
        val startTime = datetime("start_time")
        val endTime = datetime("end_time")
        val stationID = integer("station_id").references(Stations.id)
    }

    fun savePickup(pickupForm: CreatePickupForm): Either<ServiceError, Pickup> = runCatching {
        Pickups.insertAndGetId {
            it[stationID] = pickupForm.stationId
            it[startTime] = pickupForm.startTime
            it[endTime] = pickupForm.endTime
        }.value
    }.onFailure {
        logger.error("Failed to save Pickup to DB: ${it.message}")
    }.fold(
        { getPickupById(it) },
        { RepositoryError.InsertError("SQL error").left() }
    )

    fun getPickupById(id: Int): Either<ServiceError, Pickup> = runCatching {
        transaction {
            (Pickups innerJoin Stations).select { Pickups.id eq id }
                .map { toPickup(it) }.firstOrNull()
        }
    }.onFailure { logger.error(it.message) }
        .fold(
            { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID does not exist!") }) },
            { RepositoryError.SelectError(it.message).left() })


// getPickups.
// Note that the start- and end-times only look at the startTime of the pickup.

    fun getPickups(pickupQueryForm: GetPickupsForm): Either<ServiceError, List<Pickup>> {
        return runCatching {
            transaction {

                val query = (Pickups innerJoin Stations).selectAll()

                pickupQueryForm.stationId?.let {
                    query.andWhere { Pickups.stationID eq it }
                }
                pickupQueryForm.endTime?.let {
                    query.andWhere { Pickups.startTime lessEq it }
                }
                pickupQueryForm.startTime?.let {
                    query.andWhere { Pickups.startTime greaterEq it }
                }
                query.map { toPickup(it) }
            }
        }.fold(
            { it.right() },
            { ServiceError("Database query failed").left() }
        )
    }

    private fun toPickup(row: ResultRow): Pickup {
        return Pickup(
            row[Pickups.id].value,
            row[Pickups.startTime],
            row[Pickups.endTime],
            toStation(row)
        )
    }
}
