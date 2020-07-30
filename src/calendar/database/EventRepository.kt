package ombruk.backend.calendar.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
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

    override fun insertEvent(postForm: EventPostForm): Either<RepositoryError, Event> {
        val id = runCatching {
            Events.insertAndGetId {
                it[startDateTime] = postForm.startDateTime
                it[endDateTime] = postForm.endDateTime
                it[recurrenceRuleID] = postForm.recurrenceRule?.id
                it[stationID] = postForm.stationId
                it[partnerID] = postForm.partnerId
            }.value
        }.getOrElse {
            logger.error("Failed to save event to DB: ${it.message}")
            return@insertEvent RepositoryError.InsertError("SQL error").left()
        }

        return getEventByID(id)
    }


    override fun updateEvent(event: EventUpdateForm): Either<RepositoryError, Event> = runCatching {
        transaction {
            Events.update({ Events.id eq event.id }) { row ->
                event.startDateTime?.let { row[startDateTime] = event.startDateTime }
                event.endDateTime?.let { row[endDateTime] = event.endDateTime }
            }
        }
    }
        .onFailure { logger.error(it.message) }
        .fold({ getEventByID(event.id) }, {
            RepositoryError.UpdateError(
                it.message
            ).left()
        })


    override fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<RepositoryError, List<Event>> =
        runCatching {
            transaction {
                var statement = eventDeleteForm.eventId?.let { Op.build { Events.id eq eventDeleteForm.eventId } }
                    ?: Op.build { Events.recurrenceRuleID eq eventDeleteForm.recurrenceRuleId }
                eventDeleteForm.fromDate?.let {
                    statement =
                        AndOp(
                            listOf(
                                statement,
                                Op.build { Events.startDateTime.greaterEq(eventDeleteForm.fromDate!!) })
                        )
                }
                eventDeleteForm.toDate?.let {
                    statement =
                        AndOp(listOf(statement, Op.build { Events.endDateTime.lessEq(eventDeleteForm.toDate!!) }))
                }

                val eventsToDelete =
                    (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).select { statement }
                        .mapNotNull { toEvent(it) }

                Events.deleteWhere { statement }
                return@transaction eventsToDelete
            }
        }
            .onFailure { logger.error(it.message) }
            .fold(
                { Either.cond(it.isNotEmpty(), { it }, { RepositoryError.NoRowsFound("No matches found") }) },
                { RepositoryError.DeleteError(it.message).left() })


    override fun getEventByID(eventID: Int): Either<RepositoryError, Event> = runCatching {
        transaction {
            (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).select { Events.id eq eventID }
                .map { toEvent(it) }.firstOrNull()
        }
    }
        .onFailure { logger.error(it.message) }
        .fold(
            { Either.cond(it != null, { it!! }, { RepositoryError.NoRowsFound("ID does not exist!") }) },
            { RepositoryError.SelectError(it.message).left() })


    override fun getEvents(eventGetForm: EventGetForm?, eventType: EventType?): Either<RepositoryError, List<Event>> =
        runCatching {
            transaction {
                val query = (Events innerJoin Stations innerJoin Partners leftJoin RecurrenceRules).selectAll()
                eventType?.let {
                    when (eventType) {
                        EventType.SINGLE -> query.andWhere { Events.recurrenceRuleID.isNull() }
                        EventType.RECURRING -> query.andWhere { Events.recurrenceRuleID.isNotNull() }
                    }
                }
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
            .fold({ it.right() }, { RepositoryError.SelectError(it.message).left() })

    override fun exists(id: Int) = transaction { Events.select{Events.id eq id}.count() >= 1 }


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