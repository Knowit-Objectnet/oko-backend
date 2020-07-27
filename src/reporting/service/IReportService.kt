package ombruk.backend.reporting.service

import arrow.core.Either
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.ServiceError

interface IReportService {
    fun saveReport(report: Report): Either<ServiceError, Report>
    fun getReportById(reportID: Int): Either<ServiceError, Report>
    fun getReports(): Either<ServiceError, List<Report>>
    fun getReportsByPartnerId(partnerID: Int): Either<ServiceError, List<Report>>
    fun deleteReportByEventId(eventID: Int): Either<Throwable, Unit>
    // TODO: Maybe add getReports(partnerID: Int)
}