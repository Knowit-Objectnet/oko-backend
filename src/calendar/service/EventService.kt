package ombruk.backend.calendar.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.RecurrenceRules
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.reporting.service.IReportService
import ombruk.backend.shared.error.ServiceError
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.reporting.service.ReportService
import org.jetbrains.exposed.sql.transactions.transaction

object EventService : IEventService {

    private fun saveRecurring(eventPostForm: EventPostForm) = transaction {
        eventPostForm.map { form ->
            EventRepository.insertEvent(form)
                .flatMap { event ->
                    ReportService.saveReport(event).fold({ rollback(); it.left() }, { event.right() })
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
        EventRepository.deleteEvent(eventDeleteForm)
    }

    override fun updateEvent(eventUpdate: EventUpdateForm) = transaction {
        EventRepository.updateEvent(eventUpdate)
            .flatMap { event ->
                ReportService.updateReport(event)
                    .fold({ rollback(); it.left() }, { event.right() })
            }
    }

}