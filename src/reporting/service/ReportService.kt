package ombruk.backend.reporting.service

import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm

object ReportService : IReportService {

    override fun saveReport(event: Event) = ReportRepository.insertReport(event)

    override fun updateReport(event: Event) = ReportRepository.updateReport(event)

    override fun updateReport(reportUpdateForm: ReportUpdateForm) = ReportRepository.updateReport(reportUpdateForm)

    override fun getReportById(reportID: Int) = ReportRepository.getReportByID(reportID)

    override fun getReports(reportGetForm: ReportGetForm) = ReportRepository.getReports(reportGetForm)


}