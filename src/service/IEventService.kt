package ombruk.backend.service

import arrow.core.Either
import ombruk.backend.form.EventUpdateForm
import ombruk.backend.form.api.CreateEventForm
import ombruk.backend.form.api.EventDeleteForm
import ombruk.backend.model.Event
import ombruk.backend.model.EventType
import ombruk.calendar.form.api.EventGetForm

interface IEventService {
    fun saveEvent(event: CreateEventForm): Either<ServiceError, Event>
    fun getEventByID(id: Int): Either<ServiceError, Event>
    fun getEvents(
        eventGetForm: EventGetForm,
        eventType: EventType? = null
    ): Either<ServiceError, List<Event>>

    fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<ServiceError, List<Event>>

    fun updateEvent(eventUpdate: EventUpdateForm): Either<ServiceError, Event>
}
