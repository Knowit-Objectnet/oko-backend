package ombruk.backend.reporting.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Event
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.partner.model.Partner
import ombruk.backend.reporting.model.Report
import ombruk.backend.partner.database.Partners
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
    val stationID = integer("station_id").references(Stations.id)
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val weight = integer("weight").nullable()
    val createdDateTime = datetime("created_date_time")
}

object ReportRepository : IReportRepository {

    override fun insertReport(event: Event) = runCatching {
        transaction {
            Reports.insertAndGetId {
                it[weight] = null
                it[eventID] = event.id
                it[partnerID] = event.partner.id
                it[stationID] = event.station.id
                it[startDateTime] = event.startDateTime
                it[endDateTime] = event.endDateTime
                it[createdDateTime] = LocalDateTime.now()
            }.value
        }
    }
        .onFailure { logger.error("Failed to insert station to db: ${it.message}") }
        .fold({ getReportByID(it) }, { RepositoryError.InsertError("SQL error").left() })

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
        runCatching { Reports.selectAll().map { toReport(it) } }
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
            resultRow[Reports.partnerID],
            resultRow[Reports.stationID],
            resultRow[Reports.startDateTime],
            resultRow[Reports.endDateTime],
            resultRow[Reports.weight],
            resultRow[Reports.createdDateTime]
        )
    }

}
