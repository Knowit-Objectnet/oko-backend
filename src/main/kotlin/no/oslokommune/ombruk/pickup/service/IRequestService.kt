package no.oslokommune.ombruk.pickup.service

import arrow.core.Either
import no.oslokommune.ombruk.pickup.form.request.RequestDeleteForm
import no.oslokommune.ombruk.pickup.form.request.RequestGetForm
import no.oslokommune.ombruk.pickup.form.request.RequestPostForm
import no.oslokommune.ombruk.pickup.model.Pickup
import no.oslokommune.ombruk.pickup.model.Request
import no.oslokommune.ombruk.shared.error.ServiceError


interface IRequestService {

    /**
     * Adds a request to a [Pickup].
     *
     * @param requestPostForm A [RequestPostForm] that specifies what partner should be added to what request.
     * @return A [ServiceError] on failure and the stored [Request] on success.
     */
    fun saveRequest(requestPostForm: RequestPostForm): Either<ServiceError, Request>

    /**
     * Gets a [List] of [Request] objects that can be filtered with constraints. Seeing as a [Request] has no primary key,
     * one has to GET a specific [Request] by specifying both a partner id and a no.oslokommune.ombruk.pickup id.
     *
     * @param requestGetForm a [RequestGetForm] with constraints that are only added if they are not null.
     * @return A [ServiceError] on success and a [List] of [Request] objects on success.
     */
    fun getRequests(requestGetForm: RequestGetForm? = null): Either<ServiceError, List<Request>>

    /**
     * Deletes a request from a [Pickup]
     *
     * @param requestDeleteForm A [RequestDeleteForm] that specifies what [Request] should be deleted.
     * @return A [ServiceError] on failure and an [Int] specifying how many [Request] objects were deleted on success.
     */
    fun deleteRequest(requestDeleteForm: RequestDeleteForm): Either<ServiceError, Int>
}