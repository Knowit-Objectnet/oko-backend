package no.oslokommune.ombruk.pickup.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.service.UttakService
import no.oslokommune.ombruk.pickup.database.PickupRepository
import no.oslokommune.ombruk.pickup.form.pickup.*
import no.oslokommune.ombruk.pickup.model.Pickup
import no.oslokommune.ombruk.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction


object PickupService : IPickupService {

    override fun savePickup(pickupPostForm: PickupPostForm): Either<ServiceError, Pickup> =
        PickupRepository.savePickup(pickupPostForm)


    override fun getPickupById(pickupGetByIdForm: PickupGetByIdForm): Either<ServiceError, Pickup> =
        PickupRepository.getPickupById(pickupGetByIdForm.id)


    override fun getPickups(pickupQueryForm: PickupGetForm?): Either<ServiceError, List<Pickup>> =
        PickupRepository.getPickups(pickupQueryForm)


    override fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<ServiceError, Pickup> = transaction {
        pickupUpdateForm.chosenPartnerId?.let { // partner has been chosen and no.oslokommune.ombruk.pickup has been filled. Create uttak.
            var pickup: Pickup? = null      // used for temp storage
            PickupRepository.updatePickup(pickupUpdateForm)
                .map {
                    pickup = it
                    val uttakPostForm = UttakPostForm(
                        it.startDateTime, it.endDateTime,
                        it.stasjon.id,
                        it.chosenPartner!!.id // chosePartner is always set or we wouldn't be here.
                    )
                    return@map UttakService.saveUttak(uttakPostForm) //Creates the uttak
                }
                .fold(
                    // failure, we rollback and set a left.
                    { rollback(); it.left() },
                    { pickup!!.right() }    // is never null on right.
                )
        } ?: PickupRepository.updatePickup(pickupUpdateForm)    // no.oslokommune.ombruk.pickup is not fulfilled, only updated.
    }

    override fun deletePickup(pickupDeleteForm: PickupDeleteForm): Either<ServiceError, Int> =
        PickupRepository.deletePickup(pickupDeleteForm.id)


}