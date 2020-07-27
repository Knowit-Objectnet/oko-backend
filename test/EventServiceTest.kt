import arrow.core.Either
import arrow.core.getOrHandle
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import ombruk.backend.database.*
import ombruk.backend.form.EventUpdateForm
import ombruk.backend.form.api.EventDeleteForm
import ombruk.backend.form.api.ValidationError
import ombruk.backend.model.*
import ombruk.backend.service.EventService
import ombruk.backend.utils.assertEventEqual
import ombruk.backend.utils.everyWeekDay
import ombruk.backend.utils.rangeTo
import ombruk.calendar.form.api.EventGetForm
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.*

class EventServiceTest {
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

            eventService = EventService()
        }

    }

    @After
    fun cleanEventsFromDB() {
        transaction {
            Events.deleteAll()
        }
    }

    @Test
    fun testGetSingularEventById() {
        val expectedEvent = transaction {
            EventRepository.insertEvent(
                Event(
                    0,
                    startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
                    endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
                    partner = testPartner,
                    station = testStation
                )
            )
        }.getOrHandle { throw IllegalStateException("Result was a left") }


        val actualEvent =
            eventService.getEventByID(expectedEvent.id).getOrHandle { throw IllegalStateException("Result was a left") }
        assertNotNull(actualEvent, "actualEvent should not be null here:(") {
            assertEquals(expectedEvent.id, it.id)
            assertEquals(expectedEvent.startDateTime, it.startDateTime)
            assertEquals(expectedEvent.endDateTime, it.endDateTime)
            assertEquals(expectedEvent.partner, it.partner)
            assertEquals(expectedEvent.station, it.station)
            assertNull(it.recurrenceRule)
        }
    }

    @Test
    fun testInsertSingleEvent() {
        val expectedEvent = Event(
            startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation
        )

        val actualEvent =
            eventService.saveEvent(expectedEvent).getOrHandle { throw IllegalStateException("Result was a left") }

        val actualRow = transaction {
            Events.select { Events.id eq actualEvent.id }.singleOrNull()
        }

        assertNotNull(actualRow, "The row should not be null after insertion") {
            assertEquals(expectedEvent.startDateTime, it[Events.startDateTime])
            assertEquals(expectedEvent.startDateTime, it[Events.startDateTime])
            assertEquals(expectedEvent.partner.id, it[Events.partnerID])
            assertEquals(expectedEvent.station.id, it[Events.stationID])
            assertNull(it.getOrNull(Events.recurrenceRuleID))
        }

    }

    @Test
    fun testGetRecurringEventById() {

        val recurrenceRuleId = transaction {
            RecurrenceRules.insertAndGetId {
                it[interval] = 1
                it[count] = 2
                it[days] = "MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY"
            }.value
        }

        val expectedEvent = transaction {
            EventRepository.insertEvent(
                Event(
                    startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
                    endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
                    partner = testPartner,
                    station = testStation,
                    recurrenceRule = RecurrenceRule(
                        recurrenceRuleId,
                        count = 2,
                        days = everyWeekDay()
                    )
                )
            )
        }.getOrHandle { throw IllegalStateException("Result is left") }

        assertNotNull(
            eventService.getEventByID(expectedEvent.id).getOrHandle { throw IllegalStateException("Result is left") }) {
            assertEventEqual(expectedEvent, it)
        }
    }

    @Test
    fun testSaveCountRecurrenceRule() {
        val expectedCount = 37
        val event = Event(
            startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation,
            recurrenceRule = RecurrenceRule(count = expectedCount, days = listOf(DayOfWeek.MONDAY))
        )
        eventService.saveEvent(event)

        val actualCount = transaction {
            Events.selectAll().count().toInt()
        }
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun testSaveDailyUntilOverWeekend() {

        val event = Event(
            startDateTime = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation,
            // 08-02 is a Sunday
            recurrenceRule = RecurrenceRule(
                until = LocalDateTime.parse("2020-08-03T16:30:00"),
                days = everyWeekDay()
            )
        )
        eventService.saveEvent(event)

        val expectedStartDateTimes = event.map { it.startDateTime }

        val actualStartDateTimes = transaction {
            Events.selectAll().orderBy(Events.startDateTime to SortOrder.ASC).map { it[Events.startDateTime] }
        }
        println(expectedStartDateTimes)
        println(actualStartDateTimes)
        assertEquals(expectedStartDateTimes, actualStartDateTimes)
    }

    @Test
    fun testSaveDailyUntilWeekend() {

        val event = Event(
            0,
            LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            // 08-02 is a Sunday
            RecurrenceRule(
                until = LocalDateTime.parse("2020-08-02T16:30:00"),
                days = everyWeekDay()
            )
        )
        eventService.saveEvent(event)

        val expectedStartDateTimes = event.map { it.startDateTime }

        val actualStartDateTimes = transaction {
            Events.selectAll().orderBy(Events.startDateTime to SortOrder.ASC).map { it[Events.startDateTime] }
        }
        println(expectedStartDateTimes)
        println(actualStartDateTimes)
        assertEquals(expectedStartDateTimes, actualStartDateTimes)
    }

    @Test
    fun testSaveDailyUntilEnd() {

        val event = Event(
            startDateTime = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation,
            // 08-02 is a Sunday
            recurrenceRule = RecurrenceRule(
                until = LocalDateTime.parse("2020-07-31T16:29:59"),
                days = everyWeekDay()
            )
        )
        eventService.saveEvent(event)

        val expectedStartDateTimes = event.map { it.startDateTime }

        val actualStartDateTimes = transaction {
            Events.selectAll().orderBy(Events.startDateTime to SortOrder.ASC).map { it[Events.startDateTime] }
        }
        println(expectedStartDateTimes)
        println(actualStartDateTimes)
        assertEquals(expectedStartDateTimes, actualStartDateTimes)
    }

    @Test
    fun testSaveWeeklyCountRecurrenceRule() {
        val expectedCount = 42

        val event = Event(
            startDateTime = LocalDateTime.parse("2020-07-06T15:48:06", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-06T16:48:06", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation,
            recurrenceRule = RecurrenceRule(count = expectedCount)
        )
        eventService.saveEvent(event)
        val actualCount = transaction {
            Events.selectAll().count().toInt()
        }
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun testSaveWeeklyInterval() {

        val event = Event(
            startDateTime = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            endDateTime = LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            partner = testPartner,
            station = testStation,
            recurrenceRule = RecurrenceRule(count = 5)
        )
        eventService.saveEvent(event)

        val expectedStartDateTimes = event.map { it.startDateTime }

        val actualStartDateTimes = transaction {
            Events.selectAll().orderBy(Events.startDateTime to SortOrder.ASC).map { it[Events.startDateTime] }
        }
        println(expectedStartDateTimes)
        println(actualStartDateTimes)
        assertEquals(expectedStartDateTimes, actualStartDateTimes)
    }

    @Test
    fun testGetAllEvents() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        //Create and save expected events
        val expectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner)
                )
            }.getOrHandle { throw IllegalStateException("Result is left") }
        }


        val actualEvents = eventService.getEvents(
            EventGetForm.create(Parameters.Empty).getOrHandle { throw IllegalStateException("Result is left") })
            .getOrHandle { throw IllegalStateException("Result is left") }

        expectedEvents.forEach { expectedEvent ->
            val actualEvent = actualEvents.find { it.id == expectedEvent.id }
            assertNotNull(actualEvent) {
                assertEventEqual(expectedEvent, it)
            }
        }
    }

    @Test
    fun testGetEventsByStationID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        //Create and save expected events
        val expectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner)
                )
            }.getOrHandle { throw IllegalStateException("Result is left") }
        }

        //Create and save unexpected events
        val unexpectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation2, testPartner)
                )
            }.getOrHandle { throw IllegalStateException("Result is left") }
        }
        val params = ParametersBuilder()
        params.append("station-id", testStation.id.toString())
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw IllegalStateException("Result is left") })
            .getOrHandle { throw IllegalStateException("Result is left") }

        // Check that the expected values are in actualEvents
        expectedEvents.forEach { expectedEvent ->
            val actualEvent = actualEvents.find { it.id == expectedEvent.id }
            assertNotNull(actualEvent) {
                assertEventEqual(expectedEvent, it)
            }
        }

        unexpectedEvents.forEach { unexpectedEvent ->
            assertNull(actualEvents.find { it.id == unexpectedEvent.id })
        }
    }

    @Test
    fun testGetEventsByPartnerID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        //Create and save expected events
        val expectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner)
                )
            }.getOrHandle { throw IllegalStateException("Result is left") }
        }

        //Create and save unexpected events
        val unexpectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner2)
                )
            }.getOrHandle { throw IllegalStateException("Result is left") }
        }

        val params = ParametersBuilder()
        params.append("partner-id", testPartner.id.toString())
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        // Check that the expected values are in actualEvents
        expectedEvents.forEach { expectedEvent ->
            val actualEvent = actualEvents.find { it.id == expectedEvent.id }
            assertNotNull(actualEvent) {
                assertEventEqual(expectedEvent, it)
            }
        }

        unexpectedEvents.forEach { unexpectedEvent ->
            assertNull(actualEvents.find { it.id == unexpectedEvent.id })
        }
    }

    @Test
    fun testGetEventsByTypeSingular() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        //Create and save expected events
        val expectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }


        val rRule = transaction {
            //Not important for the rule to match the events atm. May need to change in the future
            RecurrenceRules.insertRecurrenceRule(
                RecurrenceRule(count = 7)
            )
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }


        //Create and save unexpected events
        val unexpectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner2, rRule)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        val actualEvents = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }, eventType = EventType.SINGLE
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        // Check that the expected values are in actualEvents
        expectedEvents.forEach { expectedEvent ->
            val actualEvent = actualEvents.find { it.id == expectedEvent.id }
            assertNotNull(actualEvent) {
                assertEventEqual(expectedEvent, it)
            }
        }

        unexpectedEvents.forEach { unexpectedEvent ->
            assertNull(actualEvents.find { it.id == unexpectedEvent.id })
        }
    }

    @Test
    fun testGetEventsByTypeRecurring() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        val rRule = transaction {
            //Not important for the rule to match the events atm. May need to change in the future
            RecurrenceRules.insertRecurrenceRule(
                RecurrenceRule(count = 7)
            )
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        //Create and save expected events
        val expectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner, rRule)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        //Create and save unexpected events
        val unexpectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner2)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        val actualEvents = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") },
            eventType = EventType.RECURRING
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        // Check that the expected values are in actualEvents
        expectedEvents.forEach { expectedEvent ->
            val actualEvent = actualEvents.find { it.id == expectedEvent.id }
            assertNotNull(actualEvent) {
                assertEventEqual(expectedEvent, it)
            }
        }

        unexpectedEvents.forEach { unexpectedEvent ->
            assertNull(actualEvents.find { it.id == unexpectedEvent.id })
        }
    }

    @Test
    fun testGetEventsByPartnerAndStationID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = listOf(start..end).flatten()

        //Create and save expected events
        val expectedEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        //Create and save unexpected events
        val unexpectedPartnerEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation, testPartner2)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        //Create and save unexpected events
        val unexpectedStationEvents = dateRange.map { startDate ->
            transaction {
                EventRepository.insertEvent(
                    Event(0, startDate, startDate.plusHours(1), testStation2, testPartner)
                )
            }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        val parameters = ParametersBuilder()
        parameters.append("station-id", testStation.id.toString())
        parameters.append("partner-id", testPartner.id.toString())
        val actualEvents = eventService.getEvents(
            EventGetForm.create(
                parameters.build()
            ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        // Check that the expected values are in actualEvents
        expectedEvents.forEach { expectedEvent ->
            val actualEvent = actualEvents.find { it.id == expectedEvent.id }
            assertNotNull(actualEvent) {
                assertEventEqual(expectedEvent, it)
            }
        }

        (unexpectedPartnerEvents + unexpectedStationEvents).forEach { unexpectedEvent ->
            assertNull(actualEvents.find { it.id == unexpectedEvent.id })
        }
    }

    @Test
    fun testUpdateEvent() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)

        val initialEvent = Event(0, start, start.plusHours(1), testStation, testPartner)
        val event = transaction {
            EventRepository.insertEvent(initialEvent)
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val expectedEvent = Event(event.id, end, end.plusHours(1), testStation, testPartner)

        val updateEvent = EventUpdateForm(
            event.id,
            end,
            end.plusHours(1)
        )
        eventService.updateEvent(updateEvent).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val actualEvent =
            eventService.getEventByID(event.id).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(expectedEvent, actualEvent)
    }

    @Test
    fun testUpdateMissingEvent() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)

        val initialEvent = Event(0, start, start.plusHours(1), testStation, testPartner)

        transaction {
            EventRepository.insertEvent(initialEvent)
        }

        val updateEvent = EventUpdateForm(
            0,
            end,
            end.plusHours(1)
        )
        val actual = eventService.updateEvent(updateEvent)
        assertTrue(actual.isLeft())
    }

    @Test
    fun testUpdateMissingStartDate() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)

        val initialEvent = Event(0, start, start.plusHours(1), testStation, testPartner)

        val event = transaction {
            EventRepository.insertEvent(initialEvent)
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val expectedEvent = Event(event.id, start, end.plusHours(1), testStation, testPartner)

        transaction {
            EventRepository.insertEvent(initialEvent)
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val updateEvent = EventUpdateForm(
            event.id,
            endDateTime = end.plusHours(1)
        )
        eventService.updateEvent(updateEvent).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val actualEvent =
            eventService.getEventByID(event.id).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(expectedEvent, actualEvent)
    }

    @Test
    fun testUpdateMissingEndDate() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)

        val initialEvent = Event(0, start, start.plusHours(1), testStation, testPartner)

        val event = transaction {
            EventRepository.insertEvent(initialEvent)
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val expectedEvent = Event(event.id, start.minusHours(1), start.plusHours(1), testStation, testPartner)

        transaction {
            EventRepository.insertEvent(initialEvent)
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val updateEvent = EventUpdateForm(
            event.id,
            startDateTime = start.minusHours(1)
        )
        eventService.updateEvent(updateEvent).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val actualEvent =
            eventService.getEventByID(event.id).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(expectedEvent, actualEvent)
    }

    @Test
    fun testUpdateStartDateAfterEndDate() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)

        val initialEvent = Event(0, start, start.plusHours(1), testStation, testPartner)

        val event = transaction {
            EventRepository.insertEvent(initialEvent)
        }.getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val updateEvent = EventUpdateForm(
            event.id,
            start.plusHours(1),
            start.minusHours(1)
        )
        assertTrue(eventService.updateEvent(updateEvent).isLeft())
    }

    @Test
    fun testDeleteEventID() {

        val event = transaction {
            EventRepository.insertEvent(
                Event(
                    0,
                    LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                    LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                    testStation,
                    testPartner
                )
            ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        }

        val expectedEvent = Event(
            event.id,
            LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner
        )

        assertEventEqual(
            expectedEvent,
            eventService.getEventByID(event.id).getOrHandle { throw java.lang.IllegalStateException("Result is left") })

        val paramsBuilder = ParametersBuilder()
        paramsBuilder.append("event-id", event.id.toString())
        val params = paramsBuilder.build()
        val deleteForm = EventDeleteForm.create(params).getOrHandle { throw IllegalStateException("Result is left") }

        assertEquals(listOf(expectedEvent),
            eventService.deleteEvent(deleteForm).getOrHandle { throw IllegalStateException("Result is left") })

        assertFalse(eventService.deleteEvent(deleteForm).isRight())
    }

    @Test
    fun testDeleteRecurrenceRuleID() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEquals(
            10,
            eventService.getEvents(
                EventGetForm.create(Parameters.Empty)
                    .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }.size
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })

        val result = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
        when (result) {
            is Either.Left -> fail()
            is Either.Right -> assertTrue(result.b.isEmpty())
        }
    }

    @Test
    fun testDeleteRecurrenceRuleIDMissingID() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", "0")
        val result = eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
        assertTrue { result.isLeft() }
    }

    @Test
    fun testDeleteEventIDMissingID() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )
        val params = ParametersBuilder()
        params.append("event-id", "0")
        val result = eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
        assertTrue { result.isLeft() }
    }

    @Test
    fun testDeleteInRange() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        var params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        val events = eventService.getEvents(
            EventGetForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val expectedFirstEvent = Event(
            events[0].id,
            LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
        val expectedLastEvent = Event(
            events[events.size - 1].id,
            LocalDateTime.parse("2020-08-26T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-08-26T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )

        params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("from-date", "2020-07-29T15:30:00")
        params.append("to-date", "2020-08-24T16:30:00")
        val result = eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertTrue { result.isNotEmpty() }
        val remainingEvents = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(remainingEvents[0], expectedFirstEvent)
        assertEventEqual(remainingEvents[1], expectedLastEvent)
    }

    @Test
    fun testDeleteInRangeEndBeforeStart() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", "1")
        params.append("from-date", "2020-07-29T15:30:00")
        params.append("to-date", "2020-08-24T16:30:00")
        val result = eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
        assertTrue(result.isLeft())
        assertEquals(
            10,
            eventService.getEvents(
                EventGetForm.create(Parameters.Empty)
                    .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }.size
        )
    }

    @Test
    fun testDeleteFromDate() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("from-date", "2020-08-12T15:30:00")
        val result = eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
        assertTrue(result.isRight())
        assertEquals(
            5,
            eventService.getEvents(
                EventGetForm.create(Parameters.Empty)
                    .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }.size
        )
    }

    @Test
    fun testDeleteToDate() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        var params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        val events = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val expectedFirstEvent = Event(
            events[5].id,
            LocalDateTime.parse("2020-08-12T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-08-12T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )

        params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("to-date", "2020-08-12T15:29:59")
        eventService.deleteEvent(
            EventDeleteForm.create(params.build())
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEquals(
            5,
            eventService.getEvents(
                EventGetForm.create(Parameters.Empty)
                    .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }.size
        )
        assertEventEqual(
            expectedFirstEvent,
            eventService.getEventByID(events[0].id + 5)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
    }

    @Test
    fun testDeleteBothIDsFails() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )

        val params = ParametersBuilder()
        params.append("recurrence-rule-id", "1")
        params.append("event-id", "1")
        params.append("to-date", "2020-08-12T15:29:59")

        val result = EventDeleteForm.create(params.build())
        assertTrue(result.isLeft())
        when (result) {
            is Either.Left -> assertTrue(result.a is ValidationError.InvalidStateError)
            is Either.Right -> fail()
        }
        assertEquals(
            10,
            eventService.getEvents(
                EventGetForm.create(Parameters.Empty)
                    .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }.size
        )
    }

    @Test
    fun testDeleteWithoutIDs() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val params = ParametersBuilder()
        params.append("to-date", "2020-08-12T15:29:59")
        when (val result = EventDeleteForm.create(params.build())) {
            is Either.Left -> {
                assertTrue(result.a is ValidationError.InvalidStateError)
            }
            is Either.Right -> fail()
        }
        assertEquals(
            10,
            eventService.getEvents(
                EventGetForm.create(Parameters.Empty)
                    .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") }.size
        )
    }

    @Test
    fun testGetEventsFromDate() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val events = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val expectedFirstEvent = Event(
            events[2].id,
            LocalDateTime.parse("2020-08-03T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-08-03T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("from-date", "2020-08-01T15:30:00")
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(expectedFirstEvent, actualEvents[0])
        assertEquals(8, actualEvents.size)
    }

    @Test
    fun testGetEventsToDate() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val events = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val expectedLastEvent = Event(
            events[2].id,
            LocalDateTime.parse("2020-08-03T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-08-03T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("to-date", "2020-08-05T15:30:00")
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(expectedLastEvent, actualEvents[actualEvents.size - 1])
        assertEquals(3, actualEvents.size)
    }

    @Test
    fun testGetDatesInRange() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val events = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val expectedFirstEvent = Event(
            events[4].id,
            LocalDateTime.parse("2020-08-10T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-08-10T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
        val expectedLastEvent = Event(
            events[7].id,
            LocalDateTime.parse("2020-08-19T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-08-19T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("from-date", "2020-08-05T15:31:00")
        params.append("to-date", "2020-08-23T16:30:00")
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEventEqual(expectedFirstEvent, actualEvents[0])
        assertEventEqual(expectedLastEvent, actualEvents[actualEvents.size - 1])
        assertEquals(4, actualEvents.size)
    }

    @Test
    fun testGetDatesInvalidRange() {
        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        params.append("from-date", "2020-08-05T16:31:00")
        params.append("to-date", "2020-08-05T16:30:00")
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        )
        when(actualEvents){
            is Either.Left -> fail()
            is Either.Right -> assertTrue(actualEvents.b.isEmpty())
        }
    }

    @Test
    fun getEventsByRecurrenceRuleID() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }

        val event = eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val allEvents = eventService.getEvents(
            EventGetForm.create(Parameters.Empty)
                .getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val firstEvent = Event(
            allEvents[1].id,
            LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation,
            testPartner,
            RecurrenceRule(event.recurrenceRule!!.id, count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        )
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", event.recurrenceRule!!.id.toString())
        val actualEvents = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") })
            .getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        assertEquals(10, actualEvents.size)
        assertEventEqual(firstEvent, actualEvents[0])
    }

    @Test
    fun getEventsByRecurrenceRuleIDMissingID() {
        eventService.saveEvent(
            Event(
                0,
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation,
                testPartner
            )
        ).getOrHandle { throw java.lang.IllegalStateException("Result is left") }
        val params = ParametersBuilder()
        params.append("recurrence-rule-id", "0")
        val result = eventService.getEvents(
            EventGetForm.create(params.build()).getOrHandle { throw java.lang.IllegalStateException("Result is left") })
        when (result) {
            is Either.Left -> {
                fail()
            }
            is Either.Right -> assertTrue(result.b.isEmpty())
        }
    }

}

