package ombruk.backend.pickup.service

import arrow.core.Either
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.form.PatchPickupForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.error.ServiceError


interface IPickupService {
    fun savePickup(pickupForm: CreatePickupForm): Either<ServiceError,Pickup>
    fun getPickups(pickupQueryForm: GetPickupsForm = GetPickupsForm()): Either<ServiceError,List<Pickup>>
    fun getPickupById (id: Int): Either<ServiceError,Pickup>
    fun deletePickup(pickupID: Int?, stationID: Int?): Boolean
    fun updatePickup(patchPickupForm: PatchPickupForm ): Either<ServiceError,Pickup>
}