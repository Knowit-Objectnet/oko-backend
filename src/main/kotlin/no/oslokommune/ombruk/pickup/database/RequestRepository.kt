package no.oslokommune.ombruk.pickup.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.partner.database.Partnere
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.pickup.form.request.RequestDeleteForm
import no.oslokommune.ombruk.pickup.form.request.RequestGetForm
import no.oslokommune.ombruk.pickup.form.request.RequestPostForm
import no.oslokommune.ombruk.pickup.model.Pickup
import no.oslokommune.ombruk.pickup.model.Request
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/*
    Requests are associated to a specific no.oslokommune.ombruk.pickup. Each request is a specific partner that wants to perform the no.oslokommune.ombruk.pickup.
 */

object Requests : Table("requests") {
    val pickupID = integer("pickup_id").references(Pickups.id)
    val partnerID = integer("partner_id").references(Partnere.id)
}

object RequestRepository : IRequestRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.RequestRepository")

    @KtorExperimentalLocationsAPI
    override fun getRequests(requestGetForm: RequestGetForm?): Either<RepositoryError, List<Request>> {
        // Partner is joined twice on different tables (no.oslokommune.ombruk.pickup and on this request), therefore aliases have to be used.
        val chosenPartnerForPickup = Partnere.alias("chosenPartnerForPickup")
        val requestPartner = Partnere.alias("requestPartner")
        return runCatching {
            transaction {
                val query = (Requests innerJoin Pickups innerJoin Stasjoner)
                    .leftJoin(chosenPartnerForPickup, { Pickups.chosenPartnerId }, { chosenPartnerForPickup[Partnere.id] })
                    .innerJoin(requestPartner, { Requests.partnerID }, { requestPartner[Partnere.id] })
                    .selectAll()
                requestGetForm?.let {   // add constraints if needed.
                    requestGetForm.pickupId?.let { query.andWhere { Requests.pickupID eq it } }
                    requestGetForm.partnerId?.let { query.andWhere { Requests.partnerID eq it } }
                }
                query.map { toRequest(it, chosenPartnerForPickup, requestPartner) }
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
        getRequests(RequestGetForm(pickupId, partnerId))
            .fold(
                { it.left() },
                // If result is empty array, return 404.
                {
                    Either.cond(it.isNotEmpty(), { it.first() },
                        { RepositoryError.NoRowsFound("Failed to find request") })
                }
            )


    @KtorExperimentalLocationsAPI
    override fun saveRequest(requestPostForm: RequestPostForm) =
        runCatching {
            transaction {
                Requests.insert {
                    it[pickupID] = requestPostForm.pickupId
                    it[partnerID] = requestPostForm.partnerId
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { getSingleRequest(requestPostForm.pickupId, requestPostForm.partnerId) },
                { RepositoryError.InsertError("Failed to save request $requestPostForm").left() }
            )

    @KtorExperimentalLocationsAPI
    override fun deleteRequest(requestDeleteForm: RequestDeleteForm) = runCatching {
        transaction {
            Requests.deleteWhere {
                Requests.pickupID eq requestDeleteForm.pickupId and
                        (Requests.partnerID eq requestDeleteForm.partnerId)
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { it.right() },
            { RepositoryError.DeleteError("Failed to delete request $requestDeleteForm").left() }
        )


    /**
     * Private function for creating a request from a [ResultRow]. This function expects that the two required [Partner]
     * objects are aliased and present in the [ResultRow].
     *
     * @param row A [ResultRow] that contains two [Partnere] represented by two [Alias].
     * @param chosenPartnerForPickup an [Alias] of a subsection of the [row]. Used to represent [Pickup.chosenPartner]. Can be null.
     * @param requestPartner An [Alias] of a subsection of the [row]. Used to represent the [Request.partner]. Is always set.
     * @return A [Request].
     */
    private fun toRequest(row: ResultRow, chosenPartnerForPickup: Alias<Partnere>, requestPartner: Alias<Partnere>): Request {
        // "chosenPartnerForPickup" for the no.oslokommune.ombruk.pickup might be null
        var chosenPartner: Partner? = null
        if (row.getOrNull(chosenPartnerForPickup[Partnere.id]) != null) {
            chosenPartner = toPartner(row, chosenPartnerForPickup)
        }

        return Request(
            Pickup(
                row[Pickups.id].value,
                row[Pickups.startTime],
                row[Pickups.endTime],
                row[Pickups.description],
                toStasjon(row),
                chosenPartner
            ),
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
            row[alias[Partnere.name]],
            row[alias[Partnere.description]],
            row[alias[Partnere.phone]],
            row[alias[Partnere.email]]
        )
    }
}


