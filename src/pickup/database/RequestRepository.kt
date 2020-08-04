package ombruk.backend.pickup.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.database.Stations
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.database.toPartner
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/*
    A request is just a pickup that has been assigned a partner.
 */

object Requests : Table("requests") {
    val pickupID = integer("pickup_id").references(Pickups.id)
    val partnerID = integer("partner_id").references(Partners.id)
}

object RequestRepository : IRequestRepository {

    private val logger = LoggerFactory.getLogger("ombruk.backend.service.RequestRepository")

    @KtorExperimentalLocationsAPI
    override fun getRequests(requestGetForm: RequestGetForm?) =
        runCatching {
            transaction {
                val query = (Requests innerJoin Partners)
                    .innerJoin(Pickups, { Requests.pickupID }, { Pickups.id })
                    .innerJoin(Stations)
                    .selectAll()
                if (requestGetForm != null) {
                    requestGetForm.pickupId?.let { query.andWhere { Requests.pickupID eq it } }
                    requestGetForm.partnerId?.let { query.andWhere { Requests.partnerID eq it } }
                }
                query.map { toRequest(it) }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError("Failed to get requests: ${it.message}").left() }
            )

    @KtorExperimentalLocationsAPI
    private fun getSingleRequest(pickupId: Int, partnerId: Int) =
        getRequests(RequestGetForm(pickupId, partnerId))
            .fold(
                { it.left() },
                { Either.cond(it.isNotEmpty(), { it.first() },
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
}

fun toRequest(row: ResultRow): Request {
    return Request(
        toPickup(row),
        toPartner(row)
    )
}

