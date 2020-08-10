package ombruk.backend.pickup.database

import arrow.core.Either
import ombruk.backend.pickup.form.pickup.PickupGetForm
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.form.pickup.PickupUpdateForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.error.RepositoryError

interface IPickupRepository {

    /**
     * Saves a pickup to the database.
     *
     * @param pickupPostForm A valid [PickupPostForm]
     * @return A [RepositoryError] on failure and the stored [Pickup] on success.
     */
    fun savePickup(pickupPostForm: PickupPostForm): Either<RepositoryError, Pickup>

    /**
     * Gets a [Pickup] by its ID
     *
     * @param id The id of a pickup
     * @return A [RepositoryError] on failure and a [Pickup] on success.
     */
    fun getPickupById(id: Int): Either<RepositoryError, Pickup>

    /**
     * Gets a [List] of [Pickup] objects filtered based on what properties are set in the [PickupGetForm].
     *
     * @param pickupGetForm a [PickupGetForm] specifying what constraints should be applied to a query.
     * @return A [RepositoryError] on failure and a [List] of [Pickup] objects on success.
     */
    fun getPickups(pickupGetForm: PickupGetForm? = null): Either<RepositoryError, List<Pickup>>

    /**
     * Updates a [Pickup] object in the database. Only the properties that are not null will be updated
     *
     * @param pickupUpdateForm a [PickupUpdateForm] that specifies what should be updated.
     * @return A [RepositoryError] on failure and a [Pickup] on success.
     */
    fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<RepositoryError, Pickup>

    /**
     * Deletes a [Pickup] based on its ID.
     *
     * @param id The id of the pickup that should be deleted.
     * @return A [RepositoryError] on failure and an [Int] value describing how many pickups were deleted on success.
     */
    fun deletePickup(id: Int): Either<RepositoryError, Int>
}