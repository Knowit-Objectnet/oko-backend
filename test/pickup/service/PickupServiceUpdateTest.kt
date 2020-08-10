package pickup.service

import arrow.core.Either
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.form.pickup.PickupGetByIdForm
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.form.pickup.PickupUpdateForm
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.shared.database.initDB
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PickupServiceUpdateTest {

    lateinit var testStation: Station
    lateinit var testStation2: Station


    init {
        initDB()
        transaction {
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
            Pickups.deleteAll()

        }
    }

    @Test
    fun testUpdatePickup() {
        val startTime = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val endTime = startTime.plusHours(1)

        val initialPickup = PickupService.savePickup(
            PickupPostForm(startTime, endTime, null, testStation.id)
        )
        require(initialPickup is Either.Right)


        // Update the pickup to last one hour longer.
        val expectedPickup = initialPickup.b.copy(endDateTime = endTime.plusHours(1))

        val form = PickupUpdateForm(
            expectedPickup.id,
            expectedPickup.startDateTime,
            expectedPickup.endDateTime
        )
        PickupService.updatePickup(form)

        val actualPickup = PickupService.getPickupById(PickupGetByIdForm(initialPickup.b.id))
        require(actualPickup is Either.Right)

        assertEquals(expectedPickup, actualPickup.b)
    }

}
