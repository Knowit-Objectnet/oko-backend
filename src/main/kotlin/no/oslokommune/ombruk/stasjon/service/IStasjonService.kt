package no.oslokommune.ombruk.stasjon.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.form.StasjonGetForm
import no.oslokommune.ombruk.stasjon.form.StasjonPostForm
import no.oslokommune.ombruk.stasjon.form.StasjonUpdateForm
import no.oslokommune.ombruk.stasjon.model.Stasjon
import no.oslokommune.ombruk.shared.error.ServiceError

interface IStasjonService {

    /**
     * Get a single stasjon by its id.
     * @return Either a [ServiceError] or a [Stasjon].
     */
    fun getStasjonById(id: Int): Either<ServiceError, Stasjon>

    /**
     * Gets all stasjoner
     * @return Either a [ServiceError] or a [List] of [Stasjon] objects. The list may be empty if there are no stasjoner
     */
    @KtorExperimentalLocationsAPI
    fun getStasjoner(stasjonGetForm: StasjonGetForm): Either<ServiceError, List<Stasjon>>

    /**
     * Saves a stasjon.
     * @param stasjonPostForm Stasjon to save
     * @return Either a [ServiceError] or the saved [Stasjon]
     */
    fun saveStasjon(stasjonPostForm: StasjonPostForm): Either<ServiceError, Stasjon>

    /**
     * Update a stasjon.
     * @param stasjonUpdateForm Stasjon to update
     * @return Either a [ServiceError] or the updated [Stasjon]
     */
    fun updateStasjon(stasjonUpdateForm: StasjonUpdateForm): Either<ServiceError, Stasjon>

    /**
     * Delete a stasjon.
     * @param id ID of stasjon to delete
     * @return A [ServiceError] on failure and the deleted [Stasjon] on success.
     */
    fun deleteStasjonById(id: Int): Either<ServiceError, Stasjon>
}
