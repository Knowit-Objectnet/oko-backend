package no.oslokommune.ombruk.event

import arrow.core.Either
import arrow.core.extensions.list.functorFilter.filter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.DefaultJsonConfiguration
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import no.oslokommune.ombruk.event.database.EventRepository
import no.oslokommune.ombruk.station.database.StationRepository
import no.oslokommune.ombruk.event.form.EventDeleteForm
import no.oslokommune.ombruk.event.form.EventGetForm
import no.oslokommune.ombruk.event.form.EventPostForm
import no.oslokommune.ombruk.station.form.StationPostForm
import no.oslokommune.ombruk.event.model.Event
import no.oslokommune.ombruk.event.model.RecurrenceRule
import no.oslokommune.ombruk.station.model.Station
import no.oslokommune.ombruk.event.service.EventService
import no.oslokommune.ombruk.station.service.StationService
import no.oslokommune.ombruk.partner.database.PartnerRepository
import no.oslokommune.ombruk.partner.form.PartnerPostForm
import no.oslokommune.ombruk.partner.model.Partner
import no.oslokommune.ombruk.partner.service.PartnerService
import no.oslokommune.ombruk.shared.database.initDB
import org.junit.jupiter.api.*
import no.oslokommune.ombruk.testDelete
import no.oslokommune.ombruk.testGet
import no.oslokommune.ombruk.testPatch
import no.oslokommune.ombruk.testPost
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventTest {

    val json = Json(DefaultJsonConfiguration.copy(prettyPrint = true))

    var partners: List<Partner>
    var stations: List<Station>
    lateinit var events: List<Event>

    init {
        initDB()
        partners = createTestPartners()
        stations = createTestStations()
    }

    @BeforeEach
    fun setup() {
        events = createTestEvents()
    }

    @AfterEach
    fun teardown() {
        EventRepository.deleteEvent(EventDeleteForm())
    }

    @AfterAll
    fun finish() {
        PartnerRepository.deleteAllPartners()
        StationRepository.deleteAllStations()
    }

    private fun createTestPartners() = (0..9).map {
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

    private fun createTestStations() = (0..5).map {
        val s = StationService.saveStation(StationPostForm("no.oslokommune.ombruk.event.EventTest Station$it", hours = openHours()))
        require(s is Either.Right)
        return@map s.b
    }

    private fun createTestEvents(): List<Event> {
        var partnerCounter = 0
        var stationCounter = 0
        return (1..100L).map {
            val e = EventService.saveEvent(
                EventPostForm(
                    LocalDateTime.parse("2020-07-06T15:48:06").plusDays(it),
                    LocalDateTime.parse("2020-07-06T16:48:06").plusDays(it),
                    stations[stationCounter].id,
                    partners[partnerCounter].id
                )
            )

            partnerCounter++
            if (partnerCounter % 10 == 0) {
                partnerCounter = 0
                stationCounter = (stationCounter + 1) % 6
            }

            require(e is Either.Right)
            return@map e.b
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `get event by id`() {
            testGet("/events/${events[45].id}") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), events[45]), response.content)
            }
        }

        @Test
        fun `get all events`() {
            testGet("/events/") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, events), response.content)
            }
        }

        @Test
        fun `get events by stationId`() {
            testGet("/events?stationId=${stations[3].id}") {
                val expected = events.filter { it.station == stations[3] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events by partnerId`() {
            testGet("/events?partnerId=${partners[7].id}") {
                val expected = events.filter { it.partner == partners[7] }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events by stationId and partnerId`() {
            testGet("/events?stationId=${stations[2].id}&partnerId=${partners[4].id}") {
                val expected = events.filter { it.station == stations[2] }.filter { it.partner == partners[4] }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events from date`() {
            testGet("/events?fromDate=2020-08-10T15:00:00") {
                val expected = events.filter { it.startDateTime >= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events to date`() {
            testGet("/events?toDate=2020-08-10T15:00:00") {
                val expected = events.filter { it.startDateTime <= LocalDateTime.parse("2020-08-10T15:00:00") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer().list, expected), response.content)
            }
        }

        @Test
        fun `get events in date range`() {
            testGet("/events?fromDate=2020-08-10T15:00:00&toDate=2020-09-10T15:00:00") {
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
        fun `create single event`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stationId = stations[Random.nextInt(1, 5)].id
            val partnerId = partners[Random.nextInt(1, 9)].id

            val body =
                """{
                    "startDateTime": "2020-07-06T15:00:00",
                    "endDateTime": "2020-07-06T16:00:00",
                    "stationId": "$stationId",
                    "partnerId": "$partnerId"
                }"""

            testPost("/events", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseEvent = json.parse(Event.serializer(), response.content!!)
                val insertedEvent = EventRepository.getEventByID(responseEvent.id)
                require(insertedEvent is Either.Right)
                assertEquals(responseEvent, insertedEvent.b)
                assertEquals(startDateTime, responseEvent.startDateTime)
                assertEquals(endDateTime, responseEvent.endDateTime)
                assertEquals(stationId, responseEvent.station.id)
                assertEquals(partnerId, responseEvent.partner?.id)
            }
        }

        @Test
        fun `create recurring event`() {
            val startDateTime = LocalDateTime.parse("2020-07-06T15:00:00")
            val endDateTime = LocalDateTime.parse("2020-07-06T16:00:00")
            val stationId = stations[Random.nextInt(1, 5)].id
            val partnerId = partners[Random.nextInt(1, 9)].id
            val rRule = RecurrenceRule(count = 5)

            val body =
                """{
                    "startDateTime": "2020-07-06T15:00:00",
                    "endDateTime": "2020-07-06T16:00:00",
                    "stationId": "$stationId",
                    "partnerId": "$partnerId",
                    "recurrenceRule": { "count": "${rRule.count}" }
                }"""

            testPost("/events", body) {
                val responseEvent = json.parse(Event.serializer(), response.content!!)
                val insertedEvents =
                    EventRepository.getEvents(EventGetForm(recurrenceRuleId = responseEvent.recurrenceRule!!.id))

                assertEquals(HttpStatusCode.OK, response.status())
                require(insertedEvents is Either.Right)
                assertTrue { insertedEvents.b.contains(responseEvent) }
                assertEquals(insertedEvents.b.size, 5)

                insertedEvents.b.forEachIndexed { index, event ->
                    assertEquals(startDateTime.plusDays(7L * index), event.startDateTime)
                    assertEquals(endDateTime.plusDays(7L * index), event.endDateTime)
                    assertEquals(stationId, event.station.id)
                    assertEquals(partnerId, event.partner?.id)
                }
            }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delete event by id`() {
            testDelete("/events?eventId=${events[68].id}") {
                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(listOf(events[68]), respondedEvents)
                assertFalse(EventRepository.exists(events[68].id))
            }
        }

        @Test
        fun `delete events by station id`() {
            testDelete("/events?stationId=${stations[1].id}") {
                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                val deletedEvents = events.filter { it.station.id == stations[1].id }
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedEvents, respondedEvents)
                deletedEvents.forEach {
                    assertFalse(EventRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete events by partner id`() {
            testDelete("/events?partnerId=${partners[8].id}") {
                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                val deletedEvents = events.filter { it.partner?.id == partners[8].id }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedEvents, respondedEvents)
                deletedEvents.forEach {
                    assertFalse(EventRepository.exists(it.id))
                }
            }
        }


        @Test
        fun `delete events by partner id and station id`() {
            testDelete("/events?partnerId=${partners[7].id}&stationId=${stations[2].id}") {
                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                val deletedEvents =
                    events.filter { it.partner?.id == partners[7].id }.filter { it.station.id == stations[2].id }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedEvents, respondedEvents)
                deletedEvents.forEach {
                    assertFalse(EventRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete events from date`() {
            testDelete("/events?fromDate=2020-09-06T15:48:06") {
                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                val deletedEvents = events.filter { it.startDateTime >= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedEvents, respondedEvents)
                deletedEvents.forEach {
                    assertFalse(EventRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete events to date`() {
            testDelete("/events?toDate=2020-09-06T15:48:06") {

                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                val deletedEvents = events.filter { it.startDateTime <= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedEvents, respondedEvents)
                deletedEvents.forEach {
                    assertFalse(EventRepository.exists(it.id))
                }
            }
        }

        @Test
        fun `delete events in date range`() {
            testDelete("/events?fromDate=2020-08-06T15:48:06&toDate=2020-09-06T15:48:06") {

                val respondedEvents = json.parse(Event.serializer().list, response.content!!)
                val deletedEvents = events.filter { it.startDateTime >= LocalDateTime.parse("2020-08-06T15:48:06") }
                    .filter { it.startDateTime <= LocalDateTime.parse("2020-09-06T15:48:06") }

                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(deletedEvents, respondedEvents)
                deletedEvents.forEach {
                    assertFalse(EventRepository.exists(it.id))
                }
            }
        }
    }

    @Nested
    inner class Patch {

        @Test
        fun `update event start`() {
            val eventToUpdate = events[87]
            val expectedResponse = eventToUpdate.copy(startDateTime = eventToUpdate.startDateTime.minusHours(1))
            val body =
                """{
                    "id": "${eventToUpdate.id}",
                    "startDateTime": "${eventToUpdate.startDateTime.minusHours(1)}"
                }"""

            testPatch("/events", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expectedResponse), response.content)

                val eventInRepository = EventRepository.getEventByID(expectedResponse.id)
                require(eventInRepository is Either.Right)
                assertEquals(expectedResponse, eventInRepository.b)

            }
        }

        @Test
        fun `update event end`() {
            val eventToUpdate = events[87]
            val expectedResponse = eventToUpdate.copy(endDateTime = eventToUpdate.endDateTime.plusHours(1))
            val body =
                """{
                    "id": "${eventToUpdate.id}",
                    "endDateTime": "${eventToUpdate.endDateTime.plusHours(1)}"
                }"""

            testPatch("/events", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expectedResponse), response.content)

                val eventInRepository = EventRepository.getEventByID(expectedResponse.id)
                require(eventInRepository is Either.Right)
                assertEquals(expectedResponse, eventInRepository.b)

            }
        }

        @Test
        fun `update event start and end`() {
            val eventToUpdate = events[87]
            val expectedResponse = eventToUpdate.copy(
                startDateTime = eventToUpdate.startDateTime.minusHours(1),
                endDateTime = eventToUpdate.endDateTime.plusHours(1)
            )
            val body =
                """{
                    "id": "${eventToUpdate.id}",
                    "startDateTime": "${eventToUpdate.startDateTime.minusHours(1)}",
                    "endDateTime": "${eventToUpdate.endDateTime.plusHours(1)}"
                }"""

            testPatch("/events", body) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(json.stringify(Event.serializer(), expectedResponse), response.content)

                val eventInRepository = EventRepository.getEventByID(expectedResponse.id)
                require(eventInRepository is Either.Right)
                assertEquals(expectedResponse, eventInRepository.b)

            }
        }
    }

    private fun openHours() = mapOf<DayOfWeek, List<LocalTime>>(
        Pair(
            DayOfWeek.MONDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.TUESDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.WEDNESDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.THURSDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        ),
        Pair(
            DayOfWeek.FRIDAY,
            listOf(
                LocalTime.parse("00:00:00Z", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("23:59:59Z", DateTimeFormatter.ISO_TIME)
            )
        )
    )
}