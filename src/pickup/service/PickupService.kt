package ombruk.backend.pickup.service

import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.service.EventService
import ombruk.backend.pickup.database.PickupRepository
import ombruk.backend.pickup.form.pickup.*
import ombruk.backend.pickup.model.Pickup
import org.jetbrains.exposed.sql.transactions.transaction


object PickupService : IPickupService {

    override fun savePickup(pickupPostForm: PickupPostForm) = PickupRepository.savePickup(pickupPostForm)


    override fun getPickupById(pickupGetByIdForm: PickupGetByIdForm) =
        PickupRepository.getPickupById(pickupGetByIdForm.id)


    override fun getPickups(pickupQueryForm: PickupGetForm?) = PickupRepository.getPickups(pickupQueryForm)


    override fun updatePickup(pickupUpdateForm: PickupUpdateForm) = transaction {
        pickupUpdateForm.chosenPartnerId?.let { // partner has been chosen and pickup has been filled. Create event.
            var pickup: Pickup? = null      // used for temp storage
            PickupRepository.updatePickup(pickupUpdateForm)
                .map {
                    pickup = it
                    val eventPostForm = EventPostForm(
                        it.startDateTime, it.endDateTime,
                        it.station.id,
                        it.chosenPartner!!.id // chosePartner is always set or we wouldn't be here.
                    )
                    EventService.saveEvent(eventPostForm)   //Creates the event
                }
                .fold(
                    // failure, we rollback and set a left.
                    { rollback(); it.left() },
                    { pickup!!.right() }    // is never null on right.
                )
        } ?: PickupRepository.updatePickup(pickupUpdateForm)    // pickup is not fulfilled, only updated.
    }


    override fun deletePickup(pickupDeleteForm: PickupDeleteForm) = PickupRepository.deletePickup(pickupDeleteForm.id)


}