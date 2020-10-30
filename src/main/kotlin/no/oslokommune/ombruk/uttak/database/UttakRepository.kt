package no.oslokommune.ombruk.uttak.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.uttak.form.UttakDeleteForm
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttak.model.toWeekDayList
import no.oslokommune.ombruk.partner.database.Samarbeidspartnere
import no.oslokommune.ombruk.partner.database.toPartner
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.model.UttaksType
import no.oslokommune.ombruk.uttaksdata.database.UttaksdataRepository
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object UttakTable : IntIdTable("uttak") {
    val endretTidspunkt = datetime("endret_tidspunkt")
    val slettetTidspunkt = datetime("slettet_tidspunkt").nullable()
    val type = enumerationByName("type", 64, UttaksType::class)
    val samarbeidspartnerID = integer("samarbeidspartner_id").references(Samarbeidspartnere.id).nullable()
    val startTidspunkt = datetime("start_tidspunkt")
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val stasjonID = integer("stasjon_id").references(Stasjoner.id)
    val gjentakelsesRegelID = integer("gjentakelsesregel_id").references(GjentakelsesRegelTable.id).nullable()
    val beskrivelse = text("beskrivelse").nullable()
}

object UttakRepository : IUttakRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.UttakRepository")

    override fun insertUttak(uttakPostForm: UttakPostForm): Either<RepositoryError, Uttak> = runCatching {
        UttakTable.insertAndGetId {
            it[startTidspunkt] = uttakPostForm.startTidspunkt
            it[sluttTidspunkt] = uttakPostForm.sluttTidspunkt
            it[gjentakelsesRegelID] = uttakPostForm.gjentakelsesRegel?.id
            it[stasjonID] = uttakPostForm.stasjonID
            it[samarbeidspartnerID] = uttakPostForm.samarbeidspartnerID
            it[type] = uttakPostForm.type
            it[endretTidspunkt] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        }.value
    }
        .onFailure { logger.error("Failed to save uttak to DB; ${it.message}") }
        .fold(
            { getUttakByID(it) },
            { RepositoryError.InsertError("SQL error").left() }
        )


    override fun updateUttak(uttak: UttakUpdateForm): Either<RepositoryError, Uttak> = runCatching {
        transaction {
            UttakTable.update({ UttakTable.id eq uttak.id }) { row ->
                uttak.startTidspunkt?.let { row[startTidspunkt] = uttak.startTidspunkt }
                uttak.sluttTidspunkt?.let { row[sluttTidspunkt] = uttak.sluttTidspunkt }
                uttak.stasjonID?.let { row[stasjonID] = uttak.stasjonID }
                uttak.samarbeidspartnerID?.let { row[samarbeidspartnerID] = uttak.samarbeidspartnerID }
                uttak.gjentakelsesRegel?.let {
                    row[gjentakelsesRegelID] = uttak.gjentakelsesRegel.id
                    GjentakelsesRegelTable.update({ GjentakelsesRegelTable.id eq uttak.gjentakelsesRegel.id }) {
                        gjenRow ->
                            uttak.gjentakelsesRegel.antall?.let { gjenRow[antall] = uttak.gjentakelsesRegel.antall }
                            uttak.gjentakelsesRegel.intervall?.let { gjenRow[intervall] = uttak.gjentakelsesRegel.intervall }
                            uttak.gjentakelsesRegel.dager?.let { gjenRow[dager] = uttak.gjentakelsesRegel.dager.toString() }
                            uttak.gjentakelsesRegel.sluttTidspunkt?.let { gjenRow[sluttTidspunkt] = uttak.gjentakelsesRegel.sluttTidspunkt }
                            gjenRow[endretTidspunkt] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                    }
                }
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { getUttakByID(uttak.id) },
            { RepositoryError.UpdateError(it.message).left() }
        )

    override fun deleteUttak(uttakDeleteForm: UttakDeleteForm): Either<RepositoryError, Unit> =
            runCatching {
                transaction {
                    UttakTable.update({ UttakTable.id eq uttakDeleteForm.id }) { row ->
                        row[slettetTidspunkt] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                    }
                    GjentakelsesRegelTable.deleteGjentakelsesRegel(uttakDeleteForm.id)

                }
            }
            .onFailure { logger.error("Failed to mark Uttak as deleted:${it.message}") }
            .fold(
                { Unit.right() },
                { RepositoryError.DeleteError("Failed to delete uttak.").left() }
            )

    override fun getUttakByID(uttakID: Int): Either<RepositoryError, Uttak> = runCatching {
        transaction {
            // leftJoin GjentakelsesRegels because not all uttak are recurring.
            (UttakTable innerJoin Stasjoner leftJoin Samarbeidspartnere leftJoin GjentakelsesRegelTable).select { UttakTable.id eq uttakID }
                .map { toUttak(it) }.firstOrNull()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID does not exist!") }) },
            { RepositoryError.SelectError(it.message).left() })

    override fun getUttakByUttaksDataID(uttaksdataID: Int): Either<RepositoryError, Uttak> =
        UttaksdataRepository.getUttakByUttaksDataID(uttaksdataID)

    @KtorExperimentalLocationsAPI
    override fun getUttak(uttakGetForm: UttakGetForm?): Either<RepositoryError, List<Uttak>> =
            runCatching {
                transaction {
                    val query = (UttakTable innerJoin Stasjoner innerJoin Samarbeidspartnere leftJoin GjentakelsesRegelTable).selectAll()
                    if (uttakGetForm != null) {
                        //  TODO: Stop the debugger here
                        query.andWhere { UttakTable.slettetTidspunkt.isNull() } // Verify for deleted stasjon, partner, etc...
                        uttakGetForm.id?.let { query.andWhere { UttakTable.id eq it } }
                        uttakGetForm.stasjonID?.let { query.andWhere { UttakTable.stasjonID eq it } }
                        uttakGetForm.partnerID?.let { query.andWhere { UttakTable.samarbeidspartnerID eq it } }
                        uttakGetForm.gjentakelsesRegelID?.let { query.andWhere { UttakTable.gjentakelsesRegelID eq it } }
                        uttakGetForm.startTidspunkt?.let { query.andWhere { UttakTable.startDateTime.greaterEq(it) } }
                        uttakGetForm.sluttTidspunkt?.let { query.andWhere { UttakTable.endDateTime.lessEq(it) } }
                    }
                    query.mapNotNull { toUttak(it) }
                }
            }
                    .onFailure { logger.error(it.message) }
                    .fold(
                            { it.right() },
                            { RepositoryError.SelectError(it.message).left() }
                    )

    override fun exists(id: Int) = transaction { UttakTable.select { UttakTable.id eq id }.count() >= 1 }


    fun toUttak(row: ResultRow?): Uttak? {
        if (row == null) return null

        return Uttak(
                row[UttakTable.id].value,
                row[UttakTable.startDateTime],
                row[UttakTable.endDateTime],
                toStasjon(row),
                toPartner(row),
                UttakRepository.getGetGjentakelsesRegelFromResultRow(row),
                row[UttakTable.type]
        )
    }


    private fun getGetGjentakelsesRegelFromResultRow(row: ResultRow): GjentakelsesRegel? {
        if (!row.hasValue(GjentakelsesRegelTable.id) || row.getOrNull(
                GjentakelsesRegelTable.id
            ) == null
        ) return null

        return GjentakelsesRegel(
            row[GjentakelsesRegelTable.id].value,
            row[GjentakelsesRegelTable.sluttTidspunkt],
            row[GjentakelsesRegelTable.dager]?.toWeekDayList(),
            row[GjentakelsesRegelTable.interval],
            row[GjentakelsesRegelTable.antall]
        )
    }
}