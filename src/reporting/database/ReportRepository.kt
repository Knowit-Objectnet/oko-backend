package ombruk.backend.reporting.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.database.toStation
import ombruk.backend.calendar.model.Event
import ombruk.backend.partner.database.Partners
import ombruk.backend.reporting.form.ReportGetForm
import ombruk.backend.reporting.form.ReportUpdateForm
import ombruk.backend.reporting.model.Report
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("ombruk.unittest.reporting.database.ReportRepository")

object Reports : IntIdTable("reports") {
    val eventID = integer("event_id").references(Events.id)
    val partnerID = integer("partner_id").references(Partners.id).nullable()
    val stationID = integer("station_id").references(Stations.id)
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val weight = integer("weight").nullable()
    val reportedDateTime = datetime("reported_date_time").nullable()
}

object ReportRepository : IReportRepository {

    override fun insertReport(event: Event) = runCatching {
        transaction {
            Reports.insertAndGetId {
                it[weight] = null
                it[reportedDateTime] = null
                it[eventID] = event.id
                it[partnerID] = event.partner?.id
                it[stationID] = event.station.id
                it[startDateTime] = event.startDateTime
                it[endDateTime] = event.endDateTime
            }.value
        }
    }
        .onFailure { logger.error("Failed to insert station to db: ${it.message}") }
        .fold({ getReportByID(it) }, { RepositoryError.InsertError("SQL error").left() })

    override fun updateReport(reportUpdateForm: ReportUpdateForm): Either<RepositoryError, Report> = runCatching {
        transaction {
            Reports.update({ Reports.id eq reportUpdateForm.id }) {
                it[weight] = reportUpdateForm.weight
                it[reportedDateTime] = LocalDateTime.now()
            }
        }
    }
        .onFailure { logger.error("Failed to update report: ${it.message}") }
        .fold(
            {
                Either.cond(
                    it > 0,
                    { getReportByID(reportUpdateForm.id) },
                    { RepositoryError.NoRowsFound("ID ${reportUpdateForm.id} does not exist!") }).flatMap { it }
            },
            { RepositoryError.UpdateError("Failed to update report").left() }
        )
    //getReportByID(reportUpdateForm.id)

    override fun updateReport(event: Event): Either<RepositoryError, Unit> = kotlin.runCatching {
        transaction {
            Reports.update({ Reports.eventID eq event.id }) {
                it[startDateTime] = event.startDateTime
                it[endDateTime] = event.endDateTime
            }
        }
    }
        .onFailure { logger.error("Failed to update report: ${it.message}") }
        .fold(
            { Either.cond(it > 0, { Unit }, { RepositoryError.NoRowsFound("eventId ${event.id} does not exist!") }) },
            { RepositoryError.UpdateError("Failed to update report").left() })


    override fun getReportByID(reportID: Int): Either<RepositoryError, Report> = transaction {
        runCatching {
            (Reports innerJoin Stations).select { Reports.id eq reportID }.map { toReport(it) }.firstOrNull()
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID $reportID does not exist!") }) },
                { RepositoryError.SelectError(it.message).left() }
            )
    }


    override fun getReports(reportGetForm: ReportGetForm?): Either<RepositoryError, List<Report>> = transaction {
        runCatching {
            val query = (Reports innerJoin Stations).selectAll()
            if (reportGetForm != null) {
                reportGetForm.eventId?.let { query.andWhere { Reports.eventID eq it } }
                reportGetForm.stationId?.let { query.andWhere { Reports.stationID eq it } }
                reportGetForm.partnerId?.let { query.andWhere { Reports.partnerID eq it } }
                reportGetForm.fromDate?.let { query.andWhere { Reports.startDateTime.greaterEq(it) } }
                reportGetForm.toDate?.let { query.andWhere { Reports.endDateTime.lessEq(it) } }
            }
            query.mapNotNull { toReport(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })
    }


    private fun toReport(resultRow: ResultRow): Report {
        return Report(
            resultRow[Reports.id].value,
            resultRow[Reports.eventID],
            resultRow[Reports.partnerID],
            toStation(resultRow),
            resultRow[Reports.startDateTime],
            resultRow[Reports.endDateTime],
            resultRow[Reports.weight],
            resultRow[Reports.reportedDateTime]
        )
    }
}
