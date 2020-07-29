package ombruk.backend.calendar.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.RecurrenceRules
import ombruk.backend.calendar.form.EventPostForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.reporting.service.IReportService
import ombruk.backend.shared.error.ServiceError
import calendar.form.EventGetForm
import org.jetbrains.exposed.sql.transactions.transaction

class EventService(private val reportingService: IReportService) : IEventService {

    private fun saveRecurring(eventPostForm: EventPostForm) = transaction {
        eventPostForm.map { form ->
            EventRepository.insertEvent(form)
                .flatMap { event ->
                    reportingService.saveReport(event).fold({ rollback(); it.left() }, { event.right() })
                }
        }.first()
    }

    override fun saveEvent(eventPostForm: EventPostForm): Either<ServiceError, Event> = transaction {
        let { eventPostForm.recurrenceRule?.let { RecurrenceRules.insertRecurrenceRule(it) } ?: Unit.right() }
            .flatMap { saveRecurring(eventPostForm) }
            .fold({ rollback(); it.left() }, { it.right() })
    }

    override fun getEventByID(id: Int): Either<ServiceError, Event> = transaction {
        EventRepository.getEventByID(id)
    }

    override fun getEvents(eventGetForm: EventGetForm?, eventType: EventType?) = transaction {
        EventRepository.getEvents(eventGetForm, eventType)
    }

    override fun deleteEvent(eventDeleteForm: EventDeleteForm) = transaction {
        transaction { EventRepository.deleteEvent(eventDeleteForm) }
    }

    override fun updateEvent(eventUpdate: EventUpdateForm) = transaction { EventRepository.updateEvent(eventUpdate) }

}