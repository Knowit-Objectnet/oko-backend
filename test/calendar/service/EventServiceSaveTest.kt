package calendar.service

import arrow.core.Either
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.EventPostForm
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
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
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

            eventService = EventService(ReportService)
        }
        @AfterClass
        @JvmStatic
        fun cleanPartnersAndStationsFromDB(){
            transaction {
                Partners.deleteAll()
                Stations.deleteAll()
            }
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
        val expectedEvent = eventService.saveEvent(
            EventPostForm(
                startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
                endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
                partnerId = testPartner.id,
                stationId = testStation.id
            )
        )
        require(expectedEvent is Either.Right)

        val actualEvent = eventService.getEventByID(expectedEvent.b.id)

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testSaveRecurringEvent() {

        val createForm = EventPostForm(
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
        val expectedEvents = createForm.mapIndexed { index: Int, postForm: EventPostForm ->
            Event(firstId + index, postForm.startDateTime, postForm.endDateTime, testStation, testPartner, postForm.recurrenceRule)
        }

        assertEquals(expectedEvents, actualEvents.b)

    }


}