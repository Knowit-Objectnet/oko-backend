package calendar.service

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
import ombruk.backend.calendar.service.EventService
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class EventServiceTest {

    init {
        initDB() // Don't want to do this. But EventRepository wont work without it
    }

    private val eventService = EventService(ReportService)

    @BeforeEach
    fun setup() {
        mockkObject(EventRepository)
        mockkObject(ReportService)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class GetEvents {
        @Test
        fun `by id`(@MockK expectedEvent: Event) {
            val id = 1
            every { EventRepository.getEventByID(id) } returns expectedEvent.right()

            val actualEvent = eventService.getEventByID(id)
            require(actualEvent is Either.Right)

            assertEquals(expectedEvent, actualEvent.b)
        }

        @Test
        fun `all`(@MockK expectedEvents: List<Event>) {
            every { EventRepository.getEvents(null, null) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents()
            require(actualEvents is Either.Right)

            assertEquals(expectedEvents, actualEvents.b)
        }

        @Test
        fun `by station id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(stationId = 1)
            every { EventRepository.getEvents(form, null) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        @Test
        fun `by partner id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(partnerId = 1)
            every { EventRepository.getEvents(form, null) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        @Test
        fun `by partner and station id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(partnerId = 1, stationId = 1)
            every { EventRepository.getEvents(form, null) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        @Test
        fun `by datetime range`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(
                fromDate = LocalDateTime.parse("2020-08-15T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                toDate = LocalDateTime.parse("2020-08-20T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
            )
            every { EventRepository.getEvents(form, null) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        @Test
        fun `by recurrenceRule id`(@MockK expectedEvents: List<Event>) {
            val form = EventGetForm(recurrenceRuleId = 1)
            every { EventRepository.getEvents(form, null) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(form)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }


        @Test
        fun `by event type single`(@MockK expectedEvents: List<Event>) {
            every { EventRepository.getEvents(null, EventType.SINGLE) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(eventType = EventType.SINGLE)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

        @Test
        fun `by event type recurring`(@MockK expectedEvents: List<Event>) {
            every { EventRepository.getEvents(null, EventType.RECURRING) } returns expectedEvents.right()

            val actualEvents = eventService.getEvents(eventType = EventType.RECURRING)
            require(actualEvents is Either.Right)
            assertEquals(expectedEvents, actualEvents.b)
        }

    }

    @Nested
    inner class SaveEvents {

        /* SAVING temp for later
        @Test
        fun `single event`(@MockK expectedEvent: Event) {
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now(), 1, 1)
            every { EventRepository.insertEvent(form) } returns expectedEvent.right()

            val actualEvent = eventService.saveEvent(form)
            require(actualEvent is Either.Right)
            assertEquals(expectedEvent, actualEvent.b)
        }

        @Test
        fun `recurring event`() {
            val e1 = mockkClass(Event::class)
            val e2 = mockkClass(Event::class)
            val e3 = mockkClass(Event::class)
            val expectedEvent = e1

            val form = EventPostForm(
                LocalDateTime.now(), LocalDateTime.now(), 1, 1, recurrenceRule = RecurrenceRule(
                    count = 3,
                    days = listOf(DayOfWeek.MONDAY)
                )
            )


            every { EventRepository.insertEvent(form) } returns e1.right() andThen e2.right() andThen e3.right()

            val actualEvent = eventService.saveEvent(form)
            require(actualEvent is Either.Right)
            assertEquals(expectedEvent, actualEvent.b)
        }
        */

    }
}