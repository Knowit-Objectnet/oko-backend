package ombruk.backend.reporting.database

import arrow.core.Either
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.reporting.model.Report

interface IReportRepository {

    fun insertReport(event: Event): Either<RepositoryError, Report>

    fun updateReport(reportUpdateForm: ReportUpdateForm): Either<RepositoryError, Report>

    fun updateReport(event: Event): Either<RepositoryError, Unit>

//    fun deleteReport(reportID: Int): Either<RepositoryError, Unit>

    fun getReportByID(reportID: Int): Either<RepositoryError, Report>

    fun getReports(reportGetForm: ReportGetForm?): Either<RepositoryError, List<Report>>

}