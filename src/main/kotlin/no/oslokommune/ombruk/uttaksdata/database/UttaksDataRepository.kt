package no.oslokommune.ombruk.uttaksdata.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataGetForm
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataUpdateForm
import no.oslokommune.ombruk.uttaksdata.model.Uttaksdata
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.database.UttakRepository.toUttak
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttaksdata.form.UttaksdataPostForm
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger("ombruk.unittest.no.oslokommune.ombruk.uttaksdata.database.ReportRepository")

object UttaksdataTable : IntIdTable("uttaksdata") {
    val uttakID =               integer("uttak_id").references(UttakTable.id)
    val vekt =                  integer("vekt")
    val rapportertTidspunkt =   datetime("rapportert_tidspunkt")
    val slettetTidspunkt =      datetime("slettet_tidspunkt").nullable()
}

object UttaksDataRepository : IUttaksDataRepository {

    override fun insertUttaksdata(form: UttaksdataPostForm) = runCatching {
            transaction {
                UttaksdataTable.insertAndGetId {
                    it[vekt] = form.vekt
                    it[uttakID] = form.uttakID
                    it[rapportertTidspunkt] = form.rapportertTidspunkt
                }.value
            }
        }
        .onFailure { logger.error("Failed to insert stasjon to db: ${it.message}") }
        .fold({ getUttaksDataByID(it) }, { RepositoryError.InsertError("SQL error").left() })

    override fun updateUttaksdata(form: UttaksdataUpdateForm): Either<RepositoryError, Uttaksdata> = runCatching {
        transaction {
            UttaksdataTable.update({ UttaksdataTable.id eq form.id and UttaksdataTable.rapportertTidspunkt.isNotNull()}) {
                row ->
                    form.uttakID?.let { row[uttakID] = it }
                    form.vekt?.let { row[vekt] = it }
                    form.rapportertTidspukt?.let { row[rapportertTidspunkt] = it }
            }
        }
    }
        .onFailure { logger.error("Failed to update uttaksdata: ${it.message}") }
        .fold(
            {
                Either.cond(
                    it > 0,
                    { getUttaksDataByID(form.id) },
                    { RepositoryError.NoRowsFound("ID ${form.id} does not exist!") }).flatMap { it }
            },
            { RepositoryError.UpdateError("Failed to update uttaksdata").left() }
        )

    override fun getUttaksDataByID(uttaksdataID: Int): Either<RepositoryError, Uttaksdata> = transaction {
        runCatching {
            UttaksdataTable.select {
                UttaksdataTable.id eq uttaksdataID and UttaksdataTable.rapportertTidspunkt.isNotNull()
            }.map { toUttaksdata(it) }.firstOrNull()
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID $uttaksdataID does not exist!") }) },
                { RepositoryError.SelectError(it.message).left() }
            )
    }

    override fun getUttakByUttaksDataID(uttaksdataID: Int): Either<RepositoryError, Uttak> = transaction {
        runCatching {
            (UttakTable innerJoin UttaksdataTable).select {
                UttakTable.id eq UttaksdataTable.uttakID
            }.map { toUttak(it) }.firstOrNull()
        }
                .onFailure { logger.error(it.message) }
                .fold(
                        { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID $uttaksdataID does not exist!") }) },
                        { RepositoryError.SelectError(it.message).left() }
                )
    }

    override fun getUttaksData(form: UttaksdataGetForm?): Either<RepositoryError, List<Uttaksdata>> = transaction {
        runCatching {
            val query = UttaksdataTable.selectAll()
            if (form != null) {
                form.uttakId?.let { query.andWhere { UttaksdataTable.uttakID eq it } }
                form.minVekt?.let { query.andWhere { UttaksdataTable.vekt.greaterEq(it) } }
                form.maxVekt?.let { query.andWhere { UttaksdataTable.vekt.lessEq(it) } }
                form.fraRapportertTidspunkt?.let { query.andWhere { UttaksdataTable.rapportertTidspunkt.lessEq(it) } }
                form.tilRapportertTidspunkt?.let { query.andWhere { UttaksdataTable.rapportertTidspunkt.greaterEq(it) } }
            }
            query.mapNotNull { toUttaksdata(it) }
        }
            .onFailure { logger.error(it.message) }
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })
    }

    override fun deleteByUttakId(uttakId: Int): Either<RepositoryError, Unit> = runCatching {
        transaction {
            UttaksdataTable.update({ UttaksdataTable.id eq uttakId and UttaksdataTable.slettetTidspunkt.isNotNull()}) { row ->
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


    private fun toUttaksdata(resultRow: ResultRow): Uttaksdata {
        return Uttaksdata(
            resultRow[UttaksdataTable.id].value,
            resultRow[UttaksdataTable.uttakID],
            resultRow[UttaksdataTable.vekt],
            resultRow[UttaksdataTable.rapportertTidspunkt]
        )
    }
}
