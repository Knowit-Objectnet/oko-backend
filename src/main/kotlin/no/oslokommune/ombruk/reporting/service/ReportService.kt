package no.oslokommune.ombruk.reporting.service

import arrow.core.Either
import no.oslokommune.ombruk.event.model.Event
import no.oslokommune.ombruk.reporting.database.ReportRepository
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.shared.error.ServiceError

object ReportService : IReportService {

    override fun saveReport(event: Event): Either<ServiceError, Report> = ReportRepository.insertReport(event)

    override fun updateReport(event: Event): Either<ServiceError, Unit> = ReportRepository.updateReport(event)

    override fun updateReport(reportUpdateForm: ReportUpdateForm): Either<ServiceError, Report> =
        ReportRepository.updateReport(reportUpdateForm)

    override fun getReportById(reportID: Int): Either<ServiceError, Report> = ReportRepository.getReportByID(reportID)

    override fun getReports(reportGetForm: ReportGetForm): Either<ServiceError, List<Report>> =
        ReportRepository.getReports(reportGetForm)


}