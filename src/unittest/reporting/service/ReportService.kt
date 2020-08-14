package ombruk.backend.reporting.service

import arrow.core.Either
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.ServiceError

object ReportService : IReportService {

    override fun saveReport(event: Event): Either<ServiceError, Report> = ReportRepository.insertReport(event)

    override fun updateReport(event: Event): Either<ServiceError, Unit> = ReportRepository.updateReport(event)

    override fun updateReport(reportUpdateForm: ReportUpdateForm): Either<ServiceError, Report> =
        ReportRepository.updateReport(reportUpdateForm)

    override fun getReportById(reportID: Int): Either<ServiceError, Report> = ReportRepository.getReportByID(reportID)

    override fun getReports(reportGetForm: ReportGetForm): Either<ServiceError, List<Report>> =
        ReportRepository.getReports(reportGetForm)


}