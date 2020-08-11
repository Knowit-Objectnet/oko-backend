package ombruk.backend.calendar.database

import arrow.core.Either
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.shared.database.IRepository
import ombruk.backend.shared.error.RepositoryError

interface IEventRepository : IRepository {
    /**
     * Inserts one or several [Event] objects into the database.
     *
     * @param eventPostForm A [EventPostForm] describing the event(s) to be posted.
     * @return An [Either] object consisting of a [RepositoryError] on failure and the saved [Event] on success. The
     * returned [Event] is equal to the one stored in the database. If the posted [Event] is recurring, the first
     * occurence will be returned.
     */
    fun insertEvent(eventPostForm: EventPostForm): Either<RepositoryError, Event>

    /**
     * Updates a stored Event. The id passed in the [Event] must already exist in the database.
     *
     * @param event A [EventUpdateForm] object containing the information that should be updated. ID cannot be altered.
     * @return An [Either] object consisting of a [RepositoryError] on failure or an [Event] with the updated values on success.
     */
    fun updateEvent(event: EventUpdateForm): Either<RepositoryError, Event>

    /**
     * Deletes one or several [Event] objects from the database. The events to be deleted are filtered through the use
     * of non-null properties in the [EventDeleteForm].
     *
     * @param eventDeleteForm A [EventDeleteForm] containing the query constraints.
     * @return An [Either] object consisting of a [RepositoryError] on failure and [List] of deleted [Event] objects on success.
     */
    fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<RepositoryError, List<Event>>

    /**
     * Fetches a specific [Event].
     *
     * @param eventID The id of the [Event] that should be fetched.
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [Event] on success.
     */
    fun getEventByID(eventID: Int): Either<RepositoryError, Event>

    /**
     * Fetches a set of events that are specified by query parameters in a [EventGetForm]. If null, all events are returned.
     *
     * @return An [Either] object consisting of a [RepositoryError] on failure and a [List] of [Event] objects on success.
     */
    fun getEvents(eventGetForm: EventGetForm?): Either<RepositoryError, List<Event>>
}