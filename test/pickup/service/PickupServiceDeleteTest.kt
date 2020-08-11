package pickup.service

import arrow.core.Either
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.pickup.PickupDeleteForm
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.shared.database.initDB
import ombruk.backend.shared.model.serializer.DayOfWeekSerializer
import ombruk.backend.shared.model.serializer.LocalTimeSerializer
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
class PickupServiceDeleteTest {
    lateinit var testStation: Station
    lateinit var testStation2: Station


    init {
        initDB()
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
                    ), hours
                )
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
