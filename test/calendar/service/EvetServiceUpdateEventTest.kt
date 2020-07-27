package calendar.service

import arrow.core.Either
import ombruk.backend.calendar.database.Events
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.form.CreateEventForm
import ombruk.backend.calendar.form.EventUpdateForm
import ombruk.backend.calendar.model.Station
import ombruk.backend.calendar.service.EventService
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.shared.database.initDB
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals


class EvetServiceUpdateEventTest {
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
    fun testUpdateEvent() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)

        val initialEvent = eventService.saveEvent(
            CreateEventForm(
                start,
                end,
                testStation.id,
                testPartner.id
            )
        )
        require(initialEvent is Either.Right)

        val expectedEvent = initialEvent.b.copy(startDateTime = start.plusHours(1), endDateTime = end.plusHours(1))

        val updateForm = EventUpdateForm(
            initialEvent.b.id,
            start.plusHours(1),
            end.plusHours(1)
        )

        eventService.updateEvent(updateForm)

        val actualEvent = eventService.getEventByID(initialEvent.b.id)
        require(actualEvent is Either.Right)
        assertEquals(expectedEvent, actualEvent.b)
    }

}