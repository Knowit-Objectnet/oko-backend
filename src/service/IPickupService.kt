package ombruk.backend.service

import ombruk.backend.form.api.CreatePickupForm
import ombruk.backend.model.Pickup


interface IPickupService {
    fun savePickup(pickup: CreatePickupForm): Pickup
    fun getPickups(stationID: Int?): List<Pickup>
    fun getPickupById (id: Int): Pickup?
    fun deletePickup(pickupID: Int?, stationID: Int?): Boolean
    fun updatePickup(pickup: Pickup): Boolean
}