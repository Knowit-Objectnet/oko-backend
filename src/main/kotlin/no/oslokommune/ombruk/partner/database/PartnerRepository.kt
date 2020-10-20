package no.oslokommune.ombruk.partner.database

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.partner.form.PartnerGetForm

import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.form.PartnerUpdateForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime


object Samarbeidspartnere : IntIdTable("samarbeidspartnere") {
    val navn =              varchar("navn", 128)
    val beskrivelse =       text("beskrivelse")
    val telefon =           varchar("telefon", 32)
    val epost =             varchar("epost", 64)
    val endretTidspunkt =   datetime("endret_tidspunkt")
    val slettetTidspunkt =  datetime("slettet_tidspunkt").nullable()
}

object PartnerRepository : IPartnerRepository {
    private val logger = LoggerFactory.getLogger("ombruk.no.oslokommune.ombruk.partner.service.PartnerRepository")

    override fun insertPartner(partner: PartnerPostForm) = runCatching {
        Samarbeidspartnere.insertAndGetId {
            it[navn] = partner.navn
            it[beskrivelse] = partner.beskrivelse
            it[telefon] = partner.telefon
            it[epost] = partner.epost
            it[endretTidspunkt] = LocalDateTime.now()
        }
    }
        .onFailure { logger.error("Failed to save partner to DB: ${it.message}") }
        .fold(
            { Partner(it.value, partner.navn, partner.beskrivelse, partner.telefon, partner.epost).right() },
            { RepositoryError.InsertError("SQL error").left() }
        )


    override fun updatePartner(partner: PartnerUpdateForm) = runCatching {
        transaction {
            Samarbeidspartnere.update({ Samarbeidspartnere.id eq partner.id and Samarbeidspartnere.slettetTidspunkt.isNull() })
            { row ->
                partner.navn?.let { row[navn] = it }
                partner.beskrivelse?.let { row[beskrivelse] = it }
                partner.telefon?.let { row[telefon] = it }
                partner.epost?.let { row[epost] = it }
                row[endretTidspunkt] = LocalDateTime.now()
            }
        }
    }
        .onFailure { logger.error("Failed to update partner to DB: ${it.message}") }
        .fold(
            //Return right if more than 1 partner has been updated. Else, return an Error
            {
                Either.cond(it > 0,
                    { getPartnerByID(partner.id) },
                    { RepositoryError.NoRowsFound("${partner.id} not found") })
            },
            { RepositoryError.UpdateError(it.message).left() })
        .flatMap { it }


    override fun deletePartner(partnerID: Int) = runCatching {
        transaction {
            Samarbeidspartnere.update({ Samarbeidspartnere.id eq partnerID }) { row ->
                row[slettetTidspunkt] = LocalDateTime.now()
                row[endretTidspunkt] = LocalDateTime.now()
            }
        }
    }
            .onFailure { logger.error("Failed to delete partner in DB: ${it.message}") }
            .fold(
                {
                    Either.cond(it > 0,
                        { Unit },
                        { RepositoryError.NoRowsFound("$partnerID not found") })
                },
                { RepositoryError.DeleteError(it.message).left() })

    fun deleteAllPartnere() = runCatching {
            transaction { Samarbeidspartnere.update({ Samarbeidspartnere.slettetTidspunkt.isNull() })
                { row ->
                    row[slettetTidspunkt] = LocalDateTime.now()
                }
            }
        }
        .onFailure { logger.error("Failed to delete partnere in DB: ${it.message}") }
        .fold(
            {
                Either.cond(it > 0,
                    { Unit },
                    { RepositoryError.NoRowsFound("not found") })
            },
            { RepositoryError.DeleteError(it.message).left() })


    override fun getPartnerByID(partnerID: Int): Either<RepositoryError.NoRowsFound, Partner> =
        runCatching {
            transaction {
                Samarbeidspartnere.select {
                    Samarbeidspartnere.id eq partnerID and Samarbeidspartnere.slettetTidspunkt.isNull()
                }.mapNotNull {
                    toPartner(it)
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                {
                    Either.cond(it.isNotEmpty(),
                        { it.first() },
                        { RepositoryError.NoRowsFound("$partnerID not found") }
                    )
                },
                { RepositoryError.NoRowsFound(it.message).left() })


    @KtorExperimentalLocationsAPI
    override fun getPartnere(partnerGetForm: PartnerGetForm): Either<RepositoryError.SelectError, List<Partner>> =
        runCatching {
            transaction {
                val query = Samarbeidspartnere.selectAll()
                partnerGetForm.name?.let {
                    query.andWhere {
                        Samarbeidspartnere.navn eq it and Samarbeidspartnere.slettetTidspunkt.isNull()
                    }
                }
                query.mapNotNull { toPartner(it) }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError(it.message).left() }
            )

    override fun exists(id: Int) = transaction { Samarbeidspartnere.select { Samarbeidspartnere.id eq id }.count() >= 1 }
    override fun exists(name: String) = transaction { Samarbeidspartnere.select { Samarbeidspartnere.navn eq name }.count() >= 1 }

}


fun toPartner(row: ResultRow): Partner? {
    if (!row.hasValue(Samarbeidspartnere.id) || row.getOrNull(Samarbeidspartnere.id) == null) {
        return null
    }

    return Partner(
        row[Samarbeidspartnere.id].value,
        row[Samarbeidspartnere.navn],
        row[Samarbeidspartnere.beskrivelse],
        row[Samarbeidspartnere.telefon],
        row[Samarbeidspartnere.epost]
    )
}
