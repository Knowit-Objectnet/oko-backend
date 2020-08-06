package ombruk.backend.pickup.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.database.toStation
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.database.toPartner
import ombruk.backend.partner.model.Partner
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.model.Request
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*
    Requests are associated to a specific pickup. Each request is a specific partner that wants to perform the pickup.
 */

object Requests : Table("requests") {
    val pickupID = integer("pickup_id").references(Pickups.id)
    val partnerID = integer("partner_id").references(Partners.id)
}

object RequestRepository : IRequestRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.RequestRepository")

    @KtorExperimentalLocationsAPI
    override fun getRequests(requestGetForm: RequestGetForm?): Either<RepositoryError, List<Request>> {
        // Partner is joined twice on different tables, therefore aliases have to be used.
        val winningPartner = Partners.alias("winningPartner")
        val requestPartner = Partners.alias("requestPartner")
        return runCatching {
            transaction {
                val query = (Requests innerJoin Pickups innerJoin Stations)
                    .leftJoin(winningPartner, { Pickups.chosenPartnerId }, { winningPartner[Partners.id] })
                    .innerJoin(requestPartner, { Requests.partnerID }, { requestPartner[Partners.id] })
                    .selectAll()
                requestGetForm?.let {   // add constraints if needed.
                    requestGetForm.pickupId?.let { query.andWhere { Requests.pickupID eq it } }
                    requestGetForm.partnerId?.let { query.andWhere { Requests.partnerID eq it } }
                }
                query.map { toRequest(it, winningPartner, requestPartner) }
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
     * @param row A [ResultRow] that contains two [Partners] represented by two [Alias].
     * @param winningPartner an [Alias] of a subsection of the [row]. Used to represent [Pickup.chosenPartner]. Can be null.
     * @param requestPartner An [Alias] of a subsection of the [row]. Used to represent the [Request.partner]. Is always set.
     * @return A [Request].
     */
    private fun toRequest(row: ResultRow, winningPartner: Alias<Partners>, requestPartner: Alias<Partners>): Request {
        return Request(
            Pickup(
                row[Pickups.id].value,
                row[Pickups.startTime],
                row[Pickups.endTime],
                row[Pickups.description],
                toStation(row),
                row[winningPartner[Partners.id]]?.let { toPartner(row, winningPartner) }
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
    private fun toPartner(row: ResultRow, alias: Alias<Partners>): Partner {
        return Partner(
            row[alias[Partners.id]].value,
            row[alias[Partners.name]],
            row[alias[Partners.description]],
            row[alias[Partners.email]],
            row[alias[Partners.phone]]
        )
    }
}


