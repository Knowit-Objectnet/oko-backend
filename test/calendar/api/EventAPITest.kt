package calendar.api

import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.service.EventService
import ombruk.backend.module
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.error.RepositoryError
import ombruk.backend.shared.error.ServiceError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class EventAPITest {
    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))
    init {
        initDB() // Don't want to do this. But EventRepository wont work without it
    }

    @BeforeEach
    fun setup(){
        mockkObject(EventService)
    }

    @AfterEach
    fun tearDown(){
        clearAllMocks()
    }

    @Nested
    inner class GetById{
        /**
         * Should also check content, but can't figure out MockK serialization atm.
         */
        @Test
        fun `get single event`(@MockK expected: Event){
            every { EventService.getEventByID(1) } returns expected.right()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

        @Test
        fun `get single event 404`(){
            every { EventService.getEventByID(1) } returns RepositoryError.NoRowsFound("test").left()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals("No rows found with provided ID, test", response.content)
            }
        }

        @Test
        fun `get single event invalid id`(){
            testGet("/events/0") {
                assertEquals(HttpStatusCode.UnprocessableEntity, response.status())
                assertEquals("id: Must be greater than 0", response.content)
            }
        }

        @Test
        fun `get single event 500`(){
            every { EventService.getEventByID(1) } returns ServiceError("test").left()

            testGet("/events/1") {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals("test", response.content)
            }
        }
    }

    @Nested
    inner class Get{
        /**
         * This is not a very good test, someone plz figure out how to fix.
         * The relaxed mock will basically make expected an empty list, which is not ideal.
         * We want a list with at least some mocked events. Serialization is what makes it difficult
         */
        @Test
        fun `get all events`(@MockK(relaxed = true) expected: List<Event>){
            every { EventService.getEvents(null, null) } returns expected.right()
            testGet("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events when there are none`(){
            every { EventService.getEvents(null, null) } returns RepositoryError.NoRowsFound("Not found").left()
            val expected = listOf<Event>()
            testGet("/events") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }
    }
}

fun testGet(path: String, func: TestApplicationCall.() -> Unit) = withTestApplication({module(true)}){
    with(handleRequest(HttpMethod.Get, path)){
        this.func()
    }
}