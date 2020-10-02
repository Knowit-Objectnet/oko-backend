package no.oslokommune.ombruk.reporting.database

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IReportRepository {

    /**
     * Inserts a report into the database. This function is automatically called when an uttak is saved, and should not
     * be used under any other circumstances. Not available through API.
     *
     * @param uttak An [Uttak] object that has been stored to the database.
     * @return A [RepositoryError] on failure and a [Report] on success.
     */
    fun insertReport(uttak: Uttak): Either<RepositoryError, Report>

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
     * when an uttak is updated, and should not be used under any other circumstances.
     *
     * @param uttak an [Uttak] object where [Uttak.id] corresponds to a stored [Report.uttakId].
     * @return A [RepositoryError] on failure and [Unit] on success.
     */
    fun updateReport(uttak: Uttak): Either<RepositoryError, Unit>

    /**
     * Gets a specific [Report] by its [Report.reportId]. Must exist in the database.
     *
     * @param reportID An ID that corresponds to a [Report.reportId].
     * @return A [RepositoryError] on failure and a [Report] on success.
     */
    fun getReportByID(reportID: Int): Either<RepositoryError, Report>

    /**
     * Gets a [List] of [Report] objects specified by parameters in a [ReportGetForm].
     *
     * @param reportGetForm A [ReportGetForm]. If null, all uttaks are fetched from the DB.
     * @return A [RepositoryError] on failure and a [List] of [Report] objects on success.
     */
    fun getReports(reportGetForm: ReportGetForm?): Either<RepositoryError, List<Report>>

}