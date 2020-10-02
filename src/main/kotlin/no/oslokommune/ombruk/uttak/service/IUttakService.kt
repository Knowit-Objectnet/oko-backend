package no.oslokommune.ombruk.uttak.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.shared.error.ServiceError

interface IUttakService {
    /**
     * Saves one or several uttak and automatically generates corresponding uttaksdata.
     * @param uttakPostForm An [UttakPostForm] that describes the uttak to be posted.
     * @return A [ServiceError] on failure and an [Uttak] on success. If saved uttak is recurring, the first uttak
     * is returned.
     */
    fun saveUttak(uttakPostForm: UttakPostForm): Either<ServiceError, Uttak>

    /**
     * Gets a specific uttak by it's [Uttak.id].
     * @param id The id of the [Uttak] to get. Must exist in db.
     * @return A [ServiceError] on failure and the corresponding [Uttak] on success.
     */
    fun getUttakByID(id: Int): Either<ServiceError, Uttak>

    /**
     * Gets a list of uttak constrained by the values passed into the [uttakGetForm].
     * @param uttakGetForm The constraints to apply to the query. If all properties are null, all uttak will be queried.
     * @return A [ServiceError] on failure and a [List] of [Uttak] objects on success.
     */
    @KtorExperimentalLocationsAPI
    fun getUttak(
            uttakGetForm: UttakGetForm? = null
    ): Either<ServiceError, List<Uttak>>

    /**
     * Deletes one or more uttak specified by the values passed into the [uttakDeleteForm].
     * @param uttakDeleteForm The constraints to apply to the query. If all properties are null, all uttak will be deleted.
     * @return A [ServiceError] on failure and a [List] of the deleted [Uttak] objects on success.
     */
    @KtorExperimentalLocationsAPI
    fun deleteUttak(uttakDeleteForm: UttakDeleteForm): Either<ServiceError, List<Uttak>>

    /**
     * Updates a singular uttak. Must be called several times to update all uttak belonging to a recurrence rule.
     * @param uttakUpdate A [UttakUpdateForm] containing the values to be updated. Only non-null values will be updated.
     * @return A [ServiceError] on failure and the updated [Uttak] on success.
     */
    fun updateUttak(uttakUpdate: UttakUpdateForm): Either<ServiceError, Uttak>
}
