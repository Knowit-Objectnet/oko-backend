package calendar.service

import arrow.core.Either
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.service.EventService
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
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

    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class GetEvents {
        @Test
        fun `get event by id`(@MockK expectedEvent: Event) {
            val id = 1
            every { EventRepository.getEventByID(id) } answers { expectedEvent.right() }

            val actualEvent = eventService.getEventByID(id)
            require(actualEvent is Either.Right)

            assertEquals(expectedEvent, actualEvent.b)
        }

        @Test
        fun `get all events`(@MockK expectedEvents: List<Event>) {
            every { EventRepository.getEvents(null, null) } answers { expectedEvents.right() }

            val actualEvents = eventService.getEvents()
            require(actualEvents is Either.Right)

            assertEquals(expectedEvents, actualEvents.b)
        }
    }

}