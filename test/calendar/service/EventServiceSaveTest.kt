package calendar.service

import arrow.core.Either
import calendar.utils.eventCreateFormFromEvent
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.reporting.service.ReportService
import ombruk.backend.shared.database.initDB
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class EventServiceSaveTest {
    companion object {
        lateinit var eventService: EventService
        lateinit var testPartner: Partner
        lateinit var testPartner2: Partner
        lateinit var testStation: Station
        lateinit var testStation2: Station

        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            transaction {
                val testPartnerId = Partners.insertAndGetId {
                    it[name] = "Test Partner 1"
                }.value

                testPartner = Partner(testPartnerId, "Test Partner 1")

                val testPartnerId2 = Partners.insertAndGetId {
                    it[name] = "Test Partner 2"
                }.value

                testPartner2 = Partner(testPartnerId2, "Test Partner 2")


                val testStationId = Stations.insertAndGetId {
                    it[name] = "Test Station 1"
                }.value

                testStation = Station(testStationId, "Test Station 1")

                val testStationId2 = Stations.insertAndGetId {
                    it[name] = "Test Station 2"
                }.value
                testStation2 = Station(testStationId2, "Test Station 2")
            }

            eventService = EventService(ReportService)
        }

    }

    @After
    fun cleanEventsFromDB() {
        transaction {
            Events.deleteAll()
        }
    }

    @Test
    fun testSaveEvent() {
        val eventToInsert = Event(
            0,
            startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation
        )
        val actualEvent = eventService.saveEvent(eventCreateFormFromEvent(eventToInsert))
        require(actualEvent is Either.Right)

        val expectedEvent = eventToInsert.copy(id = actualEvent.b.id)

        assertEquals(expectedEvent, actualEvent.b)
    }

    @Test
    fun testSaveRecurringEvent() {

        val createForm = CreateEventForm(
            startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
            partnerId = testPartner.id,
            stationId = testStation.id,
            recurrenceRule = RecurrenceRule(
                count = 37,
                days = listOf(DayOfWeek.MONDAY)
            )
        )

        eventService.saveEvent(createForm)

        val actualEvents = eventService.getEvents()
        require(actualEvents is Either.Right)

        val firstId = actualEvents.b.first().id
        val expectedEvents = createForm.mapIndexed { index: Int, form: CreateEventForm ->
            Event(firstId + index, form.startDateTime, form.endDateTime, testStation, testPartner, form.recurrenceRule)
        }

        assertEquals(expectedEvents, actualEvents.b)

    }


}