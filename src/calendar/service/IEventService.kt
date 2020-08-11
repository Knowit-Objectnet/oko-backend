package ombruk.backend.calendar.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.calendar.form.event.EventGetForm

interface IEventService {
    /**
     * Saves one or several events and automatically generates corresponding reports.
     * @param eventPostForm An [EventPostForm] that describes the events to be posted.
     * @return A [ServiceError] on failure and an [Event] on success. If saved event is recurring, the first event
     * is returned.
     */
    fun saveEvent(eventPostForm: EventPostForm): Either<ServiceError, Event>

    /**
     * Gets a specific event by it's [Event.id].
     * @param id The id of the [Event] to get. Must exist in db.
     * @return A [ServiceError] on failure and the corresponding [Event] on success.
     */
    fun getEventByID(id: Int): Either<ServiceError, Event>

    /**
     * Gets a list of events constrained by the values passed into the [eventGetForm].
     * @param eventGetForm The constraints to apply to the query. If all properties are null, all events will be queried.
     * @return A [ServiceError] on failure and a [List] of [Event] objects on success.
     */
    @KtorExperimentalLocationsAPI
    fun getEvents(
        eventGetForm: EventGetForm? = null
    ): Either<ServiceError, List<Event>>

    /**
     * Deletes one or more events specified by the values passed into the [eventDeleteForm].
     * @param eventDeleteForm The constraints to apply to the query. If all properties are null, all events will be deleted.
     * @return A [ServiceError] on failure and a [List] of the deleted [Event] objects on success.
     */
    @KtorExperimentalLocationsAPI
    fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<ServiceError, List<Event>>

    /**
     * Updates a singular event. Must be called several times to update all events belonging to a recurrence rule.
     * @param eventUpdate A [EventUpdateForm] containing the values to be updated. Only non-null values will be updated.
     * @return A [ServiceError] on failure and the updated [Event] on success.
     */
    fun updateEvent(eventUpdate: EventUpdateForm): Either<ServiceError, Event>
}
