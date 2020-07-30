package calendar.service

import arrow.core.Either
import calendar.form.EventGetForm
import ombruk.backend.calendar.database.EventRepository
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.EventPostForm
import ombruk.backend.calendar.model.Event
import ombruk.backend.calendar.model.EventType
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
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class EventServiceGetEventTest {
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
    fun testGetEventById() {
        val expectedEvent = transaction {
            EventRepository.insertEvent(
                EventPostForm(
                    startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
                    endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
                    partnerId = testPartner.id,
                    stationId = testStation.id
                )
            )
        }
        require(expectedEvent is Either.Right)

        val actualEvent = eventService.getEventByID(expectedEvent.b.id)
        require(actualEvent is Either.Right)

        assertEquals(expectedEvent.b, actualEvent.b)
    }

    @Test
    fun testGetAllEvents() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        //Create and save expected events
        val expectedEvents = dateRange.map {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id)
            )
        }.map {
            require(it is Either.Right)
            it.b
        }


        val actualEvents = eventService.getEvents()
        require(actualEvents is Either.Right)
        assertEquals(expectedEvents, actualEvents.b)
    }

    @Test
    fun testGetEventsByStationID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        //Create and save expected events
        val expectedEvents = dateRange.map {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id)
            )
        }.map {
            require(it is Either.Right)
            it.b
        }

        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation2.id, testPartner.id)
            )
        }

        val getForm = EventGetForm(stationID = testStation.id)

        val actualEvents = eventService.getEvents(getForm)
        require(actualEvents is Either.Right)

        assertEquals(expectedEvents, actualEvents.b)
    }

    @Test
    fun testGetEventsByPartnerID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        //Create and save expected events
        val expectedEvents = dateRange.map {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id)
            )
        }.map {
            require(it is Either.Right)
            it.b
        }

        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner2.id)
            )
        }


        val getForm = EventGetForm(partnerID = testPartner.id)

        val actualEvents = eventService.getEvents(getForm)
        require(actualEvents is Either.Right)

        assertEquals(expectedEvents, actualEvents.b)
    }


    @Test
    fun testGetEventsByPartnerAndStationID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        //Create and save expected events
        val expectedEvents = dateRange.map {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id)
            )
        }.map {
            require(it is Either.Right)
            it.b
        }

        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner2.id)
            )
        }

        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation2.id, testPartner.id)
            )
        }

        val getForm = EventGetForm(stationID = testStation.id, partnerID = testPartner.id)

        val actualEvents = eventService.getEvents(getForm)
        require(actualEvents is Either.Right)

        assertEquals(expectedEvents, actualEvents.b)
    }

    @Test
    fun testGetDatesInRange() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        //Create and save expected events
        val expectedEvents = dateRange.map {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(5), testStation.id, testPartner.id)
            )
        }.map {
            require(it is Either.Right)
            it.b
        }

        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it.plusYears(5), it.plusYears(5).plusHours(1), testStation.id, testPartner2.id)
            )
        }

        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it.minusYears(5), it.minusYears(5).plusHours(1), testStation2.id, testPartner.id)
            )
        }

        val getForm = EventGetForm(
            toDate = LocalDateTime.parse("2020-08-15T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            fromDate = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        )

        val actualEvents = eventService.getEvents(getForm)
        require(actualEvents is Either.Right)

        assertEquals(expectedEvents, actualEvents.b)
    }

    @Test
    fun getEventsByRecurrenceRuleID() {
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

        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id, RecurrenceRule(count = 7))
            )
        }

        val getForm = EventGetForm(recurrenceRuleID = recurrenceRuleId.b)

        val actualEvents = eventService.getEvents(getForm)
        require(actualEvents is Either.Right)

        val firstId = actualEvents.b.first().id
        val expectedEvents = createForm.mapIndexed { index: Int, postForm: EventPostForm ->
            Event(
                firstId + index,
                postForm.startDateTime,
                postForm.endDateTime,
                testStation,
                testPartner,
                postForm.recurrenceRule
            )
        }

        assertEquals(expectedEvents, actualEvents.b)
    }

    @Test
    fun testGetEventsByTypeSingular() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end


        //Create and save expected events
        val expectedEvents = dateRange.map {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id)
            )
        }.map {
            require(it is Either.Right)
            it.b
        }

        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id, RecurrenceRule(count = 7))
            )
        }

        val actualEvents = eventService.getEvents(eventType = EventType.SINGLE)
        require(actualEvents is Either.Right)

        assertEquals(expectedEvents, actualEvents.b)
    }

    @Test
    fun testGetEventsByTypeRecurring() {
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

        eventService.saveEvent(createForm)


        //Save unexpected events
        dateRange.forEach {
            eventService.saveEvent(
                EventPostForm(it, it.plusHours(1), testStation.id, testPartner.id)
            )
        }

        val actualEvents = eventService.getEvents(eventType = EventType.RECURRING)
        require(actualEvents is Either.Right)

        val firstId = actualEvents.b.first().id

        val expectedEvents = createForm.mapIndexed { index: Int, postForm: EventPostForm ->
            Event(
                firstId + index,
                postForm.startDateTime,
                postForm.endDateTime,
                testStation,
                testPartner,
                postForm.recurrenceRule
            )
        }

        assertEquals(expectedEvents, actualEvents.b)
    }


}