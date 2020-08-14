package ombruk.backend.calendar.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.model.toWeekDayList
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.database.toPartner
import ombruk.backend.shared.error.RepositoryError
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object Events : IntIdTable("events") {
    val startDateTime = datetime("start_date_time")
    val endDateTime = datetime("end_date_time")
    val recurrenceRuleID =
        integer("recurrence_rule_id").references(RecurrenceRules.id, onDelete = ReferenceOption.CASCADE).nullable()
    val stationID = integer("station_id").references(Stations.id, onDelete = ReferenceOption.CASCADE)
    val partnerID = integer("partner_id").references(Partners.id, onDelete = ReferenceOption.CASCADE)
}

object EventRepository : IEventRepository {
    private val logger = LoggerFactory.getLogger("ombruk.backend.service.EventRepository")

    override fun insertEvent(eventPostForm: EventPostForm): Either<RepositoryError, Event> = runCatching {
        Events.insertAndGetId {
            it[startDateTime] = eventPostForm.startDateTime
            it[endDateTime] = eventPostForm.endDateTime
            it[recurrenceRuleID] = eventPostForm.recurrenceRule?.id
            it[stationID] = eventPostForm.stationId
            it[partnerID] = eventPostForm.partnerId
        }.value
    }
        .onFailure { logger.error("Failed to save event to DB; ${it.message}") }
        .fold(
            { getEventByID(it) },
            { RepositoryError.InsertError("SQL error").left() }
        )


    override fun updateEvent(event: EventUpdateForm): Either<RepositoryError, Event> = runCatching {
        transaction {
            Events.update({ Events.id eq event.id }) { row ->
                event.startDateTime?.let { row[startDateTime] = event.startDateTime }
                event.endDateTime?.let { row[endDateTime] = event.endDateTime }
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { getEventByID(event.id) },
            { RepositoryError.UpdateError(it.message).left() }
        )


    @KtorExperimentalLocationsAPI
    override fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<RepositoryError, List<Event>> =
        runCatching {
            transaction {
                /*
                This is a conditional delete, and is somewhat special. Essentially, what's being done is building separate
                operations for each value of the eventDeleteForm that's not null. These are then added to a list, which is
                then combined to a full statement that can be ran. Op.build is a bit finicky as to what it accepts
                as input, so you might have to use function (foo.lessEq(bar)) instead of DSL (foo eq bar) some places.
                 */
                val statements = mutableListOf<Op<Boolean>>()
                eventDeleteForm.eventId?.let { statements.add(Op.build { Events.id eq it }) }
                eventDeleteForm.recurrenceRuleId?.let { statements.add(Op.build { Events.recurrenceRuleID eq it }) }
                eventDeleteForm.fromDate?.let { statements.add(Op.build { Events.startDateTime.greaterEq(it) }) }
                eventDeleteForm.toDate?.let { statements.add(Op.build { Events.startDateTime.lessEq(it) }) }
                eventDeleteForm.partnerId?.let { statements.add(Op.build { Events.partnerID eq it }) }
                eventDeleteForm.stationId?.let { statements.add(Op.build { Events.stationID eq it }) }

                // Delete and return deleted events. Have to handle the case where no statements are sepcified
                if (statements.isEmpty()) {
                    val result = (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).selectAll()
                        .mapNotNull { toEvent(it) }
                    Events.deleteAll()
                    return@transaction result
                } else {
                    val statement = AndOp(statements)
                    val result =
                        (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).select { statement }
                            .mapNotNull { toEvent(it) }
                    Events.deleteWhere { statement }
                    return@transaction result
                }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it.isNotEmpty(), { it }, { RepositoryError.NoRowsFound("No matches found") }) },
                { RepositoryError.DeleteError(it.message).left() })


    override fun getEventByID(eventID: Int): Either<RepositoryError, Event> = runCatching {
        transaction {
            // leftJoin RecurrenceRules because not all events are recurring.
            (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).select { Events.id eq eventID }
                .map { toEvent(it) }.firstOrNull()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID does not exist!") }) },
            { RepositoryError.SelectError(it.message).left() })


    @KtorExperimentalLocationsAPI
    override fun getEvents(eventGetForm: EventGetForm?): Either<RepositoryError, List<Event>> =
        runCatching {
            transaction {
                val query = (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).selectAll()
                if (eventGetForm != null) {
                    eventGetForm.eventId?.let { query.andWhere { Events.id eq it } }
                    eventGetForm.stationId?.let { query.andWhere { Events.stationID eq it } }
                    eventGetForm.partnerId?.let { query.andWhere { Events.partnerID eq it } }
                    eventGetForm.recurrenceRuleId?.let { query.andWhere { Events.recurrenceRuleID eq it } }
                    eventGetForm.fromDate?.let { query.andWhere { Events.startDateTime.greaterEq(it) } }
                    eventGetForm.toDate?.let { query.andWhere { Events.endDateTime.lessEq(it) } }
                }
                query.mapNotNull { toEvent(it) }
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { it.right() },
                { RepositoryError.SelectError(it.message).left() }
            )

    override fun exists(id: Int) = transaction { Events.select { Events.id eq id }.count() >= 1 }


    private fun toEvent(row: ResultRow?): Event? {
        if (row == null) return null

        return Event(
            row[Events.id].value,
            row[Events.startDateTime],
            row[Events.endDateTime],
            toStation(row),
            toPartner(row),
            getGetRecurrenceRuleFromResultRow(row)
        )
    }


    private fun getGetRecurrenceRuleFromResultRow(row: ResultRow): RecurrenceRule? {
        if (!row.hasValue(RecurrenceRules.id) || row.getOrNull(
                RecurrenceRules.id
            ) == null
        ) return null

        return RecurrenceRule(
            row[RecurrenceRules.id].value,
            row[RecurrenceRules.until],
            row[RecurrenceRules.days]?.toWeekDayList(),
            row[RecurrenceRules.interval],
            row[RecurrenceRules.count]
        )
    }
}