package calendar.service

import arrow.core.Either
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import ombruk.backend.shared.utils.rangeTo
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventServiceDeleteTest {
    private lateinit var testPartner: Partner
    private lateinit var testPartner2: Partner
    private lateinit var testStation: Station
    private lateinit var testStation2: Station

    init {
        initDB()
        transaction {
            val testPartnerId = Partners.insertAndGetId {
                it[name] = "TestPartner 1"
                it[description] = "Description of TestPartner 1"
                it[phone] = "+47 2381931"
                it[email] = "example@gmail.com"
            }.value

            testPartner =
                Partner(
                    testPartnerId,
                    "TestPartner 1",
                    "Description of TestPartner 1",
                    "+47 2381931",
                    "example@gmail.com"
                )

            val testPartnerId2 = Partners.insertAndGetId {
                it[name] = "TestPartner 2"
                it[description] = "Description of TestPartner 2"
                it[phone] = "911"
                it[email] = "example@gmail.com"
            }.value

            testPartner2 =
                Partner(
                    testPartnerId2,
                    "TestPartner 2",
                    "Description of TestPartner 2",
                    "911",
                    "example@gmail.com"
                )

            var opensAt = LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME)!!
            var closesAt = LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)!!
            var hours = mapOf(
                Pair(DayOfWeek.MONDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.TUESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.WEDNESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.THURSDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.FRIDAY, listOf(opensAt, closesAt))
            )
            val json = Json(JsonConfiguration.Stable)

            val testStationId = Stations.insertAndGetId {
                it[name] = "Test Station 1"
                it[Stations.hours] =
                    json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                        .toString()
            }.value

            testStation = Station(
                testStationId,
                "Test Station 1",
                hours
            )

            opensAt = LocalTime.parse("08:00:00", DateTimeFormatter.ISO_TIME)
            closesAt = LocalTime.parse("20:00:00", DateTimeFormatter.ISO_TIME)
            hours = mapOf(
                Pair(DayOfWeek.MONDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.TUESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.WEDNESDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.THURSDAY, listOf(opensAt, closesAt)),
                Pair(DayOfWeek.FRIDAY, listOf(opensAt, closesAt))
            )

            val testStationId2 = Stations.insertAndGetId {
                it[name] = "Test Station 2"
                it[Stations.hours] =
                    json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                        .toString()
            }.value
            testStation2 = Station(
                testStationId2,
                "Test Station 2",
                hours
            )

        }
    }

    @AfterAll
    fun cleanPartnersAndStationsFromDB() {
        transaction {
            Partners.deleteAll()
            Stations.deleteAll()
        }
    }

    @AfterEach
    fun cleanEventsFromDB() {
        transaction {
            Events.deleteAll()
        }
    }


    @KtorExperimentalLocationsAPI
    @Test
    fun testDeleteEventByid() {

        val eventToDelete = EventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        val eventNotToDelete = EventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        require(eventToDelete is Either.Right)
        require(eventNotToDelete is Either.Right)

        val deleteForm = EventDeleteForm(eventToDelete.b.id)

        assert(EventService.deleteEvent(deleteForm) is Either.Right)

        val eventLeftAfterDelete = EventService.getEvents()
        require(eventLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete.b, eventLeftAfterDelete.b.first())
    }


    @KtorExperimentalLocationsAPI
    @Test
    fun testDeleteEventByRecurrenceRuleId() {

        val eventToDelete = EventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )

        val eventNotToDelete = EventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        require(eventToDelete is Either.Right)
        require(eventNotToDelete is Either.Right)

        val deleteForm = EventDeleteForm(recurrenceRuleId = eventToDelete.b.recurrenceRule!!.id)

        assert(EventService.deleteEvent(deleteForm) is Either.Right)

        val eventLeftAfterDelete = EventService.getEvents()
        require(eventLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete.b, eventLeftAfterDelete.b.first())

    }

    @KtorExperimentalLocationsAPI
    @Test
    fun testDeleteInRange() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        // Save expected Events
        val createForm = EventPostForm(
            LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation.id,
            testPartner.id,
            RecurrenceRule(count = 7)
        )

        val recurrenceRuleId = EventService.saveEvent(createForm).map { it.recurrenceRule!!.id }
        require(recurrenceRuleId is Either.Right)

        val eventNotToDelete = dateRange.map {
            EventService.saveEvent(
                EventPostForm(
                    it,
                    it.plusHours(1),
                    testStation.id,
                    testPartner.id
                )
            )
        }.map { require(it is Either.Right); it.b }


        val deleteForm = EventDeleteForm(
            fromDate = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            toDate = LocalDateTime.parse("2021-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            recurrenceRuleId = recurrenceRuleId.b
        )

        assert(EventService.deleteEvent(deleteForm) is Either.Right)

        val eventsLeftAfterDelete = EventService.getEvents()
        require(eventsLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete, eventsLeftAfterDelete.b)
    }
}