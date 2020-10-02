package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.partner.database.Partnere
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("ombruk.unittest.no.oslokommune.ombruk.uttaksdata.database.ReportRepository")

object Reports : IntIdTable("uttaksdata") {
    val uttakID = integer("uttak_id").references(UttakTable.id)
    val partnerID = integer("partner_id").references(Partnere.id).nullable()
    val stasjonID = integer("stasjon_id").references(Stasjoner.id)
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val weight = integer("weight").nullable()
    val uttaksdataedDateTime = datetime("modified_date_time").nullable()
}

object UttaksdataRepository : IUttaksdataRepository {

    override fun insertReport(uttak: Uttak) = runCatching {
        transaction {
            Reports.insertAndGetId {
                it[weight] = null
                it[uttaksdataedDateTime] = null
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

    override fun updateReport(uttaksdataUpdateForm: UttaksdataUpdateForm): Either<RepositoryError, Uttaksdata> = runCatching {
        transaction {
            Reports.update({ Reports.id eq uttaksdataUpdateForm.id }) {
                it[weight] = uttaksdataUpdateForm.weight
                it[uttaksdataedDateTime] = LocalDateTime.now()
            }
        }
    }
        .onFailure { logger.error("Failed to update uttaksdata: ${it.message}") }
        .fold(
            {
                Either.cond(
                    it > 0,
                    { getReportByID(uttaksdataUpdateForm.id) },
                    { RepositoryError.NoRowsFound("ID ${uttaksdataUpdateForm.id} does not exist!") }).flatMap { it }
            },
            { RepositoryError.UpdateError("Failed to update uttaksdata").left() }
        )
    //getReportByID(uttaksdataUpdateForm.id)

    override fun updateReport(uttak: Uttak): Either<RepositoryError, Unit> = kotlin.runCatching {
        transaction {
            Reports.update({ Reports.uttakID eq uttak.id }) {
                it[startDateTime] = uttak.startDateTime
                it[endDateTime] = uttak.endDateTime
            }
        }
    }
        .onFailure { logger.error("Failed to update uttaksdata: ${it.message}") }
        .fold(
            { Either.cond(it > 0, { Unit }, { RepositoryError.NoRowsFound("uttakId ${uttak.id} does not exist!") }) },
            { RepositoryError.UpdateError("Failed to update uttaksdata").left() })


    override fun getReportByID(uttaksdataID: Int): Either<RepositoryError, Uttaksdata> = transaction {
        runCatching {
            (Reports innerJoin Stasjoner).select { Reports.id eq uttaksdataID }.map { toReport(it) }.firstOrNull()
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID $uttaksdataID does not exist!") }) },
                { RepositoryError.SelectError(it.message).left() }
            )
    }


    override fun getReports(uttaksdataGetForm: UttaksdataGetForm?): Either<RepositoryError, List<Uttaksdata>> = transaction {
        runCatching {
            val query = (Reports innerJoin Stasjoner).selectAll()
            if (uttaksdataGetForm != null) {
                uttaksdataGetForm.uttakId?.let { query.andWhere { Reports.uttakID eq it } }
                uttaksdataGetForm.stasjonId?.let { query.andWhere { Reports.stasjonID eq it } }
                uttaksdataGetForm.partnerId?.let { query.andWhere { Reports.partnerID eq it } }
                uttaksdataGetForm.fromDate?.let { query.andWhere { Reports.startDateTime.greaterEq(it) } }
                uttaksdataGetForm.toDate?.let { query.andWhere { Reports.endDateTime.lessEq(it) } }
            }
            query.mapNotNull { toReport(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })
    }


    private fun toReport(resultRow: ResultRow): Uttaksdata {
        return Uttaksdata(
            resultRow[Reports.id].value,
            resultRow[Reports.uttakID],
            resultRow[Reports.partnerID],
            toStasjon(resultRow),
            resultRow[Reports.startDateTime],
            resultRow[Reports.endDateTime],
            resultRow[Reports.weight],
            resultRow[Reports.uttaksdataedDateTime]
        )
    }
}
