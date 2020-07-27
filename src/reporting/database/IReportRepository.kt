package ombruk.backend.reporting.database

import arrow.core.Either
import ombruk.backend.calendar.model.Event
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.reporting.model.Report

interface IReportRepository {

    fun insertReport(event: Event): Either<RepositoryError, Report>

    fun updateReport(report: Report): Either<RepositoryError, Unit>

    fun deleteReport(reportID: Int): Either<RepositoryError, Unit>

    fun getReportByID(reportID: Int): Either<RepositoryError, Report>

    fun getReports(): Either<RepositoryError, List<Report>>

    fun getReportsByPartnerID(partnerID: Int): Either<RepositoryError.NoRowsFound, List<Report>>
}