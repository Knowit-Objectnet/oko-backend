package pickup.service

import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.service.PickupService
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


class PickupServiceUpdateTest {
    companion object {
        lateinit var pickupService: PickupService
        lateinit var testStation: Station
        lateinit var testStation2: Station


        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            transaction {
                val testStationId = Stations.insertAndGetId {
                    it[name] = "Test Station 1"
                }.value

                testStation = Station(testStationId, "Test Station 1")

                val testStationId2 = Stations.insertAndGetId {
                    it[name] = "Test Station 2"
                }.value

                testStation2 = Station(testStationId2, "Test Station 2")

            }

            pickupService = PickupService
        }
    }

    @After
    fun cleanEventsFromDB() {
        transaction {
            Pickups.deleteAll()

        }
    }

    @Test
    fun testUpdatePickup() {
        val initialPickup = pickupService.savePickup(
            CreatePickupForm(
                LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME),
                testStation.id
            )
        )

        val expectedPickup = initialPickup.copy(station =  testStation2)
        pickupService.updatePickup(expectedPickup)

        val actualPickup = pickupService.getPickupById(initialPickup.id)

        assertEquals(expectedPickup, actualPickup)
    }

}
