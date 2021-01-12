package no.oslokommune.ombruk.uttaksforesporsel.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.partner.database.Partnere
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksForesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.UttaksForesporsel
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.UttakTable
import no.oslokommune.ombruk.uttak.model.Uttak
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/*
    Uttaksforesporsels are associated to a specific [Uttak]. Each uttaksforesporsel is a specific partner that wants to perform the [Uttak].
 */

object UttaksForesporselTable : Table("uttaksforesporsel") {
    val uttakID = integer("uttak_id").references(UttakTable.id)
    val partnerID = integer("partner_id").references(Partnere.id)
//    val status = UttakTable.enumerationByName("status", 64, UttaksType::class)
}

object UttaksforesporselRepository : IUttaksforesporselRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.UttaksforesporselRepository")

    @KtorExperimentalLocationsAPI
    override fun getForesporsler(foresporselGetForm: UttaksForesporselGetForm?): Either<RepositoryError, List<UttaksForesporsel>> {
        // Partner is joined twice on different tables ([Uttak] and on this uttaksforesporsel), therefore aliases have to be used.
        val foresporselPartner = Partnere.alias("foresporselPartner")
        val uttakPartner = Partnere.alias("uttakPartner")
        return runCatching {
            transaction {
                val query = (UttaksForesporselTable innerJoin UttakTable innerJoin Stasjoner)
                    .leftJoin(foresporselPartner, { UttakTable.partnerID }, { foresporselPartner[Partnere.id] })
                    .innerJoin(uttakPartner, { UttaksForesporselTable.partnerID }, { uttakPartner[Partnere.id] })
                    .selectAll()
                foresporselGetForm?.let {   // add constraints if needed.
                    foresporselGetForm.uttakId?.let { query.andWhere { UttaksForesporselTable.uttakID eq it } }
                    foresporselGetForm.partnerId?.let { query.andWhere { UttaksForesporselTable.partnerID eq it } }
                }
                query.map { toUttaksforesporsel(it, foresporselPartner, uttakPartner) }
            }
        }
            .onFailure { it.printStackTrace(); logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError("Failed to get foresporsler: ${it.message}").left() }
            )
    }

    @KtorExperimentalLocationsAPI
    private fun getSingleForesporsel(uttakId: Int, partnerId: Int) =
        getForesporsler(UttaksForesporselGetForm(uttakId, partnerId))
            .fold(
                { it.left() },
                // If result is empty array, return 404.
                {
                    Either.cond(it.isNotEmpty(), { it.first() },
                        { RepositoryError.NoRowsFound("Failed to find uttaksforesporsel") })
                }
            )


    @KtorExperimentalLocationsAPI
    override fun saveForesporsel(foresporselPostForm: UttaksforesporselPostForm) =
        runCatching {
            transaction {
                UttaksForesporselTable.insert {
                    it[uttakID] = foresporselPostForm.uttakId
                    it[partnerID] = foresporselPostForm.partnerId
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { getSingleForesporsel(foresporselPostForm.uttakId, foresporselPostForm.partnerId) },
                { RepositoryError.InsertError("Failed to save uttaksforesporsel $foresporselPostForm").left() }
            )

    @KtorExperimentalLocationsAPI
    override fun deleteForesporsel(foresporselDeleteForm: UttaksforesporselDeleteForm) = runCatching {
        transaction {
            UttaksForesporselTable.deleteWhere {
                UttaksForesporselTable.uttakID eq foresporselDeleteForm.uttaksId and
                        (UttaksForesporselTable.partnerID eq foresporselDeleteForm.partnerId)
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { it.right() },
            { RepositoryError.DeleteError("Failed to delete uttaksforesporsel $foresporselDeleteForm").left() }
        )


    /**
     * Private function for creating a uttaksforesporsel from a [ResultRow]. This function expects that the two required [Partner]
     * objects are aliased and present in the [ResultRow].
     *
     * @param row A [ResultRow] that contains two [Partnere] represented by two [Alias].
     * @param chosenPartnerForUttak an [Alias] of a subsection of the [row]. Used to represent [Uttak.partner]. Can be null.
     * @param foresporselPartner An [Alias] of a subsection of the [row]. Used to represent the [UttaksForesporsel.partner]. Is always set.
     * @return A [UttaksForesporsel].
     */
    private fun toUttaksforesporsel(
        row: ResultRow,
        chosenPartnerForUttak: Alias<Partnere>,
        foresporselPartner: Alias<Partnere>
    ): UttaksForesporsel {
        // TODO: I Don't think this actually works
        // "partner" for [Uttak] might be null
        var chosenPartner: Partner? = null
        if (row.getOrNull(chosenPartnerForUttak[Partnere.id]) != null) {
            chosenPartner = toPartner(row, chosenPartnerForUttak)
        }

        return UttaksForesporsel(
            UttakRepository.toUttak(row)!!,
            toPartner(row, foresporselPartner)
        )

    }

    /**
     * Helper function for creating a partner from an [Alias].
     *
     * @param row A [ResultRow]. Needed in order to place the [alias] in the correct context for its content to be accessed.
     * @param alias a subset of the [row] which contains an [Alias]ed [Partner].
     * @return A [Partner].
     */
    private fun toPartner(row: ResultRow, alias: Alias<Partnere>): Partner {
        return Partner(
            row[alias[Partnere.id]].value,
            row[alias[Partnere.navn]],
            row[alias[Partnere.beskrivelse]],
            row[alias[Partnere.telefon]],
            row[alias[Partnere.epost]]
        )
    }
}


