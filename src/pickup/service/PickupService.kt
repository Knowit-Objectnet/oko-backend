package ombruk.backend.pickup.service

import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.form.PatchPickupForm
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object PickupService : IPickupService {

    override fun savePickup(pickupForm: CreatePickupForm) = transaction {
        // Returns an either with pickup
        PickupRepository.savePickup(pickupForm)
    }

    override fun getPickupById(id: Int) = transaction {
        PickupRepository.getPickupById(id)
    }


    override fun getPickups(pickupQueryForm: GetPickupsForm) = transaction {
        PickupRepository.getPickups(pickupQueryForm)
    }


    override fun updatePickup(patchPickupForm: PatchPickupForm) =
        PickupRepository.updatePickup(patchPickupForm)


    override fun deletePickup(pickupID: Int?, stationID: Int?): Boolean {
        if (pickupID == null && stationID == null) {
            throw IllegalArgumentException("Must pass in an argument")
        }

        return try {
            var count = 0
            pickupID?.let {
                count = transaction {
                    Requests.deleteWhere { Requests.pickupID eq pickupID }
                    PickupRepository.Pickups.deleteWhere { PickupRepository.Pickups.id eq pickupID }
                }
            }
            stationID?.let {
                count = transaction {
                    RequestService.deleteRequests(null, null, stationID)
                    PickupRepository.Pickups.deleteWhere { PickupRepository.Pickups.stationID eq stationID }
                }
            }
            count > 0
        } catch (e: Exception) {
            false
        }
    }


}