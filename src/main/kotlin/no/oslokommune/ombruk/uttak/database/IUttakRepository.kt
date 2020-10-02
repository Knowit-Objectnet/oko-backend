package no.oslokommune.ombruk.uttak.database

import arrow.core.Either
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.shared.database.IRepository
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IUttakRepository : IRepository {
    /**
     * Inserts one or several [Uttak] objects into the database.
     *
     * @param uttakPostForm A [UttakPostForm] describing the uttak(s) to be posted.
     * @return An [Either] object consisting of a [RepositoryError] on failure and the saved [Uttak] on success. The
     * returned [Uttak] is equal to the one stored in the database. If the posted [Uttak] is recurring, the first
     * occurence will be returned.
     */
    fun insertUttak(uttakPostForm: UttakPostForm): Either<RepositoryError, Uttak>

    /**
     * Updates a stored Uttak. The id passed in the [Uttak] must already exist in the database.
     *
     * @param uttak A [UttakUpdateForm] object containing the information that should be updated. ID cannot be altered.
     * @return An [Either] object consisting of a [RepositoryError] on failure or an [Uttak] with the updated values on success.
     */
    fun updateUttak(uttak: UttakUpdateForm): Either<RepositoryError, Uttak>

    /**
     * Deletes one or several [Uttak] objects from the database. The uttaks to be deleted are filtered through the use
     * of non-null properties in the [UttakDeleteForm].
     *
     * @param uttakDeleteForm A [UttakDeleteForm] containing the query constraints.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [List] of deleted [Uttak] objects on success.
     */
    fun deleteUttak(uttakDeleteForm: UttakDeleteForm): Either<RepositoryError, List<Uttak>>

    /**
     * Fetches a specific [Uttak].
     *
     * @param uttakID The id of the [Uttak] that should be fetched.
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [Uttak] on success.
     */
    fun getUttakByID(uttakID: Int): Either<RepositoryError, Uttak>

    /**
     * Fetches a set of uttaks that are specified by query parameters in a [UttakGetForm]. If null, all uttaks are returned.
     *
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [List] of [Uttak] objects on success.
     */
    fun getUttaks(uttakGetForm: UttakGetForm?): Either<RepositoryError, List<Uttak>>
}