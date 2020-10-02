package no.oslokommune.ombruk.reporting.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.partner.database.Partners
import no.oslokommune.ombruk.reporting.form.ReportGetForm
import no.oslokommune.ombruk.reporting.form.ReportUpdateForm
import no.oslokommune.ombruk.reporting.model.Report
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("ombruk.unittest.no.oslokommune.ombruk.reporting.database.ReportRepository")

object Reports : IntIdTable("reports") {
    val uttakID = integer("uttak_id").references(UttakTable.id)
    val partnerID = integer("partner_id").references(Partners.id).nullable()
    val stasjonID = integer("stasjon_id").references(Stasjoner.id)
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val weight = integer("weight").nullable()
    val reportedDateTime = datetime("reported_date_time").nullable()
}

object ReportRepository : IReportRepository {

    override fun insertReport(uttak: Uttak) = runCatching {
        transaction {
            Reports.insertAndGetId {
                it[weight] = null
                it[reportedDateTime] = null
                it[uttakID] = uttak.id
                it[partnerID] = uttak.partner?.id
                it[stasjonID] = uttak.stasjon.id
                it[startDateTime] = uttak.startDateTime
                it[endDateTime] = uttak.endDateTime
            }.value
        }
    }
        .onFailure { logger.error("Failed to insert stasjon to db: ${it.message}") }
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

    override fun updateReport(uttak: Uttak): Either<RepositoryError, Unit> = kotlin.runCatching {
        transaction {
            Reports.update({ Reports.uttakID eq uttak.id }) {
                it[startDateTime] = uttak.startDateTime
                it[endDateTime] = uttak.endDateTime
            }
        }
    }
        .onFailure { logger.error("Failed to update report: ${it.message}") }
        .fold(
            { Either.cond(it > 0, { Unit }, { RepositoryError.NoRowsFound("uttakId ${uttak.id} does not exist!") }) },
            { RepositoryError.UpdateError("Failed to update report").left() })


    override fun getReportByID(reportID: Int): Either<RepositoryError, Report> = transaction {
        runCatching {
            (Reports innerJoin Stasjoner).select { Reports.id eq reportID }.map { toReport(it) }.firstOrNull()
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID $reportID does not exist!") }) },
                { RepositoryError.SelectError(it.message).left() }
            )
    }


    override fun getReports(reportGetForm: ReportGetForm?): Either<RepositoryError, List<Report>> = transaction {
        runCatching {
            val query = (Reports innerJoin Stasjoner).selectAll()
            if (reportGetForm != null) {
                reportGetForm.uttakId?.let { query.andWhere { Reports.uttakID eq it } }
                reportGetForm.stasjonId?.let { query.andWhere { Reports.stasjonID eq it } }
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
            resultRow[Reports.uttakID],
            resultRow[Reports.partnerID],
            toStasjon(resultRow),
            resultRow[Reports.startDateTime],
            resultRow[Reports.endDateTime],
            resultRow[Reports.weight],
            resultRow[Reports.reportedDateTime]
        )
    }
}
