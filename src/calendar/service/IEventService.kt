package ombruk.backend.calendar.service

import arrow.core.Either
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.shared.error.ServiceError
import ombruk.calendar.form.api.EventGetForm

interface IEventService {
    fun saveEvent(event: CreateEventForm): Either<ServiceError, Event>
    fun getEventByID(id: Int): Either<ServiceError, Event>
    fun getEvents(
        eventGetForm: EventGetForm? = null,
        eventType: EventType? = null
    ): Either<ServiceError, List<Event>>

    fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<ServiceError, List<Event>>

    fun updateEvent(eventUpdate: EventUpdateForm): Either<ServiceError, Event>
}
