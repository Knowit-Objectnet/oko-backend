package no.oslokommune.ombruk.uttaksforesporsel.database

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.Uttaksforesporsel
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IUttaksforesporselRepository {

    /**
     * Gets a [List] of [Uttaksforesporsel] objects that can be filtered with constraints.
     *
     * @param requestGetForm a [UttaksforesporselGetForm] with constraints that are only added if they are not null.
     * @return A [RepositoryError] on success and a [List] of [Uttaksforesporsel] objects on success.
     */
    fun getRequests(requestGetForm: UttaksforesporselGetForm? = null): Either<RepositoryError, List<Uttaksforesporsel>>

    /**
     * Stores a [Pickup] in the database.
     *
     * @param requestPostForm A [UttaksforesporselPostForm] that specifies what partner should be added to what uttaksforesporsel.
     * @return A [RepositoryError] on failure and the stored [Uttaksforesporsel] on success.
     */
    fun saveRequest(requestPostForm: UttaksforesporselPostForm): Either<RepositoryError, Uttaksforesporsel>

    /**
     * Deletes a uttaksforesporsel from a [Pickup]
     *
     * @param requestDeleteForm A [UttaksforesporselDeleteForm] that specifies what [Uttaksforesporsel] should be deleted.
     * @return A [RepositoryError] on failure and an [Int] specifying how many [Uttaksforesporsel] objects were deleted on success.
     */
    fun deleteRequest(requestDeleteForm: UttaksforesporselDeleteForm): Either<RepositoryError, Int>
}