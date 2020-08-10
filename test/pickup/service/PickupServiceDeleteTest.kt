package pickup.service

import arrow.core.Either
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.pickup.PickupDeleteForm
import ombruk.backend.pickup.form.pickup.PickupPostForm
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
class PickupServiceDeleteTest {
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
            Requests.deleteAll()
        }
    }

    @Test
    fun testDeletePickupById() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = start.plusHours(1)

        // Create a pickup to be deleted.
        val pickupToDelete = PickupService.savePickup(
            PickupPostForm(
                startDateTime = start,
                endDateTime = end,
                stationId = testStation.id
            )
        )
        // This will barf if something goes wrong...

        require(pickupToDelete is Either.Right)

        // Another one, identical, not to be deleted.
        val pickupNotToDelete = PickupService.savePickup(
            PickupPostForm(start, end, null, testStation.id)
        )
        require(pickupNotToDelete is Either.Right)

        // Delete the first one. XXX: stationId makes no sense here:
        val deleteForm = PickupDeleteForm(pickupToDelete.b.id)
        PickupService.deletePickup(deleteForm)

        // Fetch what is left
        val pickupsLeftAfterDelete = PickupService.getPickups()

        require(pickupsLeftAfterDelete is Either.Right)
        // And compare.
        assertEquals(pickupNotToDelete.b, pickupsLeftAfterDelete.b.first())
    }

//    @Test
//    fun testDeletePickupByStation() {
//        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
//        val end = start.plusHours(1)
//
//        // Create a pickup (we don't need to save it
//        pickupService.savePickup(
//            PickupPostForm(
//                start,
//                start.plusHours(1),
//                testStation.id
//            )
//        )
//
//        val pickupsNotToDelete = pickupService.savePickup(
//            PickupPostForm(
//                start,
//                end,
//                testStation2.id
//            )
//        )
//
//        // Delete all pickups for testStation
//        pickupService.deletePickup(null, testStation.id)
//
//        val pickupsLeftAfterDelete = pickupService.getPickups()
//        require(pickupsNotToDelete is Either.Right)
//        require(pickupsLeftAfterDelete is Either.Right)
//        assertEquals(pickupsNotToDelete.b, pickupsLeftAfterDelete.b.first())
//    }

}
