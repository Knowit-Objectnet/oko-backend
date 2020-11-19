package no.oslokommune.ombruk.uttak.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.oslokommune.ombruk.uttak.model.GjentakelsesRegel
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * This repository is only used when inserting recurring uttak into the db. Recurring uttak cannot be updated,
 * and fetching them from the database in done from the [UttakRepository]. Entries in the table are automatically
 * deleted when the corresponding uttak are deleted through cascading delete. Thus, only insertion is needed.
 */
object GjentakelsesRegelTable : IntIdTable("gjentakelsesregler") {
    val dager = varchar("dager", 50)
    val antall = integer("antall").nullable()
    val sluttTidspunkt = datetime("slutt_tidspunkt")
    val endretTidspunkt = datetime("endret_tidspunkt")
    val slettetTidspunkt = datetime("slettet_tidspunkt").nullable()
    val intervall = integer("intervall")

    private val logger = LoggerFactory.getLogger("ombruk.backend.service.PartnerRepository")

    fun insertGjentakelsesRegel(gjentakelsesRegel: GjentakelsesRegel): Either<RepositoryError, GjentakelsesRegel> = runCatching {
        GjentakelsesRegelTable.insertAndGetId {
            it[dager] = gjentakelsesRegel.dager.joinToString()
            it[antall] = gjentakelsesRegel.antall
            it[sluttTidspunkt] = gjentakelsesRegel.sluttTidspunkt!!
            it[intervall] = gjentakelsesRegel.intervall
            it[endretTidspunkt] = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            {
                gjentakelsesRegel.id = it.value
                gjentakelsesRegel.right()
            },
            { RepositoryError.InsertError(it.message).left() })

    /*
    fun updateSluttTidspunkt(id: Int, newSluttTidspunkt: LocalDateTime): Either<RepositoryError, Unit> = runCatching {
       GjentakelsesRegelTable.update({ GjentakelsesRegelTable.id eq id and slettetTidspunkt.isNotNull()}) {
            row ->
            row[sluttTidspunkt] = newSluttTidspunkt
       }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Unit.right() },
            { RepositoryError.InsertError(it.message).left() })
     */

    fun deleteGjentakelsesRegel(id: Int): Either<RepositoryError, Unit> =
            runCatching {
                transaction {
                    GjentakelsesRegelTable.update({
                        GjentakelsesRegelTable.id eq id and slettetTidspunkt.isNotNull()
                    }) { row ->
                        row[slettetTidspunkt] = LocalDateTime.now()
                    }
                }
            }
            .onFailure { logger.error("Failed to mark Uttak as deleted:${it.message}") }
            .fold(
                    { Unit.right() },
                    { RepositoryError.DeleteError("Failed to delete uttak.").left()}
            )
}