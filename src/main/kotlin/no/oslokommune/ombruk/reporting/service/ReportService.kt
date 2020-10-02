package no.oslokommune.ombruk.reporting.service

import arrow.core.Either
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.reporting.database.ReportRepository
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.shared.error.ServiceError

object ReportService : IReportService {

    override fun saveReport(uttak: Uttak): Either<ServiceError, Report> = ReportRepository.insertReport(uttak)

    override fun updateReport(uttak: Uttak): Either<ServiceError, Unit> = ReportRepository.updateReport(uttak)

    override fun updateReport(reportUpdateForm: ReportUpdateForm): Either<ServiceError, Report> =
        ReportRepository.updateReport(reportUpdateForm)

    override fun getReportById(reportID: Int): Either<ServiceError, Report> = ReportRepository.getReportByID(reportID)

    override fun getReports(reportGetForm: ReportGetForm): Either<ServiceError, List<Report>> =
        ReportRepository.getReports(reportGetForm)


}