package ombruk.backend.database

import arrow.core.Either
import ombruk.backend.model.Report

interface IReportRepository {

    fun insertReport(report: Report): Either<RepositoryError, Report>

    fun updateReport(report: Report): Either<RepositoryError, Unit>

    fun deleteReport(reportID: Int): Either<RepositoryError, Unit>

    fun getReportByID(reportID: Int): Either<RepositoryError, Report>

    fun getReports(): Either<RepositoryError, List<Report>>

    fun getReportsByPartnerID(partnerID: Int): Either<RepositoryError.NoRowsFound, List<Report>>
}