package calendar.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.event.EventUpdateForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import testutils.testDelete
import testutils.testGet
import testutils.testPatch
import testutils.testPost
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class EventAPITest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    init {
        initDB() // Don't want to do this. But EventRepository wont work without it
    }

    @BeforeEach
    fun setup() {
        mockkObject(EventRepository)
        mockkObject(EventService)
        mockkObject(StationRepository)
        mockkObject(PartnerRepository)
        mockkObject(Authorization)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    inner class GetById {
        @Test
        fun `get single event 200`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val expected = Event(1, LocalDateTime.now(), LocalDateTime.now(), s, p, null)
            every { EventService.getEventByID(1) } returns expected.right()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        @Test
        fun `get single event 404`() {
            every { EventService.getEventByID(1) } returns RepositoryError.NoRowsFound("test").left()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        @Test
        fun `get single event 422`() {
            testGet("/events/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        @Test
        fun `get single event 500`() {
            every { EventService.getEventByID(1) } returns ServiceError("test").left()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        @Test
        fun `get single event 400`() {
            testGet("/events/NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("id could not be parsed.", response.content)
            }
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `get events 200`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val e1 = Event(1, LocalDateTime.now(), LocalDateTime.now(), s, p, null)
            val e2 = e1.copy(2)
            val e3 = e1.copy(3)
            val expected = listOf(e1, e2, e3)

            every { EventService.getEvents(EventGetForm(), null) } returns expected.right()

            testGet("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events 500`() {
            every { EventService.getEvents(EventGetForm(), null) } returns ServiceError("test").left()

            testGet("/events") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        @Test
        fun `get events 400`() {
            testGet("/events?eventId=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("eventId could not be parsed.", response.content)
            }
        }

        @Test
        fun `get events 422`() {
            testGet("/events?eventId=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("eventId: Must be greater than 0", response.content)
            }
        }
    }

    @Nested
    inner class Post {

        @Test
        fun `post simple event`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), s.id, p.id)
            val expected = Event(1, form.startDateTime, form.endDateTime, s, p)

            every { EventService.saveEvent(form) } returns expected.right()
            every { PartnerRepository.exists(1) } returns true
            every { StationRepository.exists(1) } returns true

            testPost("/events", json.stringify(EventPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expected), response.content)
            }
        }
    }

    @Nested
    inner class Patch {

        @Test
        fun `patch simple event`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val initial = Event(1, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), s, p)
            val form = EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))
            val expected = initial.copy(startDateTime = form.startDateTime!!, endDateTime = form.endDateTime!!)

            every { EventService.updateEvent(form) } returns expected.right()
            every { EventRepository.getEventByID(1) } returns initial.right()

            testPatch("/events", json.stringify(EventUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expected), response.content)
            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete event by id`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val expected = listOf(Event(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), s, p))

            every { EventService.deleteEvent(EventDeleteForm(1)) } returns expected.right()

            testDelete("/events?eventId=1") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `delete all events`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val expected = listOf(Event(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), s, p))

            every { EventService.deleteEvent(EventDeleteForm()) } returns expected.right()

            testDelete("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }
    }
}
