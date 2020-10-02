package no.oslokommune.ombruk.pickup.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import no.oslokommune.ombruk.stasjon.database.Stasjoner
import no.oslokommune.ombruk.stasjon.database.toStasjon
import no.oslokommune.ombruk.partner.database.Partners
import no.oslokommune.ombruk.partner.database.toPartner
import no.oslokommune.ombruk.pickup.form.pickup.PickupGetForm
import no.oslokommune.ombruk.pickup.form.pickup.PickupPostForm
import no.oslokommune.ombruk.pickup.form.pickup.PickupUpdateForm
import no.oslokommune.ombruk.pickup.model.Pickup
import no.oslokommune.ombruk.shared.database.IRepository
import no.oslokommune.ombruk.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object Pickups : IntIdTable("pickups") {
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val stasjonID = integer("stasjon_id").references(Stasjoner.id)
    val description = text("description").nullable()
    val chosenPartnerId = integer("chosen_partner_id").references(Partners.id).nullable()
}

object PickupRepository : IPickupRepository, IRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.PickupRepository")

    override fun savePickup(pickupPostForm: PickupPostForm): Either<RepositoryError, Pickup> = runCatching {
        transaction {
            Pickups.insertAndGetId {
                it[stasjonID] = pickupPostForm.stasjonId
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


    override fun getPickupById(id: Int): Either<RepositoryError, Pickup> = runCatching {
        transaction {
            (Pickups innerJoin Stasjoner leftJoin Partners)
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


    // Note that the start- and end-times only look at the startTime of the no.oslokommune.ombruk.pickup.
    @KtorExperimentalLocationsAPI
    override fun getPickups(pickupGetForm: PickupGetForm?) =
        runCatching {
            transaction {
                val query = (Pickups innerJoin Stasjoner leftJoin Partners).selectAll()
                pickupGetForm?.let {
                    pickupGetForm.stasjonId?.let { query.andWhere { Pickups.stasjonID eq it } }
                    pickupGetForm.endDateTime?.let { query.andWhere { Pickups.startTime lessEq it } }
                    pickupGetForm.startDateTime?.let { query.andWhere { Pickups.startTime greaterEq it } }
                    pickupGetForm.partnerId?.let { query.andWhere { Pickups.chosenPartnerId eq it } }
                }
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


    override fun updatePickup(pickupUpdateForm: PickupUpdateForm): Either<RepositoryError, Pickup> =
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
                { RepositoryError.UpdateError("Failed to update no.oslokommune.ombruk.pickup $pickupUpdateForm").left() }
            )


    override fun deletePickup(id: Int) = runCatching {
        transaction {
            Pickups.deleteWhere { Pickups.id eq id }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { id.right() },
            { RepositoryError.DeleteError("Failed to delete no.oslokommune.ombruk.pickup with ID $id").left() }
        )

    fun deleteAllPickups() = runCatching {
        transaction {
            Pickups.deleteAll()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Unit.right() },
            { RepositoryError.DeleteError("Failed to delete all pickups").left() }
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
        toStasjon(row),
        row[Pickups.chosenPartnerId]?.let { toPartner(row) }
    )
}
