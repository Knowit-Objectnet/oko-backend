package calendar.service

import arrow.core.Either
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.event.EventDeleteForm
import ombruk.backend.calendar.form.event.EventPostForm
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.utils.rangeTo
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventServiceDeleteTest {
    private val eventService = EventService(ReportService)
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


            val testStationId = Stations.insertAndGetId {
                it[name] = "Test Station 1"
                it[openingTime] = "09:00:00"
                it[closingTime] = "21:00:00"
            }.value

            testStation = Station(
                testStationId,
                "Test Station 1",
                LocalTime.parse("09:00:00", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("21:00:00", DateTimeFormatter.ISO_TIME)
            )

            val testStationId2 = Stations.insertAndGetId {
                it[name] = "Test Station 2"
                it[openingTime] = "08:00:00"
                it[closingTime] = "20:00:00"
            }.value
            testStation2 = Station(
                testStationId2,
                "Test Station 2",
                LocalTime.parse("08:00:00", DateTimeFormatter.ISO_TIME),
                LocalTime.parse("20:00:00", DateTimeFormatter.ISO_TIME)
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


    @Test
    fun testDeleteEventByid() {

        val eventToDelete = eventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        val eventNotToDelete = eventService.saveEvent(
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

        assert(eventService.deleteEvent(deleteForm) is Either.Right)

        val eventLeftAfterDelete = eventService.getEvents()
        require(eventLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete.b, eventLeftAfterDelete.b.first())
    }


    @Test
    fun testDeleteEventByRecurrenceRuleId() {

        val eventToDelete = eventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )

        val eventNotToDelete = eventService.saveEvent(
            EventPostForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        require(eventToDelete is Either.Right)
        require(eventNotToDelete is Either.Right)

        val deleteForm =
            EventDeleteForm(recurrenceRuleId = eventToDelete.b.recurrenceRule!!.id)

        assert(eventService.deleteEvent(deleteForm) is Either.Right)

        val eventLeftAfterDelete = eventService.getEvents()
        require(eventLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete.b, eventLeftAfterDelete.b.first())

    }

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

        val recurrenceRuleId = eventService.saveEvent(createForm).map { it.recurrenceRule!!.id }
        require(recurrenceRuleId is Either.Right)

        val eventNotToDelete = dateRange.map {
            eventService.saveEvent(
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

        assert(eventService.deleteEvent(deleteForm) is Either.Right)

        val eventsLeftAfterDelete = eventService.getEvents()
        require(eventsLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete, eventsLeftAfterDelete.b)
    }
}