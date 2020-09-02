package calendar.service

import arrow.core.Either
import arrow.core.right
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.RecurrenceRules
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.service.EventService
import ombruk.backend.reporting.model.Report
import ombruk.backend.reporting.service.ReportService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class EventServiceTest {

    @BeforeEach
    fun setup() {
        mockkObject(EventRepository)
        mockkObject(ReportService)
        mockkObject(RecurrenceRules)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @AfterAll
    fun finish() {
        unmockkAll()
    }

    @Nested
    inner class GetEvents {
        /**
         * Check that get by id returns the expected event.
         */
        @Test
        fun `get by id`(@MockK expectedEvent: Event) {
            val id = 1
            every { EventRepository.getEventByID(id) } returns expectedEvent.right()

            val actualEvent = EventService.getEventByID(id)
            require(actualEvent is Either.Right)

            assertEquals(expectedEvent, actualEvent.b)
        }

        /**
         * Check that get events returns the exepected list of events
         */
        @Test
        fun `get all`(@MockK expectedEvents: List<Event>) {
            every { EventRepository.getEvents(null) } returns expectedEvents.right()

            val actualEvents = EventService.getEvents()
            require(actualEvents is Either.Right)

            assertEquals(expectedEvents, actualEvents.b)
        }

        /**
         * Check that we can get the expected event when given a station id
         */
        @Test
        fun `get by station id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(stationId = 1)
            every { EventRepository.getEvents(form) } returns expectedEvents.right()

            val actualEvents = EventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        /**
         * Check that we can get the expected event when given a partner id
         */
        @Test
        fun `get by partner id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(partnerId = 1)
            every { EventRepository.getEvents(form) } returns expectedEvents.right()

            val actualEvents = EventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        /**
         * Check that we can get the expected event when given a station and partner id
         */
        @Test
        fun `get by partner and station id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(partnerId = 1, stationId = 1)
            every { EventRepository.getEvents(form) } returns expectedEvents.right()

            val actualEvents = EventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        /**
         * Check that we can get the expected event when given a date time range
         */
        @Test
        fun `get by datetime range`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(
                fromDate = LocalDateTime.parse("2020-08-15T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-08-20T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
            )
            every { EventRepository.getEvents(form) } returns expectedEvents.right()

            val actualEvents = EventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        /**
         * Check that we can get the expected event when given a recurrence rule id
         */
        @Test
        fun `get by recurrenceRule id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(recurrenceRuleId = 1)
            every { EventRepository.getEvents(form) } returns expectedEvents.right()

            val actualEvents = EventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

    }

    @Nested
    inner class SaveEvents {

        /**
         * Check that save single events calls the repository and returns the saved event.
         */
        @Test
        fun `save single event`(@MockK expectedEvent: Event, @MockK report: Report) {
            val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val form = EventPostForm(from, from.plusHours(1), 1, 1)
            every { EventRepository.insertEvent(form) } returns expectedEvent.right()
            every { ReportService.saveReport(expectedEvent) } returns report.right()

            val actualEvent = EventService.saveEvent(form)
            require(actualEvent is Either.Right)
            verify(exactly = 1) { EventRepository.insertEvent(form) }
            assertEquals(expectedEvent, actualEvent.b)
        }

        /**
         * Check that the repository is called 3 times, because 3 events should be saved.
         */
        @Test
        fun `save recurring event`(@MockK expectedEvent: Event, @MockK report: Report) {
            val rRule = RecurrenceRule(count = 3, days = listOf(DayOfWeek.MONDAY))
            val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val form = EventPostForm(from, from.plusHours(1), 1, 1, recurrenceRule = rRule)

            every { RecurrenceRules.insertRecurrenceRule(rRule) } returns rRule.right()
            every { ReportService.saveReport(expectedEvent) } returns report.right()
            every { EventRepository.insertEvent(any()) } returns expectedEvent.right()

            val actualEvent = EventService.saveEvent(form)
            require(actualEvent is Either.Right)
            verify(exactly = 3) { EventRepository.insertEvent(any()) }
        }

    }

    @Nested
    inner class UpdateEvents {

        /**
         * Check that update event returns the updated event.
         */
        @Test
        fun `update single event`(@MockK expectedEvent: Event) {
            val from = LocalDateTime.parse("2020-09-02T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME)
            val updateForm = EventUpdateForm(1, from, from.plusHours(1))

            every { ReportService.updateReport(expectedEvent) } returns Unit.right()
            every { EventRepository.updateEvent(updateForm) } returns expectedEvent.right()

            val actualEvent = EventService.updateEvent(updateForm)

            require(actualEvent is Either.Right)
            assertEquals(expectedEvent, actualEvent.b)
        }
    }

    @Nested
    inner class DeleteEvents {

        /**
         * Delete event doesn't really have any logic so we just have to check if it actually calls
         * the repository.
         */
        @Test
        fun `delete event by id`() {
            val deleteForm = EventDeleteForm(1)

            EventService.deleteEvent(deleteForm)
            verify(exactly = 1) { EventRepository.deleteEvent(deleteForm) }
        }

    }
}