package no.oslokommune.ombruk.reporting.service

import arrow.core.Either
import no.oslokommune.ombruk.event.model.Event
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.shared.error.ServiceError

interface IReportService {
    /**
     * Saves a [Report] to the database. Only available internally, and is automatically called when an [Event] is created.
     *
     * @param event A stored [Event] object
     * @return A [ServiceError] on failure and a [Report] on success.
     */
    fun saveReport(event: Event): Either<ServiceError, Report>

    /**
     * Updates a stored [Report]. Only available internally, and is automatically called when an [Event] is updated.
     *
     * @param event A stored [Event] object.
     * @return A [ServiceError] on failure and a [Unit] on success.
     */
    fun updateReport(event: Event): Either<ServiceError, Unit>

    /**
     * Sets the weight of a stored [Report]. Available through the API.
     *
     * @param reportUpdateForm a [ReportUpdateForm] where the [ReportUpdateForm.id] corresponds to a stored [Report.reportId].
     * @return a [ServiceError] on failure and a [Report] on success.
     */
    fun updateReport(reportUpdateForm: ReportUpdateForm): Either<ServiceError, Report>

    /**
     * Gets a [Report] that corresponds with the specified [reportID].
     *
     * @param reportID ID of a stored [Report]
     * @return A [ServiceError] on failure and a [Report] on success.
     */
    fun getReportById(reportID: Int): Either<ServiceError, Report>

    /**
     * Gets a list of [Report] objects specified by parameters in a [ReportGetForm]
     *
     * @param reportGetForm a [ReportGetForm] with values used to filter results. Null values are not used for filtering.
     * @return A [ServiceError] on failure and a [List] of [Report] objects on success.
     */
    fun getReports(reportGetForm: ReportGetForm): Either<ServiceError, List<Report>>
}