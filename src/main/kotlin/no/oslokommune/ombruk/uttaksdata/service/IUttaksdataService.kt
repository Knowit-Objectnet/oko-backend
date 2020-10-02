package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.ServiceError

interface IUttaksdataService {
    /**
     * Saves a [Uttaksdata] to the database. Only available internally, and is automatically called when an [Uttak] is created.
     *
     * @param uttak A stored [Uttak] object
     * @return A [ServiceError] on failure and a [Uttaksdata] on success.
     */
    fun saveReport(uttak: Uttak): Either<ServiceError, Uttaksdata>

    /**
     * Updates a stored [Uttaksdata]. Only available internally, and is automatically called when an [Uttak] is updated.
     *
     * @param uttak A stored [Uttak] object.
     * @return A [ServiceError] on failure and a [Unit] on success.
     */
    fun updateReport(uttak: Uttak): Either<ServiceError, Unit>

    /**
     * Sets the weight of a stored [Uttaksdata]. Available through the API.
     *
     * @param uttaksdataUpdateForm a [UttaksdataUpdateForm] where the [UttaksdataUpdateForm.id] corresponds to a stored [Uttaksdata.uttaksdataId].
     * @return a [ServiceError] on failure and a [Uttaksdata] on success.
     */
    fun updateReport(uttaksdataUpdateForm: UttaksdataUpdateForm): Either<ServiceError, Uttaksdata>

    /**
     * Gets a [Uttaksdata] that corresponds with the specified [uttaksdataID].
     *
     * @param uttaksdataID ID of a stored [Uttaksdata]
     * @return A [ServiceError] on failure and a [Uttaksdata] on success.
     */
    fun getReportById(uttaksdataID: Int): Either<ServiceError, Uttaksdata>

    /**
     * Gets a list of [Uttaksdata] objects specified by parameters in a [UttaksdataGetForm]
     *
     * @param uttaksdataGetForm a [UttaksdataGetForm] with values used to filter results. Null values are not used for filtering.
     * @return A [ServiceError] on failure and a [List] of [Uttaksdata] objects on success.
     */
    fun getReports(uttaksdataGetForm: UttaksdataGetForm): Either<ServiceError, List<Uttaksdata>>
}