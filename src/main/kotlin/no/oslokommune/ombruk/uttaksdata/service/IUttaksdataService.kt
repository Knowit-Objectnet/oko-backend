package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.ServiceError
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm

interface IUttaksdataService {
    /**
     * Saves a [Uttaksdata] to the database. Only available internally, and is automatically called when an [Uttak] is created.
     *
     * @param uttaksdataPostForm A stored [Uttak] object
     * @return A [ServiceError] on failure and a [Uttaksdata] on success.
     */
    fun saveUttaksdata(uttaksdataPostForm: UttaksdataPostForm): Either<ServiceError, Uttaksdata>

    /**
     * Updates a stored [Uttaksdata]. Only available internally, and is automatically called when an [Uttak] is updated.
     *
     * @param uttaksdata A stored [Uttak] object.
     * @return A [ServiceError] on failure and a [Unit] on success.
     */
    fun updateUttaksdata(uttaksdata: UttaksdataUpdateForm): Either<ServiceError, Uttaksdata>

    /**
     * Gets a [Uttaksdata] that corresponds with the specified [uttaksdataID].
     *
     * @param uttaksdataID ID of a stored [Uttaksdata]
     * @return A [ServiceError] on failure and a [Uttaksdata] on success.
     */
    fun getUttaksdataById(uttaksdataID: Int): Either<ServiceError, Uttaksdata>

    /**
     * Gets a list of [Uttaksdata] objects specified by parameters in a [UttaksdataGetForm]
     *
     * @param uttaksdataGetForm a [UttaksdataGetForm] with values used to filter results. Null values are not used for filtering.
     * @return A [ServiceError] on failure and a [List] of [Uttaksdata] objects on success.
     */
    fun getUttaksdata(uttaksdataGetForm: UttaksdataGetForm): Either<ServiceError, List<Uttaksdata>>
}