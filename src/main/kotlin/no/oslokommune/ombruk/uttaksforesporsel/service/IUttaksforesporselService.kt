package no.oslokommune.ombruk.uttaksforesporsel.service

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.Pickup
import no.oslokommune.ombruk.uttaksforesporsel.model.Uttaksforesporsel
import no.oslokommune.ombruk.shared.error.ServiceError


interface IUttaksforesporselService {

    /**
     * Adds a uttaksforesporsel to a [Pickup].
     *
     * @param requestPostForm A [UttaksforesporselPostForm] that specifies what partner should be added to what uttaksforesporsel.
     * @return A [ServiceError] on failure and the stored [Uttaksforesporsel] on success.
     */
    fun saveRequest(requestPostForm: UttaksforesporselPostForm): Either<ServiceError, Uttaksforesporsel>

    /**
     * Gets a [List] of [Uttaksforesporsel] objects that can be filtered with constraints. Seeing as a [Uttaksforesporsel] has no primary key,
     * one has to GET a specific [Uttaksforesporsel] by specifying both a partner id and a no.oslokommune.ombruk.pickup id.
     *
     * @param requestGetForm a [UttaksforesporselGetForm] with constraints that are only added if they are not null.
     * @return A [ServiceError] on success and a [List] of [Uttaksforesporsel] objects on success.
     */
    fun getRequests(requestGetForm: UttaksforesporselGetForm? = null): Either<ServiceError, List<Uttaksforesporsel>>

    /**
     * Deletes a uttaksforesporsel from a [Pickup]
     *
     * @param requestDeleteForm A [UttaksforesporselDeleteForm] that specifies what [Uttaksforesporsel] should be deleted.
     * @return A [ServiceError] on failure and an [Int] specifying how many [Uttaksforesporsel] objects were deleted on success.
     */
    fun deleteRequest(requestDeleteForm: UttaksforesporselDeleteForm): Either<ServiceError, Int>
}