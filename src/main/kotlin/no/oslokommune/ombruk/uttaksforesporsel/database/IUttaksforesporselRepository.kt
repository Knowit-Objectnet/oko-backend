package no.oslokommune.ombruk.uttaksforesporsel.database

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IUttaksforesporselRepository {

    /**
     * Gets a [List] of [UttaksForesporsel] objects that can be filtered with constraints.
     *
     * @param requestGetForm a [UttaksForesporselGetForm] with constraints that are only added if they are not null.
     * @return A [RepositoryError] on success and a [List] of [UttaksForesporsel] objects on success.
     */
    fun getForesporsler(requestGetForm: UttaksForesporselGetForm? = null): Either<RepositoryError, List<UttaksForesporsel>>

    /**
     * Stores a [Pickup] in the database.
     *
     * @param requestPostForm A [UttaksforesporselPostForm] that specifies what partner should be added to what uttaksforesporsel.
     * @return A [RepositoryError] on failure and the stored [UttaksForesporsel] on success.
     */
    fun saveForesporsel(requestPostForm: UttaksforesporselPostForm): Either<RepositoryError, UttaksForesporsel>

    /**
     * Deletes a uttaksforesporsel from a [Pickup]
     *
     * @param requestDeleteForm A [UttaksforesporselDeleteForm] that specifies what [UttaksForesporsel] should be deleted.
     * @return A [RepositoryError] on failure and an [Int] specifying how many [UttaksForesporsel] objects were deleted on success.
     */
    fun deleteForesporsel(requestDeleteForm: UttaksforesporselDeleteForm): Either<RepositoryError, Int>
}