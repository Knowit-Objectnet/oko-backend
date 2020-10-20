package no.oslokommune.ombruk.uttaksforesporsel.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.partner.database.Samarbeidspartnere
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselDeleteForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselGetForm
import no.oslokommune.ombruk.uttaksforesporsel.form.uttaksforesporsel.UttaksforesporselPostForm
import no.oslokommune.ombruk.uttaksforesporsel.model.Uttaksforesporsel
import no.oslokommune.ombruk.shared.error.RepositoryError
import no.oslokommune.ombruk.uttak.database.UttakRepository
import no.oslokommune.ombruk.uttak.database.UttakTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/*
    Uttaksforesporsels are associated to a specific no.oslokommune.ombruk.pickup. Each uttaksforesporsel is a specific partner that wants to perform the no.oslokommune.ombruk.pickup.
 */

object Uttaksforesporsels : Table("uttaksforesporsel") {
    val uttaksID = integer("uttak_id").references(UttakTable.id)
    val partnerID = integer("partner_id").references(Samarbeidspartnere.id)
}

object UttaksforesporselRepository : IUttaksforesporselRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.UttaksforesporselRepository")

    @KtorExperimentalLocationsAPI
    override fun getRequests(requestGetForm: UttaksforesporselGetForm?): Either<RepositoryError, List<Uttaksforesporsel>> {
        // Partner is joined twice on different tables (no.oslokommune.ombruk.pickup and on this uttaksforesporsel), therefore aliases have to be used.
        val chosenPartnerForPickup = Samarbeidspartnere.alias("chosenPartnerForPickup")
        val requestPartner = Samarbeidspartnere.alias("requestPartner")
        return runCatching {
            transaction {
                val query = (Uttaksforesporsels innerJoin UttakTable innerJoin Stasjoner)
                    .leftJoin(chosenPartnerForPickup, { UttakTable.partnerID }, { chosenPartnerForPickup[Samarbeidspartnere.id] })
                    .innerJoin(requestPartner, { Uttaksforesporsels.partnerID }, { requestPartner[Samarbeidspartnere.id] })
                    .selectAll()
                requestGetForm?.let {   // add constraints if needed.
                    requestGetForm.pickupId?.let { query.andWhere { Uttaksforesporsels.uttaksID eq it } }
                    requestGetForm.partnerId?.let { query.andWhere { Uttaksforesporsels.partnerID eq it } }
                }
                query.map { toUttaksforesporsel(it, chosenPartnerForPickup, requestPartner) }
            }
        }
            .onFailure { it.printStackTrace(); logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError("Failed to get requests: ${it.message}").left() }
            )
    }

    @KtorExperimentalLocationsAPI
    private fun getSingleRequest(pickupId: Int, partnerId: Int) =
        getRequests(UttaksforesporselGetForm(pickupId, partnerId))
            .fold(
                { it.left() },
                // If result is empty array, return 404.
                {
                    Either.cond(it.isNotEmpty(), { it.first() },
                        { RepositoryError.NoRowsFound("Failed to find uttaksforesporsel") })
                }
            )


    @KtorExperimentalLocationsAPI
    override fun saveRequest(requestPostForm: UttaksforesporselPostForm) =
        runCatching {
            transaction {
                Uttaksforesporsels.insert {
                    it[uttaksID] = requestPostForm.uttaksId
                    it[partnerID] = requestPostForm.partnerId
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { getSingleRequest(requestPostForm.uttaksId, requestPostForm.partnerId) },
                { RepositoryError.InsertError("Failed to save uttaksforesporsel $requestPostForm").left() }
            )

    @KtorExperimentalLocationsAPI
    override fun deleteRequest(requestDeleteForm: UttaksforesporselDeleteForm) = runCatching {
        transaction {
            Uttaksforesporsels.deleteWhere {
                Uttaksforesporsels.uttaksID eq requestDeleteForm.uttaksId and
                        (Uttaksforesporsels.partnerID eq requestDeleteForm.partnerId)
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
     * @param row A [ResultRow] that contains two [Samarbeidspartnere] represented by two [Alias].
     * @param chosenPartnerForPickup an [Alias] of a subsection of the [row]. Used to represent [Pickup.chosenPartner]. Can be null.
     * @param requestPartner An [Alias] of a subsection of the [row]. Used to represent the [Uttaksforesporsel.partner]. Is always set.
     * @return A [Uttaksforesporsel].
     */
    private fun toUttaksforesporsel(row: ResultRow, chosenPartnerForPickup: Alias<Samarbeidspartnere>, requestPartner: Alias<Samarbeidspartnere>): Uttaksforesporsel {
        // "chosenPartnerForPickup" for the no.oslokommune.ombruk.pickup might be null
        var chosenPartner: Partner? = null
        if (row.getOrNull(chosenPartnerForPickup[Samarbeidspartnere.id]) != null) {
            chosenPartner = toPartner(row, chosenPartnerForPickup)
        }

        return Uttaksforesporsel(
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
    private fun toPartner(row: ResultRow, alias: Alias<Samarbeidspartnere>): Partner {
        return Partner(
            row[alias[Samarbeidspartnere.id]].value,
            row[alias[Samarbeidspartnere.navn]],
            row[alias[Samarbeidspartnere.beskrivelse]],
            row[alias[Samarbeidspartnere.telefon]],
            row[alias[Samarbeidspartnere.epost]]
        )
    }
}


