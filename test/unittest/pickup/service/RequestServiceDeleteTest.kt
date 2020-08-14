package pickup.service

import arrow.core.Either
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import ombruk.backend.calendar.database.Stations
import ombruk.backend.calendar.model.Station
import ombruk.backend.partner.database.Partners
import ombruk.backend.partner.model.Partner
import ombruk.backend.pickup.database.Pickups
import ombruk.backend.pickup.database.Requests
import ombruk.backend.pickup.form.pickup.PickupPostForm
import ombruk.backend.pickup.form.request.RequestDeleteForm
import ombruk.backend.pickup.form.request.RequestGetForm
import ombruk.backend.pickup.form.request.RequestPostForm
import ombruk.backend.pickup.model.Request
import ombruk.backend.pickup.service.PickupService
import ombruk.backend.pickup.service.RequestService
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
class RequestServiceTest {
    lateinit var testPartner: Partner
    lateinit var testPartner2: Partner
    lateinit var testStation: Station
    lateinit var testStation2: Station

    init {
        initDB()
        transaction {
            val testPartnerId = Partners.insertAndGetId {
                it[name] = "TestPartner 1"
                it[description] = "Description of TestPartner 1"
                it[phone] = "+47 2381931"
                it[email] = "example@gmail.com"
            }.value

            testPartner =
                Partner(
                    testPartnerId,
                    "TestPartner 1",
                    "Description of TestPartner 1",
                    "+47 2381931",
                    "example@gmail.com"
                )

            val testPartnerId2 = Partners.insertAndGetId {
                it[name] = "TestPartner 2"
                it[description] = "Description of TestPartner 2"
                it[phone] = "911"
                it[email] = "example@gmail.com"
            }.value

            testPartner2 = Partner(
                testPartnerId2,
                "TestPartner 2",
                "Description of TestPartner 2",
                "911",
                "example@gmail.com"
            )

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
            Requests.deleteAll()
            Pickups.deleteAll()
        }
    }

    @Test
    fun testAddPartnersToPickup() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup = PickupService.savePickup(
            PickupPostForm(
                start,
                end,
                null,
                testStation.id
            )
        )
        require(pickup is Either.Right)
        val expectedRequest = Request(pickup.b, testPartner)
        val request = RequestService.saveRequest(RequestPostForm(pickup.b.id, testPartner.id))
        require(request is Either.Right)
        println(request.b)

        val actualRequests = RequestService.getRequests(RequestGetForm())
        require(actualRequests is Either.Right)
        assertEquals(expectedRequest, actualRequests.b.first())
    }

    @Test
    fun testDeleteRequestByPickupID() {
        val start = LocalDateTime.parse("2020-07-27T15:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val end = LocalDateTime.parse("2020-08-14T16:30:00", DateTimeFormatter.ISO_DATE_TIME)
        val pickup1 = PickupService.savePickup(
            PickupPostForm(
                start,
                end,
                null,
                testStation.id
            )
        )
        val pickup2 = PickupService.savePickup(
            PickupPostForm(
                start,
                end,
                null,
                testStation.id
            )
        )
        require(pickup1 is Either.Right)
        require(pickup2 is Either.Right)

        val requestToDelete = Request(pickup1.b, testPartner)
        val requestNotToDelete = Request(pickup2.b, testPartner2)

        RequestService.saveRequest(RequestPostForm(requestToDelete.pickup.id, requestToDelete.partner.id))
        RequestService.saveRequest(RequestPostForm(requestNotToDelete.pickup.id, requestNotToDelete.partner.id))

        RequestService.deleteRequest(RequestDeleteForm(pickup1.b.id, testPartner.id))
        val requestsInRepositoryAfterDelete = RequestService.getRequests(RequestGetForm())
        require(requestsInRepositoryAfterDelete is Either.Right)
        assert(!requestsInRepositoryAfterDelete.b.contains(requestToDelete))
        assert(requestsInRepositoryAfterDelete.b.contains(requestNotToDelete))
    }
}
