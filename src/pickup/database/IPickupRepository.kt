package ombruk.backend.pickup.database

import arrow.core.Either
import ombruk.backend.pickup.form.pickup.PickupDeleteForm
import ombruk.backend.pickup.form.pickup.PickupGetForm
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.form.pickup.PickupUpdateForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.error.RepositoryError

interface IPickupRepository {

    fun savePickup(pickupPostForm: PickupPostForm): Either<RepositoryError, Pickup>
    fun getPickupById(id: Int): Either<RepositoryError, Pickup>
    fun getPickups(pickupGetForm: PickupGetForm): Either<RepositoryError, Pickup>
    fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<RepositoryError, Pickup>
    fun deletePickup(pickupDeleteForm: PickupDeleteForm): Either<RepositoryError, Pickup>
}