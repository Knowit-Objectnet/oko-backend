package ombruk.backend.reporting.service

import arrow.core.leftIfNull
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object ReportService : IReportService {
    private val logger = LoggerFactory.getLogger("ombruk.reporting.service.PartnerService")

    override fun saveReport(event: Event) = ReportRepository.insertReport(event)

    override fun updateReport(event: Event) = ReportRepository.updateReport(event)

    override fun updateReport(reportUpdateForm: ReportUpdateForm) = ReportRepository.updateReport(reportUpdateForm)

    override fun getReportById(reportID: Int) = ReportRepository.getReportByID(reportID)

    override fun getReports(reportGetForm: ReportGetForm) = ReportRepository.getReports(reportGetForm)


}