package pickup.service

import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.utils.rangeTo
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals


class PickupServiceDeleteTest {
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
            Requests.deleteAll()
        }
    }

    @Test
    fun testDeletePickupById() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        val pickupTodelete = pickupService.savePickup(CreatePickupForm(start, start.plusHours(1), testStation.id))

        val pickupsNotToDelete = dateRange.map { startDate ->
            pickupService.savePickup(
                CreatePickupForm(startDate, startDate.plusHours(1), testStation.id)
            )
        }

        pickupService.deletePickup(pickupTodelete.id, null)

        val pickupsLeftAfterDelete = pickupService.getPickups()
        assertEquals(pickupsNotToDelete, pickupsLeftAfterDelete)
    }

    @Test
    fun testDeletePickupByStation() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        pickupService.savePickup(CreatePickupForm(start, start.plusHours(1), testStation.id))

        val pickupsNotToDelete = dateRange.map { startDate ->
            pickupService.savePickup(
                CreatePickupForm(startDate, startDate.plusHours(1), testStation2.id)
            )
        }

        pickupService.deletePickup(null, testStation.id)

        val pickupsLeftAfterDelete = pickupService.getPickups()
        assertEquals(pickupsNotToDelete, pickupsLeftAfterDelete)
    }

}
