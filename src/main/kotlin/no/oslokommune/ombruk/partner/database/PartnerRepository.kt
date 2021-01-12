package no.oslokommune.ombruk.partner.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.partner.form.PartnerGetForm

import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.UttakTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


object Partnere : IntIdTable("samarbeidspartnere") {
    val navn = varchar("navn", 128)
    val beskrivelse = text("beskrivelse")
    val telefon = varchar("telefon", 32)
    val epost = varchar("epost", 64)
    val endretTidspunkt = datetime("endret_tidspunkt")
    val slettetTidspunkt = datetime("slettet_tidspunkt").nullable()
}

object PartnerRepository : IPartnerRepository {
    private val logger = LoggerFactory.getLogger("ombruk.no.oslokommune.ombruk.partner.service.PartnerRepository")

    override fun insertPartner(partner: PartnerPostForm) = runCatching {
        Partnere.insertAndGetId {
            it[navn] = partner.navn
            it[beskrivelse] = partner.beskrivelse
            it[telefon] = partner.telefon
            it[epost] = partner.epost
            it[endretTidspunkt] = LocalDateTime.now()
        }
    }
        .fold(
            { getPartnerByID(it.value) },
            {
                logger.error("Failed to save partner to DB: ${it.message}")
                RepositoryError.InsertError("SQL error").left()
            }
        )

    override fun updatePartner(partner: PartnerUpdateForm) = runCatching {
        transaction {
            Partnere.update({ Partnere.id eq partner.id and Partnere.slettetTidspunkt.isNull() })
            { row ->
                partner.navn?.let { row[navn] = it }
                partner.beskrivelse?.let { row[beskrivelse] = it }
                partner.telefon?.let { row[telefon] = it }
                partner.epost?.let { row[epost] = it }
                row[endretTidspunkt] = LocalDateTime.now()
            }
        }
    }
        .fold(
            {
                if (it > 0)
                    getPartnerByID(partner.id)
                else
                    RepositoryError.NoRowsFound("${partner.id} not found").left()
            },
            {
                logger.error("Failed to save partner to DB: ${it.message}")
                RepositoryError.UpdateError(it.message).left()
            }
        )

    override fun deletePartner(partnerID: Int) = runCatching {
        transaction {
            Partnere.update({ Partnere.id eq partnerID }) { row ->
                row[slettetTidspunkt] = LocalDateTime.now()
                row[endretTidspunkt] = LocalDateTime.now()
            }
        }
    }
        .fold(
            {
                if (it > 0)
                    getPartnerByID(partnerID, true)
                else
                    RepositoryError.NoRowsFound("$partnerID not found").left()
            },
            {
                logger.error("Failed to delete partner in DB: ${it.message}")
                RepositoryError.DeleteError(it.message).left()
            }
        )

    fun deleteAllPartnere() = runCatching {
        transaction {
            Partnere.update({ Partnere.slettetTidspunkt.isNull() })
            { row ->
                row[slettetTidspunkt] = LocalDateTime.now()
            }
        }
    }
        .fold(
            {
                if (it > 0)
                    getPartnere(PartnerGetForm(), true)
                else
                    RepositoryError.NoRowsFound("Rows not found").left()
            },
            {
                logger.error("Failed to delete partnere in DB: ${it.message}")
                RepositoryError.DeleteError(it.message).left()
            })


    override fun getPartnerByID(partnerID: Int, showDeleted: Boolean): Either<RepositoryError, Partner> =
        runCatching {
            transaction {
                Partnere.select {
                    if (showDeleted)
                        Partnere.id eq partnerID
                    else
                        Partnere.id eq partnerID and Partnere.slettetTidspunkt.isNull()

                }.mapNotNull {
                    toPartner(it)
                }

            }
        }
            .fold(
                {
                    if (it.isNotEmpty())
                        it.first().right()
                    else
                        RepositoryError.NoRowsFound("$partnerID not found").left()
                },
                {
                    logger.error("${it.message}")
                    RepositoryError.DeleteError(it.message).left()
                }
            )

    @KtorExperimentalLocationsAPI
    override fun getPartnere(
        partnerGetForm: PartnerGetForm,
        showDeleted: Boolean
    ): Either<RepositoryError.SelectError, List<Partner>> =
        runCatching {
            transaction {
                val query = Partnere.selectAll()
                partnerGetForm.navn?.let {
                    query.andWhere { Partnere.navn.lowerCase().like("%${it.toLowerCase()}%") }
                }
                partnerGetForm.beskrivelse?.let {
                    query.andWhere { Partnere.beskrivelse.lowerCase().like("%${it.toLowerCase()}%") }
                }
                partnerGetForm.telefon?.let {
                    query.andWhere { Partnere.telefon.like("%${it}%") }
                }
                partnerGetForm.epost?.let {
                    query.andWhere { Partnere.epost.lowerCase().like("%${it.toLowerCase()}%") }
                }
                query.andWhere { Partnere.slettetTidspunkt.isNull() }
                query.mapNotNull { toPartner(it) }
            }
        }
            .fold(
                {
                    it.right()
                },
                {
                    logger.error("${it.message}")
                    RepositoryError.SelectError(it.message).left()
                }
            )

    override fun exists(id: Int) =
        transaction { Partnere.select { Partnere.id eq id and Partnere.slettetTidspunkt.isNull() }.count() >= 1 }

    override fun exists(name: String) = transaction { Partnere.select { Partnere.navn eq name }.count() >= 1 }

    /**
     * Used by teardown() when testing.
     */
    fun deleteAllPartnereForTesting() = runCatching {
        val appConfig = HoconApplicationConfig(ConfigFactory.load())
        val debug = appConfig.property("ktor.oko.debug").getString().toBoolean()
        if (!debug) {
            throw Exception()
        }
        transaction {
            Partnere.deleteAll()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Unit.right() },
            { RepositoryError.DeleteError("Failed to delete all partners").left() }
        )
}

fun toPartner(row: ResultRow): Partner? {
    if (!row.hasValue(Partnere.id) || row.getOrNull(Partnere.id) == null) {
        return null
    }

    return Partner(
        row[Partnere.id].value,
        row[Partnere.navn],
        row[Partnere.beskrivelse],
        row[Partnere.telefon],
        row[Partnere.epost],
        row[Partnere.endretTidspunkt]
    )
}
