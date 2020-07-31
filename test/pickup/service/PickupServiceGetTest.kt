package pickup.service

import arrow.core.Either
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.CreatePickupForm
import ombruk.backend.pickup.form.GetPickupsForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.service.PickupService
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


class PickupServiceGetTest {
    companion object {
        lateinit var pickupService: PickupService
        lateinit var testStation: Station
        lateinit var testStation2: Station


        @BeforeClass
        @JvmStatic
        fun setup() {
            initDB()
            // Clear the database in order to get to a known state.
            // Note that order matter (db constraints)
            transaction {
                // Partners.deleteAll()

                Requests.deleteAll()
                Pickups.deleteAll()
                //Stations.deleteAll()

            }
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

            pickupService = PickupService
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

        // 1. Give me all the pickups for this station.
        var form = GetPickupsForm(null,null,testStation.id)

        var actualPickups = pickupService.getPickups(form)

        require(actualPickups is Either.Right)
        assertEquals(expectedPickups, actualPickups.b)

        // 2. Make sure that when we supply an invalid station we get an empty set back

        form = GetPickupsForm(null,null,testStation.id + 99999)
        actualPickups = pickupService.getPickups(form)
        // This should be empty
        require(actualPickups is Either.Right)

        assertEquals(listOf(), actualPickups.b )

        // 3. Let's see if we can supply invalid dates and get an empty set back.

        form = GetPickupsForm(start.plusDays(100),null,null)
        actualPickups = pickupService.getPickups(form)
        require(actualPickups is Either.Right)
        assertEquals( listOf(), actualPickups.b )

        // 4. Let's supply valid dates that would give us our one pickup back.
        form = GetPickupsForm(start.minusHours(1),end.plusHours(1),null)
        actualPickups = pickupService.getPickups(form)
        require(actualPickups is Either.Right)

        assertEquals(expectedPickups , actualPickups.b )


    }

}
