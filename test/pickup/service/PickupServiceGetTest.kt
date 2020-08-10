package pickup.service

import arrow.core.Either
import calendar.service.EventServiceDeleteTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.pickup.PickupGetByIdForm
import ombruk.backend.pickup.form.pickup.PickupGetForm
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.model.Pickup
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
import ombruk.backend.shared.utils.rangeTo
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PickupServiceGetTest {
        lateinit var testStation: Station
        lateinit var testStation2: Station


        init {
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
                var opensAt = LocalTime.parse("09:00:00Z", DateTimeFormatter.ISO_TIME)!!
                var closesAt = LocalTime.parse("21:00:00Z", DateTimeFormatter.ISO_TIME)!!
                var hours = mapOf(
                    Pair(DayOfWeek.MONDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.TUESDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.WEDNESDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.THURSDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.FRIDAY, listOf(opensAt, closesAt))
                )
                val json = Json(JsonConfiguration.Stable)

                val testStationId = Stations.insertAndGetId {
                    it[name] = "Test Station 1"
                    it[Stations.hours] =
                        json.toJson(MapSerializer(DayOfWeekSerializer, ListSerializer(LocalTimeSerializer)), hours)
                            .toString()
                }.value

                testStation = Station(
                    testStationId,
                    "Test Station 1",
                    hours
                )

                opensAt = LocalTime.parse("08:00:00", DateTimeFormatter.ISO_TIME)
                closesAt = LocalTime.parse("20:00:00", DateTimeFormatter.ISO_TIME)
                hours = mapOf(
                    Pair(DayOfWeek.MONDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.TUESDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.WEDNESDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.THURSDAY, listOf(opensAt, closesAt)),
                    Pair(DayOfWeek.FRIDAY, listOf(opensAt, closesAt))
                )

                val testStationId2 = Stations.insertAndGetId {
                    it[name] = "Test Station 2"
                    it[Stations.hours] = json.toJson(
                        MapSerializer(
                            DayOfWeekSerializer, ListSerializer(
                                LocalTimeSerializer
                            )
                        ), hours)
                        .toString()
                }.value
                testStation2 = Station(
                    testStationId2,
                    "Test Station 2",
                    hours
                )

            }
        }

        @AfterAll
        fun cleanPartnersAndStationsFromDB(){
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
    fun testGetPickupById() {

        val createForm = PickupPostForm(
            LocalDateTime.parse("2020-07-20T15:45:00", DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse("2020-07-20T16:45:00", DateTimeFormatter.ISO_DATE_TIME),
            null,
            testStation.id
        )
        val expectedPickup = PickupService.savePickup(createForm)

        // Require a right so we can get to the ID below.
        require(expectedPickup is Either.Right)
        val actualPickup = PickupService.getPickupById(PickupGetByIdForm(expectedPickup.b.id))

        assertEquals(expectedPickup, actualPickup)
    }


    @Test
    fun testGetAllPickups() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val dateRange = start..end


        // Create a bunch of pickups in the date range, (see utils for the progression)
        // This will return a list of Eithers.
        val expectedPickups = dateRange.map { startDate ->
            PickupService.savePickup(
                PickupPostForm(
                    startDate,
                    startDate.plusHours(1),
                    null,
                    testStation.id
                )
            ) // So we map through it and pick out the Pickups and return it
        }.map {
            require(it is Either.Right)
            it.b
        } // So expectedPickups is now a list of Pickups

        // 1. Give me all the pickups for this station.
        var form = PickupGetForm(null, null, testStation.id)

        var actualPickups = PickupService.getPickups(form)

        require(actualPickups is Either.Right)

        assertEquals(expectedPickups, actualPickups.b)

        // 2. Make sure that when we supply an invalid station we get an empty set back

        form = PickupGetForm(null, null, testStation.id + 99999)
        actualPickups = PickupService.getPickups(form)
        // This should be empty
        require(actualPickups is Either.Right)

        assertEquals(listOf<Pickup>(), actualPickups.b )

        // 3. Let's see if we can supply invalid dates and get an empty set back.

        form = PickupGetForm(start.plusDays(100), null, null)
        actualPickups = PickupService.getPickups(form)
        require(actualPickups is Either.Right)
        assertEquals( listOf<Pickup>(), actualPickups.b )

        // 4. Let's supply valid dates that would give us our one pickup back.
        form = PickupGetForm(
            start.minusHours(1),
            end.plusHours(1),
            null
        )
        actualPickups = PickupService.getPickups(form)
        require(actualPickups is Either.Right)

        assertEquals(expectedPickups , actualPickups.b )


    }

}
