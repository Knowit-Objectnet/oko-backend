package calendar.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.database.StationRepository
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.module
import ombruk.backend.partner.database.PartnerRepository
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.api.Authorization
import ombruk.backend.shared.api.JwtMockConfig
import ombruk.backend.shared.api.Roles
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
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
        fun `get single event`() {
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
        fun `get single event invalid id`() {
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
    }

    @Nested
    inner class Get {
        @Test
        fun `get all events`() {
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
        fun `get events when there are none`() {
            every { EventService.getEvents(null, null) } returns RepositoryError.NoRowsFound("Not found").left()
            val expected = listOf<Event>()
            testGet("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }
    }

    @Nested
    inner class Post{

        @Test
        fun `post simple event`(){
            val s = Station(1, "test")
            val p = Partner(1, "test")
            val form = EventPostForm(LocalDateTime.now(), LocalDateTime.now().plusDays(1), s.id, p.id)
            val expected = Event(1, form.startDateTime, form.endDateTime, s, p)

            every{ EventService.saveEvent(form) } returns expected.right()
            every{ PartnerRepository.exists(1) } returns true
            every{ StationRepository.exists(1) } returns true
            every{ Authorization.authorizeRole(any(), any()) } returns (Roles.RegEmployee to 1).right()

            testPost("/events", json.stringify(EventPostForm.serializer(), form)){
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expected), response.content)
            }
        }
    }

    @Nested
    inner class Patch

    @Nested
    inner class Delete
}

fun testGet(path: String, func: TestApplicationCall.() -> Unit) =
    withTestApplication({ module(true) }) {
        handleRequest(HttpMethod.Get, path).apply(func)
    }

fun testPost(path: String, body: String, bearer: String = JwtMockConfig.regEmployeeBearer, func: TestApplicationCall.() -> Unit) =
    withTestApplication({ module(true) }) {
        handleRequest(HttpMethod.Post, path) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(HttpHeaders.Authorization, "Bearer $bearer")
            setBody(body)
        }.apply(func)
    }