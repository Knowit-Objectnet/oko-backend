package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.form.pickup.*
import no.oslokommune.ombruk.uttaksforesporsel.model.Pickup
import no.oslokommune.ombruk.shared.error.ServiceError


interface IPickupService {

    /**
     * Saves a no.oslokommune.ombruk.pickup to the database.
     *
     * @param pickupPostForm A valid [PickupPostForm]
     * @return A [ServiceError] on failure and the stored [Pickup] on success.
     */
    fun savePickup(pickupPostForm: PickupPostForm): Either<ServiceError, Pickup>

    /**
     * Gets a [List] of [Pickup] objects filtered based on what properties are set in the [PickupGetForm].
     *
     * @param pickupGetForm a [PickupGetForm] specifying what constraints should be applied to a query.
     * @return A [ServiceError] on failure and a [List] of [Pickup] objects on success.
     */
    fun getPickups(pickupGetForm: PickupGetForm? = null): Either<ServiceError, List<Pickup>>

    /**
     * Gets a [Pickup] by its ID
     *
     * @param pickupGetByIdForm A [PickupGetByIdForm] that contains a valid [Pickup.id]
     * @return A [ServiceError] on failure and a [Pickup] on success.
     */
    fun getPickupById(pickupGetByIdForm: PickupGetByIdForm): Either<ServiceError, Pickup>

    /**
     * Deletes a [Pickup] based on its ID.
     *
     * @param pickupDeleteForm A [PickupDeleteForm] containing an ID corresponding to an existing [Pickup.id]
     * @return A [ServiceError] on failure and an [Int] value describing how many pickups were deleted on success.
     */
    fun deletePickup(pickupDeleteForm: PickupDeleteForm): Either<ServiceError, Int>

    /**
     * Updates a [Pickup] object in the database. Only the properties that are not null will be updated
     *
     * @param pickupUpdateForm a [PickupUpdateForm] that specifies what should be updated.
     * @return A [ServiceError] on failure and a [Pickup] on success.
     */
    fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<ServiceError, Pickup>
}