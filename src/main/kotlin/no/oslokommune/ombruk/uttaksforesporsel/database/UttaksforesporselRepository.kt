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
import no.oslokommune.ombruk.uttak.model.UttaksType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/*
    Uttaksforesporsels are associated to a specific no.oslokommune.ombruk.pickup. Each uttaksforesporsel is a specific partner that wants to perform the no.oslokommune.ombruk.pickup.
 */

object UttaksForesporselTable : Table("uttaksforesporsel") {
    val uttakID = integer("uttak_id").references(UttakTable.id)
    val partnerID = integer("partner_id").references(Partnere.id)
//    val status = UttakTable.enumerationByName("status", 64, UttaksType::class)
}

object UttaksforesporselRepository : IUttaksforesporselRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.UttaksforesporselRepository")

    @KtorExperimentalLocationsAPI
    override fun getForesporsler(requestGetForm: UttaksForesporselGetForm?): Either<RepositoryError, List<UttaksForesporsel>> {
        // Partner is joined twice on different tables (no.oslokommune.ombruk.pickup and on this uttaksforesporsel), therefore aliases have to be used.
        val foresporselPartner = Partnere.alias("foresporselPartner")
        val uttakPartner = Partnere.alias("uttakPartner")
        return runCatching {
            transaction {
                val query = (UttaksForesporselTable innerJoin UttakTable innerJoin Stasjoner)
                    .leftJoin(foresporselPartner, { UttakTable.partnerID }, { foresporselPartner[Partnere.id] })
                    .innerJoin(uttakPartner, { UttaksForesporselTable.partnerID }, { uttakPartner[Partnere.id] })
                    .selectAll()
                requestGetForm?.let {   // add constraints if needed.
                    requestGetForm.pickupId?.let { query.andWhere { UttaksForesporselTable.uttakID eq it } }
                    requestGetForm.partnerId?.let { query.andWhere { UttaksForesporselTable.partnerID eq it } }
                }
                query.map { toUttaksforesporsel(it, foresporselPartner, uttakPartner) }
            }
        }
            .onFailure { it.printStackTrace(); logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError("Failed to get requests: ${it.message}").left() }
            )
    }

    @KtorExperimentalLocationsAPI
    private fun getSingleForesporsel(pickupId: Int, partnerId: Int) =
        getForesporsler(UttaksForesporselGetForm(pickupId, partnerId))
            .fold(
                { it.left() },
                // If result is empty array, return 404.
                {
                    Either.cond(it.isNotEmpty(), { it.first() },
                        { RepositoryError.NoRowsFound("Failed to find uttaksforesporsel") })
                }
            )


    @KtorExperimentalLocationsAPI
    override fun saveForesporsel(requestPostForm: UttaksforesporselPostForm) =
        runCatching {
            transaction {
                UttaksForesporselTable.insert {
                    it[uttakID] = requestPostForm.uttaksId
                    it[partnerID] = requestPostForm.partnerId
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { getSingleForesporsel(requestPostForm.uttaksId, requestPostForm.partnerId) },
                { RepositoryError.InsertError("Failed to save uttaksforesporsel $requestPostForm").left() }
            )

    @KtorExperimentalLocationsAPI
    override fun deleteForesporsel(requestDeleteForm: UttaksforesporselDeleteForm) = runCatching {
        transaction {
            UttaksForesporselTable.deleteWhere {
                UttaksForesporselTable.uttakID eq requestDeleteForm.uttaksId and
                        (UttaksForesporselTable.partnerID eq requestDeleteForm.partnerId)
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { it.right() },
            { RepositoryError.DeleteError("Failed to delete uttaksforesporsel $requestDeleteForm").left() }
        )


    /**
     * Private function for creating a uttaksforesporsel from a [ResultRow]. This function expects that the two required [Partner]
     * objects are aliased and present in the [ResultRow].
     *
     * @param row A [ResultRow] that contains two [Partnere] represented by two [Alias].
     * @param chosenPartnerForPickup an [Alias] of a subsection of the [row]. Used to represent [Pickup.chosenPartner]. Can be null.
     * @param requestPartner An [Alias] of a subsection of the [row]. Used to represent the [UttaksForesporsel.partner]. Is always set.
     * @return A [UttaksForesporsel].
     */
    private fun toUttaksforesporsel(row: ResultRow, chosenPartnerForPickup: Alias<Partnere>, requestPartner: Alias<Partnere>): UttaksForesporsel {
        // "chosenPartnerForPickup" for the no.oslokommune.ombruk.pickup might be null
        var chosenPartner: Partner? = null
        if (row.getOrNull(chosenPartnerForPickup[Partnere.id]) != null) {
            chosenPartner = toPartner(row, chosenPartnerForPickup)
        }

        return UttaksForesporsel(
                UttakRepository.toUttak(row)!!,
            toPartner(row, requestPartner)
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


