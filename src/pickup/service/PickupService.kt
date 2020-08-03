package ombruk.backend.pickup.service

import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.pickup.form.pickup.*


object PickupService : IPickupService {

    override fun savePickup(pickupPostForm: PickupPostForm) = PickupRepository.savePickup(pickupPostForm)

    override fun getPickupById(pickupGetByIdForm: PickupGetByIdForm) =
        PickupRepository.getPickupById(pickupGetByIdForm.id)

    override fun getPickups(pickupQueryForm: PickupGetForm) = PickupRepository.getPickups(pickupQueryForm)

    override fun updatePickup(pickupUpdateForm: PickupUpdateForm) = PickupRepository.updatePickup(pickupUpdateForm)

    override fun deletePickup(pickupDeleteForm: PickupDeleteForm) = PickupRepository.deletePickup(pickupDeleteForm.id)


}