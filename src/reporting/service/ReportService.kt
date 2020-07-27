package ombruk.backend.reporting.service

import arrow.core.Left
import arrow.core.Right
import arrow.core.leftIfNull
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.database.ReportRepository
import ombruk.backend.reporting.database.Reports
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object ReportService : IReportService {
    private val logger = LoggerFactory.getLogger("ombruk.reporting.service.PartnerService")

    override fun saveReport(event: Event) = ReportRepository.insertReport(event)

    override fun getReportById(id: Int) = transaction {
        ReportRepository.getReportByID(id).leftIfNull {
            ServiceError(
                "No report with ID $id exists"
            )
        }
    }


    override fun getReports() = transaction {
        ReportRepository.getReports().leftIfNull { ServiceError("No reports") }
    }

    override fun getReportsByPartnerId(partnerID: Int) = transaction {
        ReportRepository.getReports()
    }

    override fun deleteReportByEventId(eventID: Int) = runCatching {
        transaction {
            Reports.deleteWhere { Reports.eventID eq eventID }
        }
    }.fold({ Right(Unit) }, { logger.error("Failed to delete partner in DB: ${it.message}"); Left(it) })


}