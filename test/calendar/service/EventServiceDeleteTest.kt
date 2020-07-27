package calendar.service

import arrow.core.Either
import io.ktor.http.ParametersBuilder
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.form.EventDeleteForm
import ombruk.backend.calendar.model.RecurrenceRule
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.utils.rangeTo
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

class EventServiceDeleteTest {
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
    fun testDeleteEventByid() {

        val eventToDelete = eventService.saveEvent(
            CreateEventForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        val eventNotToDelete = eventService.saveEvent(
            CreateEventForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        require(eventToDelete is Either.Right)
        require(eventNotToDelete is Either.Right)

        val paramsBuilder = ParametersBuilder()
        paramsBuilder.append("event-id", eventToDelete.b.id.toString())
        val params = paramsBuilder.build()
        val deleteForm = EventDeleteForm.create(params)
        require(deleteForm is Either.Right)

        assert(eventService.deleteEvent(deleteForm.b) is Either.Right)

        val eventLeftAfterDelete = eventService.getEvents()
        require(eventLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete.b, eventLeftAfterDelete.b.first())
    }


    @Test
    fun testDeleteEventByRecurrenceRuleId() {

        val eventToDelete = eventService.saveEvent(
            CreateEventForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id,
                RecurrenceRule(count = 5, days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
            )
        )

        val eventNotToDelete = eventService.saveEvent(
            CreateEventForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-07-27T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id,
                testPartner.id
            )
        )

        require(eventToDelete is Either.Right)
        require(eventNotToDelete is Either.Right)

        val params = ParametersBuilder()
        params.append("recurrence-rule-id", eventToDelete.b.recurrenceRule!!.id.toString())
        val deleteForm = EventDeleteForm.create(params.build())
        require(deleteForm is Either.Right)

        assert(eventService.deleteEvent(deleteForm.b) is Either.Right)

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
        val createForm = CreateEventForm(
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
                    CreateEventForm(it, it.plusHours(1), testStation.id,testPartner.id)
                )
            }.map{ require(it is Either.Right); it.b}

        val params = ParametersBuilder()
        params.append("from-date", "2020-07-27T15:30:00Z")
        params.append("to-date", "2021-07-27T15:30:00Z")
        params.append("recurrence-rule-id", recurrenceRuleId.b.toString())
        val deleteForm = EventDeleteForm.create(params.build())
        if(deleteForm is Either.Left) println(deleteForm.a)
        require(deleteForm is Either.Right)

        assert(eventService.deleteEvent(deleteForm.b) is Either.Right)

        val eventsLeftAfterDelete = eventService.getEvents()
        require(eventsLeftAfterDelete is Either.Right)

        assertEquals(eventNotToDelete, eventsLeftAfterDelete.b)
    }
}