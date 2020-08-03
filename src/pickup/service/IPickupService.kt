package ombruk.backend.pickup.service

import arrow.core.Either
import ombruk.backend.pickup.form.pickup.*
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.error.ServiceError


interface IPickupService {
    fun savePickup(pickupPostForm: PickupPostForm): Either<ServiceError,Pickup>
    fun getPickups(pickupQueryFormPickup: PickupGetForm = PickupGetForm()): Either<ServiceError,List<Pickup>>
    fun getPickupById (pickupGetByIdForm: PickupGetByIdForm): Either<ServiceError,Pickup>
    fun deletePickup(pickupDeleteForm: PickupDeleteForm): Either<ServiceError, Int>
    fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<ServiceError,Pickup>
}