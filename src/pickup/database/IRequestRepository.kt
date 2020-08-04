package ombruk.backend.pickup.database

import arrow.core.Either
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.error.RepositoryError

interface IRequestRepository {

    /**
     * Gets a [List] of [Request] objects that can be filtered with constraints.
     *
     * @param requestGetForm a [RequestGetForm] with constraints that are only added if they are not null.
     * @return A [RepositoryError] on success and a [List] of [Request] objects on success.
     */
    fun getRequests(requestGetForm: RequestGetForm? = null): Either<RepositoryError, List<Request>>

    /**
     * Stores a [Pickup] in the database.
     *
     * @param requestPostForm A [RequestPostForm] that specifies what partner should be added to what request.
     * @return A [RepositoryError] on failure and the stored [Request] on success.
     */
    fun saveRequest(requestPostForm: RequestPostForm): Either<RepositoryError, Request>

    /**
     * Deletes a request from a [Pickup]
     *
     * @param requestDeleteForm A [RequestDeleteForm] that specifies what [Request] should be deleted.
     * @return A [RepositoryError] on failure and an [Int] specifying how many [Request] objects were deleted on success.
     */
    fun deleteRequest(requestDeleteForm: RequestDeleteForm): Either<RepositoryError, Int>
}