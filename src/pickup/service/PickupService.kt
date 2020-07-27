package ombruk.backend.pickup.service

import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.calendar.database.Stations
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.calendar.model.Station
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object PickupService : IPickupService {

    override fun savePickup(form: CreatePickupForm): Pickup {
        val id = transaction {
            Pickups.insertAndGetId {
                it[stationID] = form.stationId
                it[startTime] = form.startTime
                it[endTime] = form.endTime
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

    override fun getPickups(stationID: Int?): List<Pickup>{
        return transaction{
            val query = (Pickups innerJoin Stations).selectAll()
            stationID?.let{
                query.andWhere { Pickups.stationID eq it }
            }
            query.map { toPickup(it) }
        }
    }

    override fun updatePickup(pickup: Pickup): Boolean{
            return try {
                var check = 0
                check = transaction {
                    Pickups.update({ Pickups.id eq pickup.id }) {
                    it[startTime] = pickup.startTime
                    it[endTime] = pickup.endTime
                    it[stationID] = pickup.station.id
                }}
                check>0
            } catch(e: Exception){
                false
            }
    }

    override fun deletePickup (pickupID: Int?, stationID: Int?): Boolean{
        if (pickupID == null && stationID == null){
            throw IllegalArgumentException("Must pass in an argument")
        }

        return try {
            var count =0
            pickupID?.let {
                count = transaction {
                    Requests.deleteWhere { Requests.pickupID eq pickupID }
                    Pickups.deleteWhere { Pickups.id eq pickupID}
                }
            }
            stationID?.let {
                count = transaction {
                    RequestService.deleteRequests(null, null, stationID)
                    Pickups.deleteWhere { Pickups.stationID eq stationID }
                }
            }
            count > 0
        } catch(e: Exception){
            false
        }
    }

    private fun toPickup (row: ResultRow): Pickup {
        return Pickup(
            row[Pickups.id].value,
            row[Pickups.startTime],
            row[Pickups.endTime],
            Station(row[Stations.id].value, row[Stations.name])
        )
    }
}