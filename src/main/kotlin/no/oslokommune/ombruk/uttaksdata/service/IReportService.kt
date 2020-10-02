package no.oslokommune.ombruk.uttaksdata.service

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.ReportGetForm
import no.oslokommune.ombruk.uttaksdata.form.ReportUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Report
import no.oslokommune.ombruk.shared.error.ServiceError

interface IReportService {
    /**
     * Saves a [Report] to the database. Only available internally, and is automatically called when an [Uttak] is created.
     *
     * @param uttak A stored [Uttak] object
     * @return A [ServiceError] on failure and a [Report] on success.
     */
    fun saveReport(uttak: Uttak): Either<ServiceError, Report>

    /**
     * Updates a stored [Report]. Only available internally, and is automatically called when an [Uttak] is updated.
     *
     * @param uttak A stored [Uttak] object.
     * @return A [ServiceError] on failure and a [Unit] on success.
     */
    fun updateReport(uttak: Uttak): Either<ServiceError, Unit>

    /**
     * Sets the weight of a stored [Report]. Available through the API.
     *
     * @param uttaksdataUpdateForm a [ReportUpdateForm] where the [ReportUpdateForm.id] corresponds to a stored [Report.uttaksdataId].
     * @return a [ServiceError] on failure and a [Report] on success.
     */
    fun updateReport(uttaksdataUpdateForm: ReportUpdateForm): Either<ServiceError, Report>

    /**
     * Gets a [Report] that corresponds with the specified [uttaksdataID].
     *
     * @param uttaksdataID ID of a stored [Report]
     * @return A [ServiceError] on failure and a [Report] on success.
     */
    fun getReportById(uttaksdataID: Int): Either<ServiceError, Report>

    /**
     * Gets a list of [Report] objects specified by parameters in a [ReportGetForm]
     *
     * @param uttaksdataGetForm a [ReportGetForm] with values used to filter results. Null values are not used for filtering.
     * @return A [ServiceError] on failure and a [List] of [Report] objects on success.
     */
    fun getReports(uttaksdataGetForm: ReportGetForm): Either<ServiceError, List<Report>>
}