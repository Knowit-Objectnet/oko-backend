package ombruk.backend.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.model.Partner
import ombruk.backend.model.Report
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("ombruk.reporting.database.ReportRepository")

object Reports : IntIdTable("reports") {
    val eventID = integer("event_id").references(Events.id)
    val partnerID = integer("partner_id").references(Partners.id)
    val weight = integer("weight")
    val createdDateTime = datetime("created_date_time")
}

object ReportRepository : IReportRepository {
    override fun insertReport(report: Report) = runCatching {
        transaction {
            Reports.insertAndGetId {
                it[weight] = report.weight
                it[eventID] = report.eventId
                it[partnerID] = report.partner.id
                it[createdDateTime] = LocalDateTime.now()
            }.value
        }
    }
        .onFailure { logger.error("Failed to insert station to db") }
        .fold({ report.copy(reportID = it).right() }, { RepositoryError.InsertError(it.message).left() })

    override fun updateReport(report: Report): Either<RepositoryError, Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteReport(reportID: Int) =
        kotlin.runCatching { Reports.deleteWhere { Reports.id eq reportID } }
            .onFailure { logger.error("Failed to delete report in DB: $reportID not found") }
            .fold({
                Either.cond(it > 0, { Unit }, { RepositoryError.NoRowsFound("$reportID not found") })
            },
                { RepositoryError.DeleteError(it.message).left() })

    override fun getReportByID(reportID: Int): Either<RepositoryError.NoRowsFound, Report> = runCatching {
        Reports.select { Reports.id eq reportID }
            .map { toReport(it) }.first()
    }
        .onFailure { logger.error(it.message) }
        .fold({ it.right() }, { RepositoryError.NoRowsFound(it.message).left() })

    override fun getReports(): Either<RepositoryError, List<Report>> =
        runCatching {
            Reports.selectAll().map { toReport(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })

    override fun getReportsByPartnerID(partnerID: Int): Either<RepositoryError.NoRowsFound, List<Report>> =
        runCatching {
            Reports.select(Reports.partnerID eq partnerID)
                .map { toReport(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.NoRowsFound(it.message).left() })

    private fun toReport(resultRow: ResultRow): Report {
        return Report(
            resultRow[Reports.id].value,
            resultRow[Reports.eventID],
            Partner(resultRow[Reports.partnerID], ""),
            resultRow[Reports.weight],
            resultRow[Reports.createdDateTime]
        )
    }

}
