package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.ReportGetForm
import no.oslokommune.ombruk.uttaksdata.form.ReportUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Report
import no.oslokommune.ombruk.shared.error.RepositoryError

interface IReportRepository {

    /**
     * Inserts a uttaksdata into the database. This function is automatically called when an uttak is saved, and should not
     * be used under any other circumstances. Not available through API.
     *
     * @param uttak An [Uttak] object that has been stored to the database.
     * @return A [RepositoryError] on failure and a [Report] on success.
     */
    fun insertReport(uttak: Uttak): Either<RepositoryError, Report>

    /**
     * Updates the weight of a specific uttaksdata. This function is intended for Partner uses, and only allows for the
     * weight of a uttaksdata to be updated.
     *
     * @param uttaksdataUpdateForm A [ReportUpdateForm] where [ReportUpdateForm.id] corresponds to a stored [Report]
     * @return A [RepositoryError] on failure and an updated [Report] on success.
     */
    fun updateReport(uttaksdataUpdateForm: ReportUpdateForm): Either<RepositoryError, Report>

    /**
     * Updates [Report.startDateTime] and [Report.endDateTime] for the corresponding ID. This function is automatically called
     * when an uttak is updated, and should not be used under any other circumstances.
     *
     * @param uttak an [Uttak] object where [Uttak.id] corresponds to a stored [Report.uttakId].
     * @return A [RepositoryError] on failure and [Unit] on success.
     */
    fun updateReport(uttak: Uttak): Either<RepositoryError, Unit>

    /**
     * Gets a specific [Report] by its [Report.uttaksdataId]. Must exist in the database.
     *
     * @param uttaksdataID An ID that corresponds to a [Report.uttaksdataId].
     * @return A [RepositoryError] on failure and a [Report] on success.
     */
    fun getReportByID(uttaksdataID: Int): Either<RepositoryError, Report>

    /**
     * Gets a [List] of [Report] objects specified by parameters in a [ReportGetForm].
     *
     * @param uttaksdataGetForm A [ReportGetForm]. If null, all uttaks are fetched from the DB.
     * @return A [RepositoryError] on failure and a [List] of [Report] objects on success.
     */
    fun getReports(uttaksdataGetForm: ReportGetForm?): Either<RepositoryError, List<Report>>

}