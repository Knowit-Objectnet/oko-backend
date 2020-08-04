package ombruk.backend.pickup.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.database.toStation
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.database.toPartner
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.form.pickup.PickupGetForm
import ombruk.backend.pickup.form.pickup.PickupUpdateForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.shared.database.IRepository
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object Pickups : IntIdTable("pickups") {
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val stationID = integer("station_id").references(Stations.id)
    val description = text("description").nullable()
    val chosenPartnerId = integer("chosen_partner_id").references(Partners.id).nullable()
}

object PickupRepository : IPickupRepository, IRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.PickupRepository")

    fun savePickup(pickupPostForm: PickupPostForm): Either<RepositoryError, Pickup> = runCatching {
        transaction {
            Pickups.insertAndGetId {
                it[stationID] = pickupPostForm.stationId
                it[startTime] = pickupPostForm.startDateTime
                it[endTime] = pickupPostForm.endDateTime
                it[description] = pickupPostForm.description
                it[chosenPartnerId] = null
            }
        }.value
    }
        .onFailure { logger.error("Failed to save Pickup to DB: ${it.message}") }
        .fold({ getPickupById(it) },
            { RepositoryError.InsertError("SQL error").left() })


    fun getPickupById(id: Int): Either<RepositoryError, Pickup> = runCatching {
        transaction {
            (Pickups innerJoin Stations leftJoin Partners)
                .select { Pickups.id eq id }
                .map { toPickup(it) }
                .firstOrNull()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            {   // success
                Either.cond(it != null, { it!! },
                    { RepositoryError.NoRowsFound("ID does not exist!") })
            },
            // error:
            { RepositoryError.SelectError(it.message).left() })


    // Note that the start- and end-times only look at the startTime of the pickup.
    @KtorExperimentalLocationsAPI
    fun getPickups(pickupQueryForm: PickupGetForm): Either<ServiceError, List<Pickup>> =
        runCatching {
            transaction {
                val query = (Pickups innerJoin Stations leftJoin Partners).selectAll()
                pickupQueryForm.stationId?.let { query.andWhere { Pickups.stationID eq it } }
                pickupQueryForm.endDateTime?.let { query.andWhere { Pickups.startTime lessEq it } }
                pickupQueryForm.startDateTime?.let { query.andWhere { Pickups.startTime greaterEq it } }
                pickupQueryForm.partnerId?.let { query.andWhere { Pickups.chosenPartnerId eq it } }
                query.map { toPickup(it) }
            }
        }
            // Fold into either...
            .fold(
                // success
                { it.right() },
                // error
                { RepositoryError.SelectError("Database query failed").left() }
            )


    fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<RepositoryError, Pickup> =
        runCatching {
            transaction {
                Pickups.update({ Pickups.id eq pickupUpdateForm.id }) { row ->
                    pickupUpdateForm.startDateTime?.let { row[startTime] = it }
                    pickupUpdateForm.endDateTime?.let { row[endTime] = it }
                    pickupUpdateForm.description?.let { row[description] = it }
                    pickupUpdateForm.chosenPartnerId?.let { row[chosenPartnerId] = it }
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { getPickupById(pickupUpdateForm.id) },
                { RepositoryError.UpdateError("Failed to update pickup $pickupUpdateForm").left() }
            )


    fun deletePickup(id: Int) = runCatching {
        transaction {
            Pickups.deleteWhere { Pickups.id eq id }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { id.right() },
            { RepositoryError.DeleteError("Failed to delete pickup with ID $id").left() }
        )

    override fun exists(id: Int) = transaction {
        Pickups.select { Pickups.id eq id }.count() >= 1
    }
}


// helper to turn rows into pickups.
fun toPickup(row: ResultRow): Pickup {
    return Pickup(
        row[Pickups.id].value,
        row[Pickups.startTime],
        row[Pickups.endTime],
        row[Pickups.description],
        toStation(row),
        row[Pickups.chosenPartnerId]?.let { toPartner(row) }
    )
}
