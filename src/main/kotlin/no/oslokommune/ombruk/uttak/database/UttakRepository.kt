package no.oslokommune.ombruk.uttak.database

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.extensions.either.foldable.fold
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.uttak.form.UttakGetForm
import no.oslokommune.ombruk.uttak.form.UttakPostForm
import no.oslokommune.ombruk.uttak.form.UttakUpdateForm
import no.oslokommune.ombruk.uttak.model.Uttak
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.uttak.model.toWeekDayList
import no.oslokommune.ombruk.partner.database.Partnere
import no.oslokommune.ombruk.partner.database.toPartner
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.model.UttaksType
import no.oslokommune.ombruk.uttaksdata.database.UttaksDataRepository
import no.oslokommune.ombruk.uttaksdata.database.UttaksdataTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

object UttakTable : IntIdTable("uttak") {
    val endretTidspunkt =       datetime("endret_tidspunkt")
    val slettetTidspunkt =      datetime("slettet_tidspunkt").nullable()
    val type =                  enumerationByName("type", 64, UttaksType::class)
    val partnerID =             integer("samarbeidspartner_id").references(Partnere.id).nullable()
    val startTidspunkt =        datetime("start_tidspunkt")
    val sluttTidspunkt =        datetime("slutt_tidspunkt")
    val stasjonID =             integer("stasjon_id").references(Stasjoner.id)
    val gjentakelsesRegelID =   integer("gjentakelsesregel_id").references(GjentakelsesRegelTable.id).nullable()
    val beskrivelse =           text("beskrivelse").nullable()
}

object UttakRepository : IUttakRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.UttakRepository")

    override fun insertUttak(uttakPostForm: UttakPostForm): Either<RepositoryError, Uttak> = runCatching {
        UttakTable.insertAndGetId {
            it[startTidspunkt] = uttakPostForm.startTidspunkt
            it[sluttTidspunkt] = uttakPostForm.sluttTidspunkt
            it[gjentakelsesRegelID] = uttakPostForm.gjentakelsesRegel?.id
            it[stasjonID] = uttakPostForm.stasjonId
            it[partnerID] = uttakPostForm.partnerId
            it[type] = uttakPostForm.type
            it[endretTidspunkt] = LocalDateTime.now()
        }.value
    }
        .onFailure { logger.error("Failed to save uttak to DB; ${it.message}") }
        .fold(
            { getUttakByID(it) },
            { RepositoryError.InsertError("SQL error").left() }
        )


    override fun updateUttak(uttak: UttakUpdateForm): Either<RepositoryError, Uttak> = runCatching {
        transaction {
            UttakTable.update({ UttakTable.id eq uttak.id and UttakTable.slettetTidspunkt.isNull() }) { row ->
                uttak.startTidspunkt?.let { row[startTidspunkt] = it }
                uttak.sluttTidspunkt?.let { row[sluttTidspunkt] = it }
                uttak.type?.let { row[type] = UttaksType.values().first { it.name == uttak.type.toUpperCase() }}
                uttak.stasjonID?.let { row[stasjonID] = it }
                uttak.samarbeidspartnerID?.let { row[partnerID] = it }
                uttak.beskrivelse?.let { row[beskrivelse] = it }
                uttak.gjentakelsesRegel?.let {
                    row[gjentakelsesRegelID] = uttak.gjentakelsesRegel.id // TODO:
                    GjentakelsesRegelTable.update({ GjentakelsesRegelTable.id eq uttak.gjentakelsesRegel.id }) {
                        gjenRow ->
                            uttak.gjentakelsesRegel.antall?.let { gjenRow[antall] = it }
                            uttak.gjentakelsesRegel.intervall?.let { gjenRow[intervall] = it }
                            uttak.gjentakelsesRegel.dager?.let { gjenRow[dager] = it.toString() }
                            uttak.gjentakelsesRegel.sluttTidspunkt?.let { gjenRow[sluttTidspunkt] = it }
                            gjenRow[endretTidspunkt] = LocalDateTime.now()
                    }
                }
                row[endretTidspunkt] = LocalDateTime.now() // TODO: legge endret tidspunkt i databasen istedetfor?
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { getUttakByID(uttak.id) },
            { RepositoryError.UpdateError(it.message).left() }
        )

    override fun deleteUttakById(id: Int): Either<RepositoryError, Unit> =
        getUttakByID(id)
            .fold(
                {
                    logger.error("Failed to get uttak by id before deleting it:${it.message}")
                    RepositoryError.DeleteError("Failed to get uttak by id before deleting it:${it.message}").left()
                },
                {
                    deleteUttak(it)
                }
            )

    override fun deleteUttak(uttak: Uttak): Either<RepositoryError, Unit> =
            runCatching {
                transaction {
                    UttakTable.update({ UttakTable.id eq uttak.id and UttakTable.slettetTidspunkt.isNull() }) { row ->
                        row[slettetTidspunkt] = LocalDateTime.now()
                    }
                    uttak.gjentakelsesRegel?.id?.let {
                        GjentakelsesRegelTable.deleteGjentakelsesRegel(it)
                    }
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
            (UttakTable innerJoin Stasjoner leftJoin Partnere leftJoin GjentakelsesRegelTable)
                .select { UttakTable.id eq uttakID and UttakTable.slettetTidspunkt.isNull() }
                .map { toUttak(it) }.firstOrNull()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID does not exist!") }) },
            { RepositoryError.SelectError(it.message).left() })

    override fun getUttakByUttaksDataID(uttaksdataID: Int): Either<RepositoryError, Uttak> =
        UttaksDataRepository.getUttakByUttaksDataID(uttaksdataID)

    override fun getUttak(uttakGetForm: UttakGetForm?): Either<RepositoryError, List<Uttak>> =
            runCatching {
                transaction {
                    val query =
                        (UttakTable leftJoin Stasjoner leftJoin Partnere leftJoin GjentakelsesRegelTable leftJoin UttaksdataTable).selectAll()

                    query.andWhere { UttakTable.slettetTidspunkt.isNull() }

                    if (uttakGetForm != null) {
                        uttakGetForm.id?.let { query.andWhere { UttakTable.id eq it } }
                        uttakGetForm.stasjonId?.let { query.andWhere { UttakTable.stasjonID eq it } }
                        uttakGetForm.partnerId?.let { query.andWhere { UttakTable.partnerID eq it } }
                        uttakGetForm.gjentakelsesRegelID?.let { query.andWhere { UttakTable.gjentakelsesRegelID eq it } }
                        uttakGetForm.startTidspunkt?.let { query.andWhere { UttakTable.startTidspunkt.greaterEq(it) } }
                        uttakGetForm.sluttTidspunkt?.let { query.andWhere { UttakTable.sluttTidspunkt.lessEq(it) } }
                    }
                    query.mapNotNull { toUttak(it) }
                }
            }
            .onFailure { logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError(it.message).left() }
            )

    override fun exists(id: Int) = transaction { UttakTable.select { UttakTable.id eq id and UttakTable.slettetTidspunkt.isNull() }.count() >= 1 }

    fun toUttak(row: ResultRow?): Uttak? {
        if (row == null) return null

        return Uttak(
            row[UttakTable.id].value,
            row[UttakTable.startTidspunkt],
            row[UttakTable.sluttTidspunkt],
            toStasjon(row),
            toPartner(row),
            getGetGjentakelsesRegelFromResultRow(row),
            row[UttakTable.type],
            row[UttakTable.beskrivelse],
            row[UttakTable.endretTidspunkt]
        )
    }


    private fun getGetGjentakelsesRegelFromResultRow(row: ResultRow): GjentakelsesRegel? {
        if (!row.hasValue(GjentakelsesRegelTable.id) || row.getOrNull(
                GjentakelsesRegelTable.id
            ) == null
        ) return null

        return GjentakelsesRegel(
            row[GjentakelsesRegelTable.id].value,
            row[GjentakelsesRegelTable.endretTidspunkt],
            row[GjentakelsesRegelTable.sluttTidspunkt],
            row[GjentakelsesRegelTable.slettetTidspunkt],
            row[GjentakelsesRegelTable.dager]?.toWeekDayList(),
            row[GjentakelsesRegelTable.intervall],
            row[GjentakelsesRegelTable.antall]
        )
    }

    /**
     * Used by teardown() when testing.
     * // TODO: This was unnecessary?
     */
    fun deleteAllUttakForTesting() = runCatching {
        val appConfig = HoconApplicationConfig(ConfigFactory.load())
        val debug = appConfig.property("ktor.oko.debug").getString().toBoolean()
        if (!debug) { throw Exception() }
        transaction {
            UttakTable.deleteAll()
            GjentakelsesRegelTable.deleteAll()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Unit.right() },
            { RepositoryError.DeleteError("Failed to delete all pickups").left() }
        )

}