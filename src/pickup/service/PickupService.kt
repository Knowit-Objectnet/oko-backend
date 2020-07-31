package ombruk.backend.pickup.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.database.toStation
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.calendar.model.Station
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object PickupService : IPickupService {

    override fun savePickup(pickupForm: CreatePickupForm): Pickup {
        val id = transaction {
            Pickups.insertAndGetId {
                it[stationID] = pickupForm.stationId
                it[startTime] = pickupForm.startTime
                it[endTime] = pickupForm.endTime
            }
        }.value

        return getPickupById(id)!!
    }

    override fun getPickupById(id: Int): Pickup? {
        return transaction {
            (Pickups innerJoin Stations).select {
                Pickups.id eq id
            }.map { toPickup(it) }.firstOrNull()
        }
    }

    // getPickups.
    // Note that the start- and end-times only look at the startTime of the pickup.
    override fun getPickups(pickupQueryForm: GetPickupsForm): Either<ServiceError, List<Pickup>> {
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

    override fun updatePickup(pickup: Pickup): Boolean {
        return try {
            var check = 0
            check = transaction {
                Pickups.update({ Pickups.id eq pickup.id }) {
                    it[startTime] = pickup.startTime
                    it[endTime] = pickup.endTime
                    it[stationID] = pickup.station.id
                }
            }
            check > 0
        } catch (e: Exception) {
            false
        }
    }

    override fun deletePickup(pickupID: Int?, stationID: Int?): Boolean {
        if (pickupID == null && stationID == null) {
            throw IllegalArgumentException("Must pass in an argument")
        }

        return try {
            var count = 0
            pickupID?.let {
                count = transaction {
                    Requests.deleteWhere { Requests.pickupID eq pickupID }
                    Pickups.deleteWhere { Pickups.id eq pickupID }
                }
            }
            stationID?.let {
                count = transaction {
                    RequestService.deleteRequests(null, null, stationID)
                    Pickups.deleteWhere { Pickups.stationID eq stationID }
                }
            }
            count > 0
        } catch (e: Exception) {
            false
        }
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