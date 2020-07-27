package ombruk.backend.pickup.service

import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.model.Pickup


interface IPickupService {
    fun savePickup(pickup: CreatePickupForm): Pickup
    fun getPickups(stationID: Int?): List<Pickup>
    fun getPickupById (id: Int): Pickup?
    fun deletePickup(pickupID: Int?, stationID: Int?): Boolean
    fun updatePickup(pickup: Pickup): Boolean
}