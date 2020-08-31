package calendar.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import ombruk.backend.shared.api.JwtMockConfig
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

    @BeforeEach
    fun setup() {
        mockkObject(EventRepository)
        mockkObject(EventService)
        mockkObject(StationRepository)
        mockkObject(PartnerRepository)
        mockkObject(Authorization)
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
    inner class GetById {
        /**
         * Check for 200 given a valid id
         */
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

        /**
         * Check for 404 given an id that is not present
         */
        @Test
        fun `get single event 404`() {
            every { EventService.getEventByID(1) } returns RepositoryError.NoRowsFound("test").left()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        /**
         * Check for 422 when the id is not valid
         */
        @Test
        fun `get single event 422`() {
            testGet("/events/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */
        @Test
        fun `get single event 500`() {
            every { EventService.getEventByID(1) } returns ServiceError("test").left()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when we get an id which can't be parsed as an int
         */
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

        /**
         * Check for 200 when we try to get all events with an empty form
         */
        @Test
        fun `get events 200`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val e1 = Event(1, LocalDateTime.now(), LocalDateTime.now(), s, p, null)
            val e2 = e1.copy(2)
            val e3 = e1.copy(3)
            val expected = listOf(e1, e2, e3)

            every { EventService.getEvents(EventGetForm()) } returns expected.right()

            testGet("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error
         */
        @Test
        fun `get events 500`() {
            every { EventService.getEvents(EventGetForm()) } returns ServiceError("test").left()

            testGet("/events") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed. eventId is not a number here.
         */
        @Test
        fun `get events 400`() {
            testGet("/events?eventId=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("eventId could not be parsed.", response.content)
            }
        }

        /**
         * Check for 422 when we get an invalid form. eventId is not valid here.
         */
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

        /**
         * Check for 200 when we post a valid form.
         */
        @Test
        fun `post event 200`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), s.id, p.id)
            val expected = Event(1, form.startDateTime, form.endDateTime, s, p)

            every { EventService.saveEvent(form) } returns expected.right()
            every { PartnerRepository.exists(1) } returns true
            every { StationRepository.exists(1) } returns true

            testPost("/events", json.stringify(EventPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 401 when we don't have a bearer
         */
        @Test
        fun `post event 401`() {
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, 1)

            every { PartnerRepository.exists(1) } returns true
            every { StationRepository.exists(1) } returns true

            testPost("/events", json.stringify(EventPostForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when we don't have the required role
         */
        @Test
        fun `post event 403`() {
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, 1)

            every { PartnerRepository.exists(1) } returns true
            every { StationRepository.exists(1) } returns true

            testPost("/events", json.stringify(EventPostForm.serializer(), form), JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `post event 500`() {
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 1)

            every { EventService.saveEvent(form) } returns ServiceError("test").left()
            every { PartnerRepository.exists(1) } returns true
            every { StationRepository.exists(1) } returns true

            testPost("/events", json.stringify(EventPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }

        /**
         * Check for 422 when we get an invalid form. The partner with id 1 does not exist.
         */
        @Test
        fun `post event 422`() {
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1, 1)

            every { PartnerRepository.exists(1) } returns false // Partner does not exist
            every { StationRepository.exists(1) } returns true

            testPost("/events", json.stringify(EventPostForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("partnerId: entity does not exist", response.content)
            }
        }

        /**
         * Check for 400 when we get a form we can't parse. The empty string can't be parsed to our post form.
         */
        @Test
        fun `post event 400`() {
            testPost("/events", "") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }

    }

    @Nested
    inner class Patch {

        /**
         * Check for 200 when we get a valid patch form.
         */
        @Test
        fun `patch event 200`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val initial = Event(1, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1), s, p)
            val form = EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1))
            val expected = initial.copy(startDateTime = form.startDateTime!!, endDateTime = form.endDateTime!!)

            every { EventService.updateEvent(form) } returns expected.right()
            every { EventRepository.getEventByID(1) } returns initial.right()

            testPatch("/events", json.stringify(EventUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `patch event 500`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val initial = Event(1, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1), s, p)
            val form = EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusHours(1))

            every { EventService.updateEvent(form) } returns ServiceError("test").left()
            every { EventRepository.getEventByID(1) } returns initial.right()

            testPatch("/events", json.stringify(EventUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 401 when no bearer is present
         */
        @Test
        fun `patch event 401`() {
            val form = EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))

            testPatch("/events", json.stringify(EventUpdateForm.serializer(), form), null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 403 when we don't have the required role.
         */
        @Test
        fun `patch event 403`() {
            val form = EventUpdateForm(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))

            testPatch("/events", json.stringify(EventUpdateForm.serializer(), form), JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }

        /**
         * Check for 422 when we get an invalid form. The id can't be -1.
         */
        @Test
        fun `patch event 422`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val initial = Event(1, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), s, p)
            val form = EventUpdateForm(-1, LocalDateTime.now(), LocalDateTime.now().plusDays(1))
            val expected = initial.copy(startDateTime = form.startDateTime!!, endDateTime = form.endDateTime!!)

            every { EventService.updateEvent(form) } returns expected.right()
            every { EventRepository.getEventByID(1) } returns initial.right()

            testPatch("/events", json.stringify(EventUpdateForm.serializer(), form)) {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)

            }
        }

        /**
         * Check for 400 when we get a form which can't be parsed.
         * The empty string can't be parsed to out patch form.
         */
        @Test
        fun `patch event 400`() {
            testPatch("/events", "") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Nested
    inner class Delete {

        /**
         * Check for 200 when we get a valid delete form.
         */
        @Test
        fun `delete events 200`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val expected = listOf(Event(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), s, p))

            every { EventService.deleteEvent(EventDeleteForm()) } returns expected.right()

            testDelete("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        /**
         * Check for 500 when we encounter a serious error.
         */
        @Test
        fun `delete events 500`() {
            every { EventService.deleteEvent(EventDeleteForm()) } returns ServiceError("test").left()

            testDelete("/events") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
            }
        }

        /**
         * Check for 401 when no bearer is present.
         */
        @Test
        fun `delete events 401`() {
            testDelete("/events", null) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

        /**
         * Check for 404 when there is no events that match our partner id. This is only "semi intended" behaviour
         * Someone please fix later. @todo FIX this
         */
        @Test
        fun `delete events 404`() {
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val expected = listOf(Event(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), s, p))

            every { EventService.getEvents(EventGetForm()) } returns expected.right()
            every { EventService.deleteEvent(EventDeleteForm()) } returns expected.right()

            testDelete("/events", JwtMockConfig.partnerBearer2) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

        /**
         * Check for 422 when form is invalid. eventId can't be -1.
         */
        @Test
        fun `delete event 422`() {
            testDelete("/events?eventId=-1") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("eventId: Must be greater than 0", response.content)

            }
        }

        /**
         * Check for 400 when we get a form that can't be parsed. eventId has to be an int.
         */
        @Test
        fun `delete event 400`() {
            testDelete("/events?eventId=NaN") {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals("eventId could not be parsed.", response.content)

            }
        }
    }
}
