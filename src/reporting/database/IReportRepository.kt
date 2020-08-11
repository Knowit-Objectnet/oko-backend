package ombruk.backend.reporting.database

import arrow.core.Either
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.RepositoryError

interface IReportRepository {

    /**
     * Inserts a report into the database. This function is automatically called when an event is saved, and should not
     * be used under any other circumstances. Not available through API.
     *
     * @param event An [Event] object that has been stored to the database.
     * @return A [RepositoryError] on failure and a [Report] on success.
     */
    fun insertReport(event: Event): Either<RepositoryError, Report>

    /**
     * Updates the weight of a specific report. This function is intended for Partner uses, and only allows for the
     * weight of a report to be updated.
     *
     * @param reportUpdateForm A [ReportUpdateForm] where [ReportUpdateForm.id] corresponds to a stored [Report]
     * @return A [RepositoryError] on failure and an updated [Report] on success.
     */
    fun updateReport(reportUpdateForm: ReportUpdateForm): Either<RepositoryError, Report>

    /**
     * Updates [Report.startDateTime] and [Report.endDateTime] for the corresponding ID. This function is automatically called
     * when an event is updated, and should not be used under any other circumstances.
     *
     * @param event an [Event] object where [Event.id] corresponds to a stored [Report.eventID].
     * @return A [RepositoryError] on failure and [Unit] on success.
     */
    fun updateReport(event: Event): Either<RepositoryError, Unit>

    /**
     * Gets a specific [Report] by its [Report.reportID]. Must exist in the database.
     *
     * @param reportID An ID that corresponds to a [Report.reportID].
     * @return A [RepositoryError] on failure and a [Report] on success.
     */
    fun getReportByID(reportID: Int): Either<RepositoryError, Report>

    /**
     * Gets a [List] of [Report] objects specified by parameters in a [ReportGetForm].
     *
     * @param reportGetForm A [ReportGetForm]. If null, all events are fetched from the DB.
     * @return A [RepositoryError] on failure and a [List] of [Report] objects on success.
     */
    fun getReports(reportGetForm: ReportGetForm?): Either<RepositoryError, List<Report>>

}