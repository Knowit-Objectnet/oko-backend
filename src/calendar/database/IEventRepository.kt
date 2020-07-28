package ombruk.backend.calendar.database

import arrow.core.Either
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.shared.error.RepositoryError
import ombruk.calendar.form.api.EventGetForm

interface IEventRepository {
    /**
     * Inserts an [Event] into the database. The ID passed in the [Event] will be overriden, and a serial
     * ID will be used instead.
     *
     * @param event An [Event]
     * @return An [Either] object consisting of a [RepositoryError] on failure and the saved [Event] on success.
     * The recurrence rule will be returned instead of an individual ID if the event is recurring.
     */
    fun insertEvent(from: CreateEventForm): Either<RepositoryError, Event>

    /**
     * Updates a stored Event. The id passed in the [Event] must already exist in the database.
     *
     * @param event An [Event] object containing the information that should be updated. ID cannot be altered.
     * @return An [Either] object consisting of a [RepositoryError] on failure an [Event] with the updated values on success.
     */
    fun updateEvent(event: EventUpdateForm): Either<RepositoryError, Event>

    /**
     * Deletes a Event from the database with the specified EventID.
     *
     * @param eventID The ID of the Event to be deleted.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [List] of deleted [Event] on success.
     */
    fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<RepositoryError, List<Event>>

    /**
     * Fetches a specific Event.
     *
     * @param eventID The Event that should be fetched.
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [Event] on success.
     */
    fun getEventByID(eventID: Int): Either<RepositoryError, Event>

    /**
     * Fetches all events.
     *
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [List] of [Event] objects on success.
     */
    fun getEvents(
        eventGetForm: EventGetForm?,
        eventType: EventType?
    ): Either<RepositoryError, List<Event>>
}