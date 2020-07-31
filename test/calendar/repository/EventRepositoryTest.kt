
/*package calendar.repository

import arrow.core.Either
import arrow.core.right
import io.mockk.every
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.event.EventGetForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.reporting.service.ReportService
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.AfterClass
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class EventRepositoryTestTest {
    companion object {
        lateinit var testPartner: Partner
        lateinit var testPartner2: Partner
        lateinit var testStation: Station
        lateinit var testStation2: Station

        @KtorExperimentalAPI
        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            mockkObject(EventRepository)
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
        fun cleanPartnersAndStationsFromDB() {
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
    fun testGetEventById() {
        val expectedEvent = Event(
            1,
            LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner
        )

        every { EventRepository.getEventByID(expectedEvent.id) } returns expectedEvent.right()


        val actualEvent = eventService.getEventByID(expectedEvent.id)
        require(actualEvent is Either.Right)

        assertEquals(expectedEvent, actualEvent.b)
    }

    @Test
    fun testGetAllEvents() {

        val expectedEvents = (0..10).map { id ->
            Event(
                id,
                LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
        }

        every { EventRepository.getEvents(null, null) } returns expectedEvents.right()

        val actualEvents = eventService.getEvents()
        require(actualEvents is Either.Right)
        assertEquals(expectedEvents, actualEvents.b)
    }

}
*/