package ombruk.backend.calendar.service

import arrow.core.Either
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.shared.error.ServiceError

interface IEventService {
    fun saveEvent(eventPostForm: EventPostForm): Either<ServiceError, Event>
    fun getEventByID(id: Int): Either<ServiceError, Event>
    fun getEvents(
        eventGetForm: EventGetForm? = null,
        eventType: EventType? = null
    ): Either<ServiceError, List<Event>>

    fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<ServiceError, List<Event>>

    fun updateEvent(eventUpdate: EventUpdateForm): Either<ServiceError, Event>
}
