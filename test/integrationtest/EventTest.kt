
import arrow.core.Either
import arrow.core.extensions.list.functorFilter.filter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.form.station.StationPostForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.calendar.service.StationService
import ombruk.backend.partner.form.PartnerPostForm
import ombruk.backend.partner.model.Partner
import ombruk.backend.partner.service.PartnerService
import ombruk.backend.shared.database.initDB
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import testutils.testGet
import testutils.testPost
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    val partners: List<Partner>
    val stations: List<Station>
    val events: List<Event>

    init {
        initDB()
        partners = createTestPartners()
        stations = createTestStations()
        events = createTestEvents()
    }

    private fun createTestPartners() = (1..9).map {
        val p = PartnerService.savePartner(
            PartnerPostForm(
                "TestPartner$it",
                "Description",
                "1234567$it",
                "test$it@gmail.com"
            )
        )
        require(p is Either.Right)
        return@map p.b
    }

    private fun createTestStations() = (1..5).map {
        val s = StationService.saveStation(StationPostForm("Station$it"))
        require(s is Either.Right)
        return@map s.b
    }

    private fun createTestEvents() = (1..100L).map {
        val e = EventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-06T15:48:06").plusDays(it),
                LocalDateTime.parse("2020-07-06T15:48:06").plusDays(it).plusHours(1),
                Random.nextInt(1, 5),
                Random.nextInt(1, 9)
            )
        )

        require(e is Either.Right)
        return@map e.b
    }

    @Nested
    inner class Get {
        @Test
        fun `get event by id`(){
            testGet("/events/${events[45].id}"){
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), events[45]), response.content)
            }
        }

        @Test
        fun `get all events`(){
            testGet("/events/"){
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, events), response.content)
            }
        }

        @Test
        fun `get events by stationId`(){
            testGet("/events?stationId=${stations[3].id}"){
                val expected = events.filter { it.station == stations[3] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events by partnerId`(){
            testGet("/events?partnerId=${partners[7].id}"){
                val expected = events.filter { it.partner == partners[7] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events by stationId and partnerId`(){
            testGet("/events?stationId=${stations[2].id}&partnerId=${partners[4].id}"){
                val expected = events.filter { it.station == stations[2] }.filter { it.partner == partners[4] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events from date`(){
            testGet("/events?fromDate=2020-08-10T15:00:00"){
                val expected = events.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events to date`(){
            testGet("/events?toDate=2020-08-10T15:00:00"){
                val expected = events.filter { it.startDateTime <= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events in date range`(){
            testGet("/events?fromDate=2020-08-10T15:00:00&toDate=2020-09-10T15:00:00"){
                val expected = events.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }
                    .filter { it.startDateTime <= LocalDateTime.parse("2020-09-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

    }

    @Nested
    inner class Post {

        @Test
        fun `create single event`(){
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stationId = Random.nextInt(1, 5)
            val partnerId = Random.nextInt(1, 9)

            val body = """{
                "startDateTime": "2020-07-06T15:00:00",
                "endDateTime": "2020-07-06T16:00:00",
                "stationId": "$stationId",
                "partnerId": "$partnerId"
                }""".trimIndent()

            testPost("/events", body){
                assertEquals(HttpStatusCode.OK, response.status())
                val responseEvent = json.parse(Event.serializer(), response.content!!)
                val insertedEvent = EventRepository.getEventByID(responseEvent.id)
                require(insertedEvent is Either.Right)
                assertEquals(responseEvent, insertedEvent.b)
                assertEquals(startDateTime, responseEvent.startDateTime)
                assertEquals(endDateTime, responseEvent.endDateTime)
                assertEquals(stationId, responseEvent.station.id)
                assertEquals(partnerId, responseEvent.partner.id)
            }
        }
    }
}