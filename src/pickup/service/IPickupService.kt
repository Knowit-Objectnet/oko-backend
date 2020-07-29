package ombruk.backend.pickup.service

import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.model.Pickup


interface IPickupService {
    fun savePickup(pickupForm: CreatePickupForm): Pickup
    // Allow getPickups to be called without parameters.
    fun getPickups(pickupQueryForm: GetPickupsForm = GetPickupsForm()): List<Pickup>
    fun getPickupById (id: Int): Pickup?
    fun deletePickup(pickupID: Int?, stationID: Int?): Boolean
    fun updatePickup(pickup: Pickup): Boolean
}