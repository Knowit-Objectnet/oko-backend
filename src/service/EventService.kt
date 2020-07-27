package ombruk.backend.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.database.EventRepository
import ombruk.backend.database.RecurrenceRules
import ombruk.backend.form.EventUpdateForm
import ombruk.backend.form.api.CreateEventForm
import ombruk.backend.form.api.EventDeleteForm
import ombruk.backend.model.Event
import ombruk.backend.model.EventType
import ombruk.calendar.form.api.EventGetForm
import org.jetbrains.exposed.sql.transactions.transaction

class EventService : IEventService {

    private val json = Json(JsonConfiguration.Stable)

    override fun saveEvent(form: CreateEventForm): Either<ServiceError, Event> = transaction {
        form.recurrenceRule?.let { recurrenceRule ->
            RecurrenceRules.insertRecurrenceRule(recurrenceRule)
                .flatMap {
                    form.map { newEvent ->
                        EventRepository.insertEvent(newEvent)
                    }.first()
                }
                .fold({ it.left() }, { it.right() })
        } ?: run {
            EventRepository.insertEvent(form)
        }
    }

    override fun getEventByID(id: Int): Either<ServiceError, Event> = transaction {
        EventRepository.getEventByID(id)
    }
    override fun getEvents(eventGetForm: EventGetForm, eventType: EventType?) = transaction {
        EventRepository.getEvents(eventGetForm, eventType)
    }

    override fun deleteEvent(eventDeleteForm: EventDeleteForm) = transaction {
        transaction { EventRepository.deleteEvent(eventDeleteForm) }}

    override fun updateEvent(eventUpdate: EventUpdateForm) = transaction {EventRepository.updateEvent(eventUpdate)}

}