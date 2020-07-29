package ombruk.backend.reporting.service

import arrow.core.Either
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.ServiceError

interface IReportService {
    fun saveReport(event: Event): Either<ServiceError, Report>
    fun updateReport(event: Event): Either<ServiceError, Unit>
    fun updateReport(reportUpdateForm: ReportUpdateForm): Either<ServiceError, Report>
    fun getReportById(reportID: Int): Either<ServiceError, Report>
    fun getReports(reportGetForm: ReportGetForm): Either<ServiceError, List<Report>>
}