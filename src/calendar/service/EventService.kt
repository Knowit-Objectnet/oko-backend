package ombruk.backend.calendar.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.ktor.locations.KtorExperimentalLocationsAPI
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.RecurrenceRules
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.error.ServiceError
import org.jetbrains.exposed.sql.transactions.transaction

object EventService : IEventService {

    /**
     * Helper function for [saveEvent]. Takes an iterable [eventPostForm] and posts every [Event] to the db.
     * This function is used for saving both singular and recurring events, leading to a slightly confusing name. With that
     * being said, both types of events are iterable, so the map on the [eventPostForm] will only run once on singular events.
     *
     * @param eventPostForm An [EventPostForm] containing info on the [Event](s) to be stored.
     * @return a [ServiceError] on failure and the first stored [Event] on success.
     */
    private fun saveRecurring(eventPostForm: EventPostForm) = transaction {
        eventPostForm.map { form ->
            EventRepository.insertEvent(form)
                .flatMap { event ->
                    // Automatically generate a report whenever an event is created.
                    ReportService.saveReport(event).fold({ rollback(); it.left() }, { event.right() })
                }
        }.first()
    }

    override fun saveEvent(eventPostForm: EventPostForm): Either<ServiceError, Event> = transaction {
        let {
            eventPostForm.recurrenceRule?.let { RecurrenceRules.insertRecurrenceRule(it) } ?: Unit.right()
        }  // save recurrence rule, if set
            .flatMap { saveRecurring(eventPostForm) }
            .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun getEventByID(id: Int): Either<ServiceError, Event> = transaction {
        EventRepository.getEventByID(id)
    }

    @KtorExperimentalLocationsAPI
    override fun getEvents(eventGetForm: EventGetForm?): Either<ServiceError, List<Event>> = transaction {
        EventRepository.getEvents(eventGetForm)
    }

    @KtorExperimentalLocationsAPI
    override fun deleteEvent(eventDeleteForm: EventDeleteForm): Either<ServiceError, List<Event>> = transaction {
        EventRepository.deleteEvent(eventDeleteForm)
    }

    override fun updateEvent(eventUpdate: EventUpdateForm): Either<ServiceError, Event> = transaction {
        EventRepository.updateEvent(eventUpdate)
            .flatMap { event ->
                ReportService.updateReport(event)   // automatically update the report. Rollback if this fails.
                    .fold({ rollback(); it.left() }, { event.right() })
            }
    }

}