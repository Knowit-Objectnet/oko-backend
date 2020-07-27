package pickup.service

import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.pickup.database.Pickups
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


class PickupServiceGetTest {
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
    fun testGetPickupById() {

        val createForm = CreatePickupForm(
            LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME),
            testStation.id
        )
        val expectedPickup = pickupService.savePickup(createForm)


        val actualPickup = pickupService.getPickupById(expectedPickup.id)

        assertEquals(expectedPickup, actualPickup)
    }


    @Test
    fun testGetAllPickups() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end

        val expectedPickups = dateRange.map { startDate ->
            pickupService.savePickup(
                CreatePickupForm(startDate, startDate.plusHours(1), testStation.id)
            )
        }

        val actualPickups = pickupService.getPickups(null)

        assertEquals(expectedPickups, actualPickups)
    }
}
