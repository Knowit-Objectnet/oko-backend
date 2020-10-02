package no.oslokommune.ombruk.stasjon.database

import arrow.core.Either
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.database.IRepository
import no.oslokommune.ombruk.shared.database.IRepositoryUniqueName
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IStasjonRepository : IRepository, IRepositoryUniqueName {

    /**
     * Gets a stasjon by its ID.
     *
     * @param id Id of the stasjon to get
     * @return A [RepositoryError] on failure or a [Stasjon] on success.
     */
    fun getStasjonById(id: Int): Either<RepositoryError, Stasjon>

    /**
     * Gets a list of stasjoner filtered by parameters specified in a [StasjonGetForm].
     *
     * @param stasjonGetForm A [StasjonGetForm] used for specifying query conditionals.
     * @return A [RepositoryError] on failure and a [List] of [Stasjon] objects on success.
     */
    fun getStasjoner(stasjonGetForm: StasjonGetForm): Either<RepositoryError, List<Stasjon>>

    /**
     * Insert a Stasjon to the db.
     *
     * @param stasjonPostForm The Stasjon to insert
     * @return A [RepositoryError] on failure and the inserted [Stasjon] on success.
     */
    fun insertStasjon(stasjonPostForm: StasjonPostForm): Either<RepositoryError, Stasjon>

    /**
     * Update a given Stasjon.
     *
     * @param stasjonUpdateForm A [StasjonUpdateForm] containing values to be updated. Properties that are null will not be updated.
     * @return A [RepositoryError] on failure and a [Stasjon] on success.
     */
    fun updateStasjon(stasjonUpdateForm: StasjonUpdateForm): Either<RepositoryError, Stasjon>

    /**
     * Delete a given stasjon from the DB.
     *
     * @param id The id of the [Stasjon] to delete.
     * @return A [RepositoryError] on success and an [Int] on success. The [Int] represents the amount of stasjoner that were deleted.
     */
    fun deleteStasjon(id: Int): Either<RepositoryError, Int>
}