package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksDataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.UttaksData
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.model.Uttak
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger =
    LoggerFactory.getLogger("ombruk.unittest.no.oslokommune.ombruk.uttaksdata.database.ReportRepository")

object UttaksDataTable : Table("uttaksdata") {
    val uttakId = integer("uttak_id").references(UttakTable.id)
    val vekt = integer("vekt").nullable()
    val rapportertTidspunkt = datetime("rapportert_tidspunkt")
    val slettetTidspunkt = datetime("slettet_tidspunkt").nullable()
}

object UttaksDataRepository : IUttaksDataRepository {

    fun insertUttaksdata(uttak: Uttak) = runCatching {
        transaction {
            UttaksDataTable.insert {
                it[uttakId] = uttak.id
            }
        }
    }
        .onFailure { logger.error("Failed to insert UttaksData: ${it.message}") }
        .fold(
            { getUttaksDataById(uttak.id) },
            { RepositoryError.InsertError("SQL error").left() })

    override fun updateUttaksData(form: UttaksDataUpdateForm): Either<RepositoryError, UttaksData> = runCatching {
        println(form.uttakId)
        transaction {
            UttaksDataTable.update({ UttaksDataTable.uttakId eq form.uttakId }) { row ->
                form.vekt?.let {
                    row[vekt] = it
                    row[rapportertTidspunkt] = LocalDateTime.now()
                }
            }
        }
    }
        .onFailure { logger.error("Failed to update uttaksdata: ${it.message}") }
        .fold(
            {
                Either.cond(
                    it > 0,
                    { getUttaksDataById(form.uttakId) },
                    { RepositoryError.NoRowsFound("ID ${form.uttakId} does not exist!") }).flatMap { it }
            },
            { RepositoryError.UpdateError("Failed to update uttaksdata").left() }
        )

    override fun getUttaksDataById(uttaksdataId: Int): Either<RepositoryError, UttaksData> = transaction {
        runCatching {
            UttaksDataTable.select { UttaksDataTable.uttakId eq uttaksdataId }.map { toUttaksdata(it) }.firstOrNull()
        }
            .onFailure { logger.error(it.message) }
            .fold(
                {
                    Either.cond(it != null,
                        { it!! },
                        { RepositoryError.NoRowsFound("ID $uttaksdataId does not exist!") })
                },
                { RepositoryError.SelectError(it.message).left() }
            )
    }

    override fun getUttaksData(form: UttaksDataGetForm?): Either<RepositoryError, List<UttaksData>> = transaction {
        runCatching {
            val query = (UttaksDataTable innerJoin UttakTable).selectAll()
            query.andWhere { UttakTable.slettetTidspunkt.isNull() }
            if (form != null) {
                form.uttakId?.let { query.andWhere { UttaksDataTable.uttakId eq it } }
                form.minVekt?.let { query.andWhere { UttaksDataTable.vekt.greaterEq(it) } }
                form.maxVekt?.let { query.andWhere { UttaksDataTable.vekt.lessEq(it) } }
                form.stasjonId?.let { query.andWhere { UttakTable.stasjonID eq it } }
                form.partnerId?.let { query.andWhere { UttakTable.partnerID eq it } }
                form.fraRapportertTidspunkt?.let { query.andWhere { UttaksDataTable.rapportertTidspunkt.greaterEq(it) } }
                form.tilRapportertTidspunkt?.let { query.andWhere { UttaksDataTable.rapportertTidspunkt.lessEq(it) } }
            }
            query.mapNotNull { toUttaksdata(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })
    }

    override fun deleteByUttakId(uttakId: Int): Either<RepositoryError, Unit> = runCatching {
        transaction {
            UttaksDataTable.update({ UttaksDataTable.uttakId eq uttakId and UttaksDataTable.slettetTidspunkt.isNotNull() }) { row ->
                row[slettetTidspunkt] = LocalDateTime.now()
            }
        }
    }
        .onFailure { logger.error("Failed to update uttaksdata: ${it.message}") }
        .fold(
            {
                Either.cond(
                    it > 0,
                    { Unit.right() },
                    { RepositoryError.NoRowsFound("ID $uttakId does not exist!") }).flatMap { it }
            },
            { RepositoryError.UpdateError("Failed to update uttaksdata").left() }
        )


    private fun toUttaksdata(resultRow: ResultRow): UttaksData {
        return UttaksData(
            resultRow[UttaksDataTable.uttakId],
            resultRow[UttaksDataTable.vekt],
            resultRow[UttaksDataTable.rapportertTidspunkt]
        )
    }
}
