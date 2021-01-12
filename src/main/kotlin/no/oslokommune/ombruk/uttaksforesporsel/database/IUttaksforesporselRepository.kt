package no.oslokommune.ombruk.uttaksforesporsel.database

import arrow.core.Either
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.model.Uttak;

interface IUttaksforesporselRepository {

    /**
     * Gets a [List] of [UttaksForesporsel] objects that can be filtered with constraints.
     *
     * @param foresporselGetForm a [UttaksForesporselGetForm] with constraints that are only added if they are not null.
     * @return A [RepositoryError] on success and a [List] of [UttaksForesporsel] objects on success.
     */
    fun getForesporsler(foresporselGetForm: UttaksForesporselGetForm? = null): Either<RepositoryError, List<UttaksForesporsel>>

    /**
     * Stores a [UttaksForesporsel] in the database.
     *
     * @param foresporselPostForm A [UttaksforesporselPostForm] that specifies what partner should be added to what uttaksforesporsel.
     * @return A [RepositoryError] on failure and the stored [UttaksForesporsel] on success.
     */
    fun saveForesporsel(foresporselPostForm: UttaksforesporselPostForm): Either<RepositoryError, UttaksForesporsel>

    /**
     * Deletes a [UttaksForesporsel] from an [Uttak]
     *
     * @param foresporselDeleteForm A [UttaksforesporselDeleteForm] that specifies what [UttaksForesporsel] should be deleted.
     * @return A [RepositoryError] on failure and an [Int] specifying how many [UttaksForesporsel] objects were deleted on success.
     */
    fun deleteForesporsel(foresporselDeleteForm: UttaksforesporselDeleteForm): Either<RepositoryError, Int>
}