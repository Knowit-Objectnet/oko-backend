package ombruk.backend.calendar.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.RecurrenceRules
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.shared.error.ServiceError
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

    override fun updateEvent(eventUpdate: EventUpdateForm) = transaction { EventRepository.updateEvent(eventUpdate)}

}